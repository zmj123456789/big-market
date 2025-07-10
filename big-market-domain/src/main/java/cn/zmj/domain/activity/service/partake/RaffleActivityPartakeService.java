package cn.zmj.domain.activity.service.partake;

import cn.zmj.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import cn.zmj.domain.activity.model.entity.*;
import cn.zmj.domain.activity.model.valobj.UserRaffleOrderStateVO;
import cn.zmj.domain.activity.repository.IActivityRepository;
import cn.zmj.types.enums.ResponseCode;
import cn.zmj.types.exception.AppException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2024-04-05 07:53
 */
@Service
public class RaffleActivityPartakeService extends AbstractRaffleActivityPartake{
    private final SimpleDateFormat dateFormatMonth=new SimpleDateFormat("yyyy-MM");
    private final SimpleDateFormat dateFormatDay=new SimpleDateFormat("yyyy-MM-dd");

    public RaffleActivityPartakeService(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    @Override
    protected CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date currentDate) {
//        查询总账户额度
        ActivityAccountEntity activityAccountEntity = activityRepository.queryActivityAccountByUserId(userId, activityId);
        if(activityAccountEntity==null || activityAccountEntity.getTotalCountSurplus()<=0){
            throw new AppException(ResponseCode.ACCOUNT_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_QUOTA_ERROR.getInfo());
        }
        String month = dateFormatMonth.format(currentDate);
        String day = dateFormatDay.format(currentDate);
//        查询月账户额度
        ActivityAccountMonthEntity activityAccountMonthEntity =activityRepository.queryActivityAccountMonthByUserId(userId,activityId,month);
        if(activityAccountMonthEntity!=null&&activityAccountMonthEntity.getMonthCountSurplus()<=0){
            throw new AppException(ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getInfo());
        }
//        创建月额度
        boolean isExistAccountMonth=null!=activityAccountMonthEntity;
        if(null==activityAccountMonthEntity){
            activityAccountMonthEntity = new ActivityAccountMonthEntity();
            activityAccountMonthEntity.setActivityId(activityId);
            activityAccountMonthEntity.setUserId(userId);
            activityAccountMonthEntity.setMonth(month);
            activityAccountMonthEntity.setMonthCount(activityAccountEntity.getMonthCount());
            activityAccountMonthEntity.setMonthCountSurplus(activityAccountEntity.getMonthCountSurplus());
        }
//        查询日额度
        ActivityAccountDayEntity activityAccountDayEntity=activityRepository.queryActivityAccountDayByUserId(userId,activityId,day);
        if(activityAccountDayEntity!=null&&activityAccountDayEntity.getDayCountSurplus()<=0){
            throw new AppException(ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getInfo());
        }
        boolean isExistAccountDay=null!=activityAccountDayEntity;
        if(null==activityAccountDayEntity){
            activityAccountDayEntity = new ActivityAccountDayEntity();
            activityAccountDayEntity.setUserId(userId);
            activityAccountDayEntity.setActivityId(activityId);
            activityAccountDayEntity.setDay(day);
            activityAccountDayEntity.setDayCount(activityAccountEntity.getDayCount());
            activityAccountDayEntity.setDayCountSurplus(activityAccountEntity.getDayCountSurplus());

        }
        CreatePartakeOrderAggregate createPartakeOrderAggregate = new CreatePartakeOrderAggregate();
        createPartakeOrderAggregate.setUserId(userId);
        createPartakeOrderAggregate.setActivityId(activityId);
        createPartakeOrderAggregate.setActivityAccountEntity(activityAccountEntity);
        createPartakeOrderAggregate.setExistAccountMonth(isExistAccountMonth);
        createPartakeOrderAggregate.setActivityAccountMonthEntity(activityAccountMonthEntity);
        createPartakeOrderAggregate.setExistAccountDay(isExistAccountDay);
        createPartakeOrderAggregate.setActivityAccountDayEntity(activityAccountDayEntity);
        return createPartakeOrderAggregate;

    }

    @Override
    protected UserRaffleOrderEntity buildUserRaffleOrder(String userId, Long activityId, Date currentDate) {
        ActivityEntity activityEntity = activityRepository.queryRaffleActivityByActivityId(activityId);
//        构建订单
        return UserRaffleOrderEntity.builder()
                .userId(userId)
                .activityId(activityId)
                .activityName(activityEntity.getActivityName())
                .strategyId(activityEntity.getStrategyId())
                .orderId(RandomStringUtils.randomNumeric(12))
                .orderTime(currentDate)
                .orderState(UserRaffleOrderStateVO.create)
                .build();
    }
}
