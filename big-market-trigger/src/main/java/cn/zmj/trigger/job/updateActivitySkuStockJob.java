package cn.zmj.trigger.job;

import cn.zmj.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import cn.zmj.domain.activity.service.ISkuStock;
import cn.zmj.domain.strategy.model.valobj.StrategyAwardStockKeyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class updateActivitySkuStockJob {
    @Resource
    private ISkuStock skuStock;
    @Scheduled(cron="0/5 * * * * ?")
    public void exec(){
        try {
            log.info("定时任务，更新奖品消耗库存【延迟队列获取，降低对数据库的更新频次，不要产生竞争】");
            ActivitySkuStockKeyVO activitySkuStockKeyVO=skuStock.takeQueueValue();
            if(null==activitySkuStockKeyVO) return;
            log.info("定时任务，更新sku库存sku:{} awardId:{}",activitySkuStockKeyVO.getSku(),activitySkuStockKeyVO.getActivityId());
            skuStock.updateActivitySkuStock(activitySkuStockKeyVO.getSku());
        } catch (Exception e) {
            log.info("定时任务更新sku库存失败",e);
        }
    }
}
