package cn.zmj.domain.task.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务表，发送MQ
 * @TableName task
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskEntity {
    /** 用户ID */
    private String userId;
    /**
     * 消息主题
     */
    private String topic;

    /**
     * 消息主体
     */
    private String message;

    /**
     * 消息编号
     */
    private String messageId;
}
