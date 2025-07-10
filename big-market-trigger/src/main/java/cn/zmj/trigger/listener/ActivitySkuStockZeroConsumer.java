package cn.zmj.trigger.listener;

import cn.zmj.domain.activity.service.IRaffleActivitySjuStockService;
import cn.zmj.types.event.BaseEvent;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class ActivitySkuStockZeroConsumer {
    @Value("${spring.rabbitmq.topic.activity_sku_stock_zero}")
    private String topic;

    @Resource
    private IRaffleActivitySjuStockService skuStock;

    @RabbitListener(queuesToDeclare = @Queue(value="activity_sku_stock_zero"))
    public void listener(String message){
        try {
            log.info("监听活动sku库存消耗为0消息 topic: {} message: {}", topic, message);
//        转换对象
            BaseEvent.EventMessage<Long> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<Long>>() {
            }.getType());
            Long sku = eventMessage.getData();
//        更新库存
            // 更新库存
            skuStock.clearActivitySkuStock(sku);
            // 清空队列 「此时就不需要延迟更新数据库记录了」
            skuStock.clearQueueValue();
        } catch (Exception e) {
            log.error("监听活动sku库存消耗为0消息，消费失败 topic: {} message: {}", topic, message);
            throw e;

        }


    }

}
