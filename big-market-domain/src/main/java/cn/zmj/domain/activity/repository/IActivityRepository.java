package cn.zmj.domain.activity.repository;

import cn.zmj.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import cn.zmj.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.zmj.domain.activity.model.entity.*;
import cn.zmj.domain.activity.model.valobj.ActivitySkuStockKeyVO;

import java.util.Date;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 活动仓储接口，定义需要哪些东西，给其他模块实现
 * @create 2024-03-16 10:31
 */
public interface IActivityRepository {
    ActivitySkuEntity queryActivitySku(Long sku);

    ActivityEntity queryRaffleActivityByActivityId(Long activityId);

    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);
    void doSaveOrder(CreateQuotaOrderAggregate createQuotaOrderAggregate);
    void cacheActivitySkuStockCount(String cacheKey,Integer stockCount);
    boolean substractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);
    void activitySkuStockConsumerSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);
    ActivitySkuStockKeyVO takeQueueValue();
    void clearQueueValue();
    void updateActivitySkuStock(Long sku);
    void clearActivitySkuStock(Long sku);

    UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    ActivityAccountEntity queryActivityAccountByUserId(String userId, Long activityId);

    ActivityAccountMonthEntity queryActivityAccountMonthByUserId(String userId, Long activityId, String month);

    ActivityAccountDayEntity queryActivityAccountDayByUserId(String userId, Long activityId, String day);

    void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate);

    List<ActivitySkuEntity> queryRaffleActivitySkuListByActivityId(Long activityId);

    Integer queryRaffleAccountDayPartakeCount(Long activityId, String userId);

    ActivityAccountEntity queryActivityAccountEntity(Long activityId, String userId);

    Integer queryRaffleActivityAccountPartakeCount(Long activityId, String userId);
}
