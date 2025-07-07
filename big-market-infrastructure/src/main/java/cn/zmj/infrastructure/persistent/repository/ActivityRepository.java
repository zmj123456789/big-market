package cn.zmj.infrastructure.persistent.repository;

import cn.zmj.domain.activity.model.entity.ActivityCountEntity;
import cn.zmj.domain.activity.model.entity.ActivityEntity;
import cn.zmj.domain.activity.model.entity.ActivitySkuEntity;
import cn.zmj.domain.activity.repository.IActivityRepository;
import cn.zmj.infrastructure.persistent.dao.IRaffleActivityCountDao;
import cn.zmj.infrastructure.persistent.dao.IRaffleActivityDao;
import cn.zmj.infrastructure.persistent.dao.IRaffleActivitySkuDao;
import cn.zmj.infrastructure.persistent.po.RaffleActivity;
import cn.zmj.infrastructure.persistent.po.RaffleActivityCount;
import cn.zmj.infrastructure.persistent.po.RaffleActivitySku;
import cn.zmj.infrastructure.persistent.redis.IRedisService;
import cn.zmj.types.common.Constants;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class ActivityRepository implements IActivityRepository {
    @Resource
    private IRedisService redisService;
    @Resource
    private IRaffleActivityDao raffleActivityDao;
    @Resource
    private IRaffleActivitySkuDao raffleActivitySkuDao;
    @Resource
    private IRaffleActivityCountDao raffleActivityCountDao;

    @Override
    public ActivitySkuEntity queryActivitySku(Long sku) {
        RaffleActivitySku raffleActivitySku = raffleActivitySkuDao.queryActivitySku(sku);
        return ActivitySkuEntity.builder()
                              .sku(raffleActivitySku.getSku())
                              .activityId(raffleActivitySku.getActivityId())
                              .activityCountId(raffleActivitySku.getActivityCountId())
                              .stockCount(raffleActivitySku.getStockCount())
                              .stockCountSurplus(raffleActivitySku.getStockCountSurplus())
                              .build();

    }

    @Override
    public ActivityEntity queryRaffleActivityByActivityId(Long activityId) {
        String cacheKey=Constants.RedisKey.ACTIVITY_KEY+activityId;
        ActivityEntity activityEntity = redisService.getValue(cacheKey);
        if(activityEntity!=null)return activityEntity;
        RaffleActivity raffleActivity = raffleActivityDao.queryRaffleActivityByActivityId(activityId);

       activityEntity=ActivityEntity.builder()
                      .activityId(raffleActivity.getActivityId())
                      .activityName(raffleActivity.getActivityName())
                      .activityDesc(raffleActivity.getActivityDesc())
                      .beginDateTime(raffleActivity.getBeginDateTime())
                      .endDateTime(raffleActivity.getEndDateTime())
                      .strategyId(raffleActivity.getStrategyId())
                      .state(raffleActivity.getState())
                      .build();
        redisService.setValue(cacheKey,activityEntity);
        return activityEntity;
    }

    @Override
    public ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId) {
        String cacheKey=Constants.RedisKey.ACTIVITY_COUNT_KEY+activityCountId;
        ActivityCountEntity activityCountEntity = redisService.getValue(cacheKey);
        if(activityCountEntity!=null)return activityCountEntity;
        RaffleActivityCount raffleActivityCount = raffleActivityCountDao.queryRaffleActivityCountByActivityCountId(activityCountId);
        activityCountEntity=ActivityCountEntity.builder()
                                .activityCountId(raffleActivityCount.getActivityCountId())
                                .totalCount(raffleActivityCount.getTotalCount())
                                .dayCount(raffleActivityCount.getDayCount())
                                .monthCount(raffleActivityCount.getMonthCount())
                                .build();
        redisService.setValue(cacheKey,activityCountEntity);
        return activityCountEntity;
    }
}
