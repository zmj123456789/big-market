package cn.zmj.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;
/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动单 持久化对象
 * @create 2024-03-02 13:21
 */

@Data
public class RaffleActivityOrder {
    private Long id;
    private String userId;
    private Long activityId;
    private String activityName;
    private Long strategyId;
    private String orderId;
    private Date orderTime;
    private Integer totalCount;

    /**
     * 日次数
     */
    private Integer dayCount;

    /**
     * 月次数
     */
    private Integer monthCount;
    private String state;
    private Date createTime;
    private Date updateTime;
}
