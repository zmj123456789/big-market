package cn.zmj.domain.activity.service.partake;

import cn.zmj.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import cn.zmj.domain.activity.model.entity.ActivityEntity;
import cn.zmj.domain.activity.model.entity.PartakeRaffleActivityEntity;
import cn.zmj.domain.activity.model.entity.UserRaffleOrderEntity;
import cn.zmj.domain.activity.model.valobj.ActivityStateVO;
import cn.zmj.domain.activity.repository.IActivityRepository;
import cn.zmj.domain.activity.service.IRaffleActivityPartakeService;
import cn.zmj.types.enums.ResponseCode;
import cn.zmj.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public abstract class AbstractRaffleActivityPartake implements IRaffleActivityPartakeService {
    protected final IActivityRepository activityRepository;

    public AbstractRaffleActivityPartake(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
//        基础信息
        Long activityId = partakeRaffleActivityEntity.getActivityId();
        String userId = partakeRaffleActivityEntity.getUserId();
        Date currentDate=new Date();
//        活动查询
        ActivityEntity activityEntity = activityRepository.queryRaffleActivityByActivityId(activityId);
//        校验活动状态
        if(!ActivityStateVO.open.equals(activityEntity.getState())){
            throw new AppException(ResponseCode.ACTIVITY_STATE_ERROR.getCode(),ResponseCode.ACTIVITY_STATE_ERROR.getCode());
        }
//        校验活动日期
        if(activityEntity.getBeginDateTime().after(currentDate)||activityEntity.getEndDateTime().before(currentDate)){
            throw new AppException(ResponseCode.ACTIVITY_DATE_ERROR.getCode(),ResponseCode.ACTIVITY_DATE_ERROR.getInfo());
        }
//        查询未被使用的活动参与订单记录
        UserRaffleOrderEntity userRaffleOrderEntity = activityRepository.queryNoUsedRaffleOrder(partakeRaffleActivityEntity);
        if(userRaffleOrderEntity!=null){
            log.info("创建参与活动订单 userId:{} activityId:{} userRaffleOrderEntity:{}", userId, activityId, JSON.toJSONString(userRaffleOrderEntity));
            return userRaffleOrderEntity;
        }
//        额度账户过滤，返回账户构建对象
        CreatePartakeOrderAggregate createPartakeOrderAggregate = this.doFilterAccount(userId, activityId, currentDate);
//        构建订单
        UserRaffleOrderEntity userRaffleOrder = this.buildUserRaffleOrder(userId, activityId, currentDate);
//        填充抽检单实体对象
        createPartakeOrderAggregate.setUserRaffleOrderEntity(userRaffleOrder);
//        保存聚合对象-一个领域内的聚合是一个事务
        activityRepository.saveCreatePartakeOrderAggregate(createPartakeOrderAggregate);
//        返回订单信息
        return userRaffleOrder;
    }
    protected abstract CreatePartakeOrderAggregate doFilterAccount(String userId,Long activityId,Date currentDate);
    protected abstract UserRaffleOrderEntity buildUserRaffleOrder(String userId,Long activityId,Date currentDate);
}
