package cn.zmj.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import cn.zmj.domain.activity.event.ActivitySkuStockZeroMessageEvent;
import cn.zmj.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import cn.zmj.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.zmj.domain.activity.model.entity.*;
import cn.zmj.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import cn.zmj.domain.activity.model.valobj.ActivityStateVO;
import cn.zmj.domain.activity.model.valobj.UserRaffleOrderStateVO;
import cn.zmj.domain.activity.repository.IActivityRepository;
import cn.zmj.infrastructure.event.EventPublisher;
import cn.zmj.infrastructure.persistent.dao.*;
import cn.zmj.infrastructure.persistent.po.*;
import cn.zmj.infrastructure.persistent.redis.IRedisService;
import cn.zmj.types.common.Constants;
import cn.zmj.types.enums.ResponseCode;
import cn.zmj.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
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
    @Resource
    private IDBRouterStrategy dbRouter;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private IRaffleActivityOrderDao raffleActivityOrderDao;
    @Resource
    private IRaffleActivityAccountDao raffleActivityAccountDao;
    @Autowired
    private EventPublisher eventPublisher;
    @Autowired
    private ActivitySkuStockZeroMessageEvent activitySkuStockZeroMessageEvent;
    @Resource
    private IUserRaffleOrderDao userRaffleOrderDao;
    @Resource
    private IRaffleActivityAccountMonthDao raffleActivityAccountMonthDao;
    @Resource
    private IRaffleActivityAccountDayDao raffleActivityAccountDayDao;


    @Override
    public ActivitySkuEntity queryActivitySku(Long sku) {
        RaffleActivitySku raffleActivitySku = raffleActivitySkuDao.queryActivitySku(sku);
        String cacheKey=Constants.RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY+sku;
        Long cacheSkuStock = redisService.getAtomicLong(cacheKey);
        if(cacheSkuStock==null||cacheSkuStock==0){
            cacheSkuStock=0L;
        }
        return ActivitySkuEntity.builder()
                              .sku(raffleActivitySku.getSku())
                              .activityId(raffleActivitySku.getActivityId())
                              .activityCountId(raffleActivitySku.getActivityCountId())
                              .stockCount(raffleActivitySku.getStockCount())
                              .stockCountSurplus(cacheSkuStock.intValue())
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
                      .state(ActivityStateVO.valueOf(raffleActivity.getState()))
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

    @Override
    public void doSaveOrder(CreateQuotaOrderAggregate createQuotaOrderAggregate) {
        try {
//        订单对象
            ActivityOrderEntity activityOrderEntity = createQuotaOrderAggregate.getActivityOrderEntity();
            RaffleActivityOrder raffleActivityOrder = new RaffleActivityOrder();
            raffleActivityOrder.setUserId(activityOrderEntity.getUserId());
            raffleActivityOrder.setSku(activityOrderEntity.getSku());
            raffleActivityOrder.setActivityId(activityOrderEntity.getActivityId());
            raffleActivityOrder.setActivityName(activityOrderEntity.getActivityName());
            raffleActivityOrder.setStrategyId(activityOrderEntity.getStrategyId());
            raffleActivityOrder.setOrderId(activityOrderEntity.getOrderId());
            raffleActivityOrder.setOrderTime(activityOrderEntity.getOrderTime());
            raffleActivityOrder.setTotalCount(activityOrderEntity.getTotalCount());
            raffleActivityOrder.setDayCount(activityOrderEntity.getDayCount());
            raffleActivityOrder.setMonthCount(activityOrderEntity.getMonthCount());
            raffleActivityOrder.setState(activityOrderEntity.getState().getCode());
            raffleActivityOrder.setOutBusinessNo(activityOrderEntity.getOutBusinessNo());
//账户对象
            RaffleActivityAccount raffleActivityAccount = new RaffleActivityAccount();
            raffleActivityAccount.setUserId(createQuotaOrderAggregate.getUserId());
            raffleActivityAccount.setActivityId(createQuotaOrderAggregate.getActivityId());
            raffleActivityAccount.setTotalCount(createQuotaOrderAggregate.getTotalCount());
            raffleActivityAccount.setTotalCountSurplus(createQuotaOrderAggregate.getTotalCount());
            raffleActivityAccount.setDayCount(createQuotaOrderAggregate.getDayCount());
            raffleActivityAccount.setDayCountSurplus(createQuotaOrderAggregate.getDayCount());
            raffleActivityAccount.setMonthCount(createQuotaOrderAggregate.getMonthCount());
            raffleActivityAccount.setMonthCountSurplus(createQuotaOrderAggregate.getMonthCount());
            dbRouter.doRouter(createQuotaOrderAggregate.getUserId());
//        编程式事务
            transactionTemplate.execute(status->{
                try {
    //            写入订单
                    raffleActivityOrderDao.insert(raffleActivityOrder);
    //            更新账户
                    int count = raffleActivityAccountDao.updateAccountQuota(raffleActivityAccount);
    //            创建账户
                    if(0==count){
                        raffleActivityAccountDao.insert(raffleActivityAccount);
                    }
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.info("写入订单唯一索引冲突，userId:{},activityId:{},sku:{}",activityOrderEntity.getUserId(),activityOrderEntity.getActivityId(),activityOrderEntity.getSku(),e);

                    throw new AppException(ResponseCode.INDEX_DUP.getCode(),e);
                }
            });
        } finally {
            dbRouter.clear();
        }
    }

    @Override
    public void cacheActivitySkuStockCount(String cacheKey, Integer stockCount) {
        if(redisService.isExists(cacheKey))return;
        redisService.setAtomicLong(cacheKey,stockCount);
    }

    @Override
    public boolean substractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime) {
        long surplus = redisService.decr(cacheKey);
        if(surplus==0){
            // 库存消耗没了以后，发送MQ消息，更新数据库库存
            eventPublisher.publish(activitySkuStockZeroMessageEvent.topic(),activitySkuStockZeroMessageEvent.buildEvent(sku));

            return false;
        }else if(surplus<0){
// 库存小于0，恢复为0个
            redisService.setAtomicLong(cacheKey,0);
            return false;
        }
        // 1. 按照cacheKey decr 后的值，如 99、98、97 和 key 组成为库存锁的key进行使用。
        // 2. 加锁为了兜底，如果后续有恢复库存，手动处理等【运营是人来操作，会有这种情况发放，系统要做防护】，也不会超卖。因为所有的可用库存key，都被加锁了。
        // 3. 设置加锁时间为活动到期 + 延迟1天
        String lockKey=cacheKey+Constants.UNDERLINE+surplus;
        long expireMillis=endDateTime.getTime()-System.currentTimeMillis()+ TimeUnit.DAYS.toMillis(1);
        Boolean lock = redisService.setNx(lockKey, expireMillis, TimeUnit.MILLISECONDS);
        if(!lock){
            log.info("活动加锁失败{}",lockKey);
        }
        return lock;
    }

    @Override
    public void activitySkuStockConsumerSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO) {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY;
        RBlockingQueue<ActivitySkuStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<ActivitySkuStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(activitySkuStockKeyVO,3, TimeUnit.SECONDS);
    }

    @Override
    public ActivitySkuStockKeyVO takeQueueValue() {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY;
        RBlockingQueue<ActivitySkuStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        return blockingQueue.poll();
    }

    @Override
    public void clearQueueValue() {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY;
        RBlockingQueue<ActivitySkuStockKeyVO> destinationQueue = redisService.getBlockingQueue(cacheKey);
        destinationQueue.clear();
    }

    @Override
    public void updateActivitySkuStock(Long sku) {
        raffleActivitySkuDao.updateActivitySkuStock(sku);
    }

    @Override
    public void clearActivitySkuStock(Long sku) {
        raffleActivitySkuDao.clearActivitySkuStock(sku);
    }

    @Override
    public UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
//        查询数据
        UserRaffleOrder userRaffleOrder = new UserRaffleOrder();
        userRaffleOrder.setUserId(partakeRaffleActivityEntity.getUserId());
        userRaffleOrder.setActivityId(partakeRaffleActivityEntity.getActivityId());
        UserRaffleOrder userRaffleOrderRes = userRaffleOrderDao.queryNoUsedRaffleOrder(userRaffleOrder);
        if(userRaffleOrderRes==null)return null;
//        封装结果
        return UserRaffleOrderEntity.builder()
                .userId(userRaffleOrderRes.getUserId())
                .activityId(userRaffleOrderRes.getActivityId())
                .activityName(userRaffleOrderRes.getActivityName())
                .strategyId(userRaffleOrderRes.getStrategyId())
                .orderId(userRaffleOrderRes.getOrderId())
                .orderTime(userRaffleOrderRes.getOrderTime())
                .orderState(UserRaffleOrderStateVO.valueOf(userRaffleOrderRes.getOrderState()))
                .build();
    }

    @Override
    public ActivityAccountEntity queryActivityAccountByUserId(String userId, Long activityId) {
//查询数据
        RaffleActivityAccount raffleActivityAccountReq=new RaffleActivityAccount();
        raffleActivityAccountReq.setUserId(userId);
        raffleActivityAccountReq.setActivityId(activityId);
        RaffleActivityAccount raffleActivityAccountRes=raffleActivityAccountDao.queryActivityAccountByUserId(raffleActivityAccountReq);
//        封装对象
        if(raffleActivityAccountRes==null)return null;
        return ActivityAccountEntity.builder()
                  .userId(raffleActivityAccountRes.getUserId())
                  .activityId(raffleActivityAccountRes.getActivityId())
                  .totalCount(raffleActivityAccountRes.getTotalCount())
                  .totalCountSurplus(raffleActivityAccountRes.getTotalCountSurplus())
                  .dayCount(raffleActivityAccountRes.getDayCount())
                  .dayCountSurplus(raffleActivityAccountRes.getDayCountSurplus())
                  .monthCount(raffleActivityAccountRes.getMonthCount())
                  .monthCountSurplus(raffleActivityAccountRes.getMonthCountSurplus())
                  .build();
    }

    @Override
    public ActivityAccountMonthEntity queryActivityAccountMonthByUserId(String userId, Long activityId, String month) {
        RaffleActivityAccountMonth raffleActivityAccountMonthReq = new RaffleActivityAccountMonth();
        raffleActivityAccountMonthReq.setUserId(userId);
        raffleActivityAccountMonthReq.setActivityId(activityId);
        raffleActivityAccountMonthReq.setMonth(month);
        RaffleActivityAccountMonth activityAccountMonthRes=raffleActivityAccountMonthDao.queryActivityAccountMonthByUserId(raffleActivityAccountMonthReq);
        if(activityAccountMonthRes==null)return null;
        return ActivityAccountMonthEntity.builder()
                .userId(activityAccountMonthRes.getUserId())
                .activityId(activityAccountMonthRes.getActivityId())
                .month(activityAccountMonthRes.getMonth())
                .monthCount(activityAccountMonthRes.getMonthCount())
                .monthCountSurplus(activityAccountMonthRes.getMonthCountSurplus())
                .build();
    }

    @Override
    public ActivityAccountDayEntity queryActivityAccountDayByUserId(String userId, Long activityId, String day) {
        RaffleActivityAccountDay raffleActivityAccountDayReq = new RaffleActivityAccountDay();
        raffleActivityAccountDayReq.setUserId(userId);
        raffleActivityAccountDayReq.setActivityId(activityId);
        raffleActivityAccountDayReq.setDay(day);
        RaffleActivityAccountDay raffleActivityAccountDay=raffleActivityAccountDayDao.queryActivityAccountDayByUserId(raffleActivityAccountDayReq);
        if(raffleActivityAccountDay==null)return null;
        return ActivityAccountDayEntity.builder()
                .userId(raffleActivityAccountDay.getUserId())
                .activityId(raffleActivityAccountDay.getActivityId())
                .day(raffleActivityAccountDay.getDay())
                .dayCount(raffleActivityAccountDay.getDayCount())
                .dayCountSurplus(raffleActivityAccountDay.getDayCountSurplus())
                .build();
    }

    @Override
    public void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate) {
        try {
            String userId = createPartakeOrderAggregate.getUserId();
            Long activityId = createPartakeOrderAggregate.getActivityId();
            ActivityAccountEntity activityAccountEntity = createPartakeOrderAggregate.getActivityAccountEntity();
            ActivityAccountMonthEntity activityAccountMonthEntity = createPartakeOrderAggregate.getActivityAccountMonthEntity();
            ActivityAccountDayEntity activityAccountDayEntity = createPartakeOrderAggregate.getActivityAccountDayEntity();
            UserRaffleOrderEntity userRaffleOrderEntity = createPartakeOrderAggregate.getUserRaffleOrderEntity();
//        统一切换路由，以下事务所有操作都走一个路由
            dbRouter.doRouter(userId);
            transactionTemplate.execute(
                    status -> {
                        try {
    //                    更新总账户
                            int totalCount = raffleActivityAccountDao.updateActivityAccountSubstractionQuota(RaffleActivityAccount.builder().userId(userId).activityId(activityId).build());
                            if(1!=totalCount){
                                status.setRollbackOnly();
                                log.warn("写入创建参与活动记录，更新总账户额度不足，异常 userId: {} activityId: {}", userId, activityId);
                                throw new AppException(ResponseCode.ACCOUNT_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_QUOTA_ERROR.getInfo());
                            }
    //                    创建和更新月账户，true存在则更新false不存在则插入
                            if(createPartakeOrderAggregate.isExistAccountMonth()){
                                int updateMonthCount = raffleActivityAccountMonthDao.updateActivityAccountMonthSubstractionQuota(RaffleActivityAccountMonth.builder().userId(userId).activityId(activityId).month(activityAccountMonthEntity.getMonth()).build());
                                if(updateMonthCount!=1){
        //                            未更新成功则回滚
                                    status.setRollbackOnly();
                                    log.warn("写入创建参与活动记录，更新月账户额度不足，异常 userId: {} activityId: {} month: {}", userId, activityId, activityAccountMonthEntity.getMonth());
                                    throw new AppException(ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getInfo());
                                }
                                raffleActivityAccountDao.updateActivityAccountMonthSubstractionQuota(RaffleActivityAccount.builder().userId(userId).activityId(activityId).build());

                            }else{
                                 raffleActivityAccountMonthDao.insertActivityAccountMonth(RaffleActivityAccountMonth.builder()
                                                                                                                  .userId(activityAccountMonthEntity.getUserId())
                                                                                                                  .activityId(activityAccountMonthEntity.getActivityId())
                                                                                                                  .month(activityAccountMonthEntity.getMonth())
                                                                                                                  .monthCount(activityAccountMonthEntity.getMonthCount())
                                                                                                                  .monthCountSurplus(activityAccountMonthEntity.getMonthCountSurplus()-1)
                                                                                                                  .build());
        // 新创建月账户，则更新总账表中月镜像额度
                                raffleActivityAccountDao.updateActivityAccountMonthSurplusImageQuota(RaffleActivityAccount.builder().userId(userId).activityId(activityId).monthCountSurplus(activityAccountMonthEntity.getMonthCountSurplus()).build());
                            }
    //                    创建或更新日账户，true - 存在则更新，false - 不存在则插入
                            if (createPartakeOrderAggregate.isExistAccountDay()) {
                                int updateDayCount = raffleActivityAccountDayDao.updateActivityAccountDaySubtractionQuota(RaffleActivityAccountDay.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .day(activityAccountDayEntity.getDay())
                                        .build());
                                if (1 != updateDayCount) {
                                    // 未更新成功则回滚
                                    status.setRollbackOnly();
                                    log.warn("写入创建参与活动记录，更新日账户额度不足，异常 userId: {} activityId: {} day: {}", userId, activityId, activityAccountDayEntity.getDay());
                                    throw new AppException(ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getInfo());
                                }
                                // 更新总账户中日镜像库存
                                raffleActivityAccountDao.updateActivityAccountDaySubstractionQuota(
                                        RaffleActivityAccount.builder()
                                                .userId(userId)
                                                .activityId(activityId)
                                                .build());
                            } else {
                                raffleActivityAccountDayDao.insertActivityAccountDay(RaffleActivityAccountDay.builder()
                                        .userId(activityAccountDayEntity.getUserId())
                                        .activityId(activityAccountDayEntity.getActivityId())
                                        .day(activityAccountDayEntity.getDay())
                                        .dayCount(activityAccountDayEntity.getDayCount())
                                        .dayCountSurplus(activityAccountDayEntity.getDayCountSurplus() - 1)
                                        .build());
                                // 新创建日账户，则更新总账表中日镜像额度
                                raffleActivityAccountDao.updateActivityAccountDaySurplusImageQuota(RaffleActivityAccount.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .dayCountSurplus(activityAccountEntity.getDayCountSurplus())
                                        .build());
                            }
    //                    写入参与活动订单
                            userRaffleOrderDao.insert(UserRaffleOrder.builder()
                                                    .userId(userRaffleOrderEntity.getUserId())
                                                    .activityId(userRaffleOrderEntity.getActivityId())
                                                    .activityName(userRaffleOrderEntity.getActivityName())
                                                    .strategyId(userRaffleOrderEntity.getStrategyId())
                                                    .orderId(userRaffleOrderEntity.getOrderId())
                                                    .orderTime(userRaffleOrderEntity.getOrderTime())
                                                    .orderState(userRaffleOrderEntity.getOrderState().getCode())
                                                    .build());
                            return 1;
                        } catch (DuplicateKeyException e) {
                            status.setRollbackOnly();
                            log.error("写入创建参与活动记录，唯一索引冲突 userId: {} activityId: {}", userId, activityId, e);
                            throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                        }
                    });
        } finally {
            dbRouter.clear();
        }
    }

    @Override
    public List<ActivitySkuEntity> queryRaffleActivitySkuListByActivityId(Long activityId) {
        List<RaffleActivitySku> raffleActivitySkus=raffleActivitySkuDao.queryActivitySkuListByActivityId(activityId);
        List<ActivitySkuEntity> activitySkuEntities=new ArrayList<>(raffleActivitySkus.size());
        for (RaffleActivitySku activitySkus : raffleActivitySkus) {
            ActivitySkuEntity activitySkuEntity=new ActivitySkuEntity();
            activitySkuEntity.setSku(activitySkus.getSku());
            activitySkuEntity.setActivityId(activitySkus.getActivityId());
            activitySkuEntity.setActivityCountId(activitySkus.getActivityCountId());
            activitySkuEntity.setStockCount(activitySkus.getStockCount());
            activitySkuEntity.setStockCountSurplus(activitySkus.getStockCountSurplus());
            activitySkuEntities.add(activitySkuEntity);
        }
        return activitySkuEntities;
    }
}
