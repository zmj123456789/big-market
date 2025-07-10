package cn.zmj.domain.activity.service.quota;

import cn.zmj.domain.activity.model.entity.ActivityCountEntity;
import cn.zmj.domain.activity.model.entity.ActivityEntity;
import cn.zmj.domain.activity.model.entity.ActivitySkuEntity;
import cn.zmj.domain.activity.repository.IActivityRepository;
import cn.zmj.domain.activity.service.quota.rule.factory.DefaultActivityChainFactory;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动的支撑类
 * @create 2024-03-23 09:27
 */

public class RaffleActivityAccountQuotaSupport {
    protected DefaultActivityChainFactory defaultActivityChainFactory;
    protected IActivityRepository activityRepository;

    public RaffleActivityAccountQuotaSupport(DefaultActivityChainFactory defaultActivityChainFactory, IActivityRepository activityRepository) {
        this.defaultActivityChainFactory = defaultActivityChainFactory;
        this.activityRepository = activityRepository;
    }
    public ActivitySkuEntity queryActivitySku(Long sku){
        return activityRepository.queryActivitySku(sku);
    }
    public ActivityEntity queryRaffleActivityByActivityId(Long activityId){
        return activityRepository.queryRaffleActivityByActivityId(activityId);
    }
    public ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId){
        return activityRepository.queryRaffleActivityCountByActivityCountId(activityCountId);
    }
}
