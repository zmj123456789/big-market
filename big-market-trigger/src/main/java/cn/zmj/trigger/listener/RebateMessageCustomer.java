package cn.zmj.trigger.listener;

import cn.zmj.domain.activity.model.entity.SkuRechargeEntity;
import cn.zmj.domain.activity.service.IRaffleActivityAccountQuotaService;
import cn.zmj.domain.rebate.event.SendRebateMessageEvent;
import cn.zmj.domain.rebate.model.valobj.RebateTypeVO;
import cn.zmj.types.enums.ResponseCode;
import cn.zmj.types.event.BaseEvent;
import cn.zmj.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 监听行为返利消息
 */
@Slf4j
@Component
public class RebateMessageCustomer {
    @Value("${spring.rabbitmq.topic.send_rebate}")
    private String topic;

    @Resource
    private IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;
    @RabbitListener(queuesToDeclare = @Queue(value =  "${spring.rabbitmq.topic.send_rebate}"))
    public void listener(String message){
        try {
            log.info("监听用户行为返利消息topic:{}message:{}",topic,message);
//        转换消息
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> eventMessage=JSON.parseObject(message,new TypeReference<BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage>>(){}.getType());
            SendRebateMessageEvent.RebateMessage rebateMessage = eventMessage.getData();
            if(!RebateTypeVO.SKU.getCode().equals(rebateMessage.getRebateType())){
                log.info("监听用户行为消息-非sku奖励暂时不处理topic{},message{}",topic,message);
                return;
            }
//        入账奖励
            SkuRechargeEntity skuRechargeEntity=new SkuRechargeEntity();
            skuRechargeEntity.setUserId(rebateMessage.getUserId());
            skuRechargeEntity.setSku(Long.valueOf(rebateMessage.getRebateConfig()));
            skuRechargeEntity.setOutBusinessNo(rebateMessage.getBizId());
            raffleActivityAccountQuotaService.createSkuRechargeOrder(skuRechargeEntity);
        } catch (AppException e) {
            if(ResponseCode.INDEX_DUP.getCode().equals(e.getCode())){
                log.warn("监听用户行为返利消息，消费重复topic{}message{}",topic,message,e);
                return;
            }
        } catch (Exception e) {
            log.error("监听用户行为返利消息，消费失败 topic: {} message: {}", topic, message, e);
            throw e;
        }
    }
}
