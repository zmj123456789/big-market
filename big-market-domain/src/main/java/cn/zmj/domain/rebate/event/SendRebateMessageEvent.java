package cn.zmj.domain.rebate.event;

import cn.zmj.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 发送返利消息事件
 * @create 2024-04-30 15:34
 */
@Component
public class SendRebateMessageEvent extends BaseEvent<SendRebateMessageEvent.RebateMessage> {
    @Value("${spring.rabbitmq.topic.send_rebate}")
    private String topic;
    @Override
    public EventMessage<RebateMessage> buildEvent(RebateMessage data) {
        return EventMessage.<SendRebateMessageEvent.RebateMessage>builder()
                .id(RandomStringUtils.random(11))
                .timestamp(new Date())
                .data(data)
                .build();
    }

    @Override
    public String topic() {
        return topic;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RebateMessage{
        private String userId;
        private String rebateDesc;
        private String rebateType;
        private String rebateConfig;
        /** 业务ID - 唯一ID，确保幂等 */
        private String bizId;
    }
}
