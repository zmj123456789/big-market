package cn.zmj.trigger.http;

import cn.zmj.domain.activity.model.entity.ActivityAccountEntity;
import cn.zmj.domain.activity.model.entity.UserRaffleOrderEntity;
import cn.zmj.domain.activity.service.armory.ActivityArmory;
import cn.zmj.domain.activity.service.armory.IActivityArmory;
import cn.zmj.domain.activity.service.partake.RaffleActivityPartakeService;
import cn.zmj.domain.activity.service.quota.RaffleActivityAccountQuotaService;
import cn.zmj.domain.award.model.entity.UserAwardRecordEntity;
import cn.zmj.domain.award.model.valobj.AwardStateVO;
import cn.zmj.domain.award.service.AwardService;
import cn.zmj.domain.rebate.model.entity.BehaviorEntity;
import cn.zmj.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import cn.zmj.domain.rebate.model.valobj.BehaviorTypeVO;
import cn.zmj.domain.rebate.service.IBehaviorRebateService;
import cn.zmj.domain.strategy.model.entity.RaffleAwardEntity;
import cn.zmj.domain.strategy.model.entity.RaffleFactorEntity;
import cn.zmj.domain.strategy.service.IRaffleStrategy;
import cn.zmj.domain.strategy.service.armory.IStrategyArmory;
import cn.zmj.trigger.api.IRaffleActivityService;
import cn.zmj.trigger.api.dto.ActivityDrawRequestDTO;
import cn.zmj.trigger.api.dto.ActivityDrawResponseDTO;
import cn.zmj.trigger.api.dto.UserActivityAccountRequestDTO;
import cn.zmj.trigger.api.dto.UserActivityAccountResponseDTO;
import cn.zmj.types.enums.ResponseCode;
import cn.zmj.types.exception.AppException;
import cn.zmj.types.model.Response;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/raffle/activity")
public class RaffleActivityController implements IRaffleActivityService {
    @Resource
    private IActivityArmory activityArmory;
    @Resource
    private IStrategyArmory strategyArmory;
    @Autowired
    private RaffleActivityPartakeService raffleActivityPartakeService;
    @Resource
    private IRaffleStrategy raffleStrategy;
    @Autowired
    private AwardService awardService;
    @Resource
    private IBehaviorRebateService behaviorRebateService;
    private final SimpleDateFormat dateFormatDay=new SimpleDateFormat("yyyyMMdd");
    @Autowired
    private RaffleActivityAccountQuotaService raffleActivityAccountQuotaService;

    /**
     * 活动装配 - 数据预热 | 把活动配置的对应的 sku 一起装配
     *
     * @param activityId 活动ID
     * @return 装配结果
     * <p>
     * 接口：<a href="http://localhost:8091/api/v1/raffle/activity/armory">/api/v1/raffle/activity/armory</a>
     * 入参：{"activityId":100001,"userId":"xiaofuge"}
     *
     * curl --request GET \
     *   --url 'http://localhost:8091/api/v1/raffle/activity/armory?activityId=100301'
     */
    @RequestMapping(value = "armory",method = RequestMethod.GET)
    @Override
    public Response<Boolean> armory(@RequestParam Long activityId) {
        try {
            log.info("活动装配，数据预热，开始 activityId:{}", activityId);
//        活动装配
            activityArmory.assembleActivitySkuByActivityId(activityId);
//        策略装配
            strategyArmory.assembleLotteryStrategyByActivityId(activityId);
            Response<Boolean> response = Response.<Boolean>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).data(true).build();
            log.info("活动装配，数据预热，完成 activityId:{}", activityId);
            return response;
        } catch (Exception e) {
            log.error("活动装配，数据预热，失败 activityId:{}", activityId, e);
            Response<Boolean> response = Response.<Boolean>builder().code(ResponseCode.UN_ERROR.getCode()).info(ResponseCode.UN_ERROR.getInfo()).build();
            return response;
        }

    }
    /**
     * 抽奖接口
     *
     * @param request 请求对象
     * @return 抽奖结果
     * <p>
     * 接口：<a href="http://localhost:8091/api/v1/raffle/activity/draw">/api/v1/raffle/activity/draw</a>
     * 入参：{"activityId":100001,"userId":"xiaofuge"}
     *
     * curl --request POST \
     *   --url http://localhost:8091/api/v1/raffle/activity/draw \
     *   --header 'content-type: application/json' \
     *   --data '{
     *     "userId":"xiaofuge",
     *     "activityId": 100301
     * }'
     */
    @RequestMapping(value="draw",method = RequestMethod.POST)
    @Override
    public Response<ActivityDrawResponseDTO> draw(@RequestBody ActivityDrawRequestDTO request) {
        try {
            log.info("活动抽奖 userId:{} activityId:{}", request.getUserId(), request.getActivityId());
//        参数校验
            if(StringUtils.isBlank(request.getUserId()) || request.getActivityId()==null){
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
//        参与活动,创建参与记录订单
            UserRaffleOrderEntity orderEntity = raffleActivityPartakeService.createOrder(request.getUserId(), request.getActivityId());
            log.info("活动抽奖，创建订单userId:{},activityId:{},orderId:{}",request.getUserId(),request.getActivityId(),orderEntity.getOrderId());
//        抽奖策略执行抽奖
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(RaffleFactorEntity.builder().userId(request.getUserId()).strategyId(orderEntity.getStrategyId()).endTime(orderEntity.getEndDateTime()).build());

//        存放结果写入中奖记录
            UserAwardRecordEntity userAwardRecordEntity=new UserAwardRecordEntity();

            userAwardRecordEntity.setUserId(orderEntity.getUserId());
            userAwardRecordEntity.setActivityId(orderEntity.getActivityId());
            userAwardRecordEntity.setStrategyId(orderEntity.getStrategyId());
            userAwardRecordEntity.setOrderId(orderEntity.getOrderId());
            userAwardRecordEntity.setAwardId(raffleAwardEntity.getAwardId());
            userAwardRecordEntity.setAwardTitle(raffleAwardEntity.getAwardTitle());
            userAwardRecordEntity.setAwardTime(new Date());
            userAwardRecordEntity.setAwardState(AwardStateVO.create);
            awardService.saveUserAwardRecord(userAwardRecordEntity);
//        返回结果
            return Response.<ActivityDrawResponseDTO>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo())
                    .data(ActivityDrawResponseDTO.builder().awardId(raffleAwardEntity.getAwardId()).awardTitle(raffleAwardEntity.getAwardTitle()).awardIndex(raffleAwardEntity.getSort()).build()).build();
        } catch (AppException e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<ActivityDrawResponseDTO>builder().code(e.getCode()).info(e.getInfo()).build();

        }catch (Exception e){
            log.error("活动抽奖失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<ActivityDrawResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();

        }
    }
    /**
     * 日历签到返利接口
     *
     * @param userId 用户ID
     * @return 签到返利结果
     * <p>
     * 接口：<a href="http://localhost:8091/api/v1/raffle/activity/calendar_sign_rebate">/api/v1/raffle/activity/calendar_sign_rebate</a>
     * 入参：xiaofuge
     * <p>
     * curl -X POST http://localhost:8091/api/v1/raffle/activity/calendar_sign_rebate -d "userId=xiaofuge" -H "Content-Type: application/x-www-form-urlencoded"
     */
    @RequestMapping(value="calendar_sign_rebate",method = RequestMethod.POST)
    @Override
    public Response<Boolean> calendarSignRebate(String userId) {
        try {
            log.info("日历签到返利开始userId{}",userId);
            BehaviorEntity behaviorEntity=new BehaviorEntity();
            behaviorEntity.setUserId(userId);
            behaviorEntity.setBehaviorTypeVO(BehaviorTypeVO.SIGN);
            behaviorEntity.setOutBusinessNo(dateFormatDay.format(new Date()));
            List<String> orderIds = behaviorRebateService.createOrder(behaviorEntity);
            log.info("日历签到返利完成 userId:{} orderIds: {}", userId, JSON.toJSONString(orderIds));
            return Response.<Boolean>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).data(true).build();
        } catch (AppException e) {
            log.error("日历签到返利异常 userId:{} ", userId, e);
            return Response.<Boolean>builder().code(e.getCode()).info(e.getInfo()).build();
        }catch (Exception e) {
            log.error("日历签到返利失败 userId:{}", userId,e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }
    /**
     * 查询账户额度
     * <p>
     * curl --request POST \
     * --url http://localhost:8091/api/v1/raffle/activity/query_user_activity_account \
     * --header 'content-type: application/json' \
     * --data '{
     * "userId":"xiaofuge",
     * "activityId": 100301
     * }'
     */
    @RequestMapping(value="query_user_activity_account",method=RequestMethod.POST)
    @Override
    public Response<UserActivityAccountResponseDTO> queryUserActivityAccount(UserActivityAccountRequestDTO request) {
        try {
            log.info("查询用户活动账户开始 userId:{} activityId:{}", request.getUserId(), request.getActivityId());
//        参数校验
            if(StringUtils.isBlank(request.getUserId())||request.getActivityId()==null){
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            ActivityAccountEntity activityAccountEntity=raffleActivityAccountQuotaService.queryActivityAccountEntity(request.getActivityId(),request.getUserId());
            UserActivityAccountResponseDTO userActivityAccountResponseDTO=new UserActivityAccountResponseDTO();
            userActivityAccountResponseDTO.setTotalCount(activityAccountEntity.getTotalCount());
            userActivityAccountResponseDTO.setTotalCountSurplus(activityAccountEntity.getTotalCountSurplus());
            userActivityAccountResponseDTO.setDayCount(activityAccountEntity.getDayCount());
            userActivityAccountResponseDTO.setDayCountSurplus(activityAccountEntity.getDayCountSurplus());
            userActivityAccountResponseDTO.setMonthCount(activityAccountEntity.getMonthCount());
            userActivityAccountResponseDTO.setMonthCountSurplus(activityAccountEntity.getMonthCountSurplus());
            log.info("查询用户活动账户完成 userId:{} activityId:{} dto:{}", request.getUserId(), request.getActivityId(), JSON.toJSONString(userActivityAccountResponseDTO));
            return Response.<UserActivityAccountResponseDTO>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).data(userActivityAccountResponseDTO).build();
        } catch (AppException e) {
            log.error("查询用户活动账户失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<UserActivityAccountResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
    /**
     * 判断是否签到接口
     * <p>
     * curl -X POST http://localhost:8091/api/v1/raffle/activity/is_calendar_sign_rebate -d "userId=xiaofuge" -H "Content-Type: application/x-www-form-urlencoded"
     */
    @RequestMapping(value="is_calendar_sign_rebate",method=RequestMethod.POST)
    @Override
    public Response<Boolean> isCalendarSignRebate(String userId) {
        try {
            log.info("查询用户是否完成日历签到返利开始 userId:{}", userId);
            String outBusinessNo = dateFormatDay.format(new Date());
            List<BehaviorRebateOrderEntity> behaviorRebateOrderEntities=behaviorRebateService.queryOrderByOutBusinessNo(userId,outBusinessNo);
            log.info("查询用户是否完成日历签到返利完成 userId:{} orders.size:{}", userId, behaviorRebateOrderEntities.size());
            return Response.<Boolean>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).data(!behaviorRebateOrderEntities.isEmpty()).build();
        } catch (Exception e) {
            log.error("查询用户是否完成日历签到返利失败 userId:{}", userId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();

        }

    }




}
