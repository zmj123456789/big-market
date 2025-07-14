package cn.zmj.domain.activity.service.quota;

import cn.zmj.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.zmj.domain.activity.model.entity.*;
import cn.zmj.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import cn.zmj.domain.activity.model.valobj.OrderStateVO;
import cn.zmj.domain.activity.repository.IActivityRepository;
import cn.zmj.domain.activity.service.IRaffleActivitySjuStockService;
import cn.zmj.domain.activity.service.quota.rule.factory.DefaultActivityChainFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RaffleActivityAccountQuotaService extends AbstractRaffleActivityAccountQuota implements IRaffleActivitySjuStockService {
    public RaffleActivityAccountQuotaService(DefaultActivityChainFactory defaultActivityChainFactory, IActivityRepository activityRepository) {
        super(defaultActivityChainFactory, activityRepository);
    }

    @Override
    protected void doSaveOrder(CreateQuotaOrderAggregate createQuotaOrderAggregate) {
        activityRepository.doSaveOrder(createQuotaOrderAggregate);
    }

    @Override
    protected CreateQuotaOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
//        订单实体对象
        ActivityOrderEntity activityOrderEntity=new ActivityOrderEntity();
        activityOrderEntity.setUserId(skuRechargeEntity.getUserId());
        activityOrderEntity.setSku(skuRechargeEntity.getSku());
        activityOrderEntity.setActivityId(activityEntity.getActivityId());
        activityOrderEntity.setActivityName(activityEntity.getActivityName());
        activityOrderEntity.setStrategyId(activityEntity.getStrategyId());
        activityOrderEntity.setOrderId(RandomStringUtils.randomNumeric(12));
        activityOrderEntity.setOrderTime(new Date());
        activityOrderEntity.setTotalCount(activityCountEntity.getTotalCount());
        activityOrderEntity.setDayCount(activityCountEntity.getDayCount());
        activityOrderEntity.setMonthCount(activityCountEntity.getMonthCount());
        activityOrderEntity.setState(OrderStateVO.completed);
        activityOrderEntity.setOutBusinessNo(skuRechargeEntity.getOutBusinessNo());
//        构建聚合对象
       return CreateQuotaOrderAggregate.builder()
                      .userId(activityOrderEntity.getUserId())
                      .activityId(activityOrderEntity.getActivityId())
                      .totalCount(activityOrderEntity.getTotalCount())
                      .dayCount(activityOrderEntity.getDayCount())
                      .monthCount(activityOrderEntity.getMonthCount())
                        .activityOrderEntity(activityOrderEntity)
                      .build();

    }

    @Override
    public ActivitySkuStockKeyVO takeQueueValue() throws InterruptedException {
        return  activityRepository.takeQueueValue();
    }

    @Override
    public void clearQueueValue() {
        activityRepository.clearQueueValue();
    }

    @Override
    public void updateActivitySkuStock(Long sku) {
        activityRepository.updateActivitySkuStock(sku);
    }

    @Override
    public void clearActivitySkuStock(Long sku) {
        activityRepository.clearActivitySkuStock(sku);
    }

    @Override
    public Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId) {
        return activityRepository.queryRaffleAccountDayPartakeCount(activityId,userId);
    }
}
