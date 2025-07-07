package cn.zmj.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;
/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动账户流水表 持久化对象
 * @create 2024-03-02 13:20
 */

@Data
public class RaffleActivityAccountFlow {
    private Integer id;
    private String userId;
    private Long activityId;
    private Integer dayCount;
    private Integer totalCount;
    private Integer monthCount;
    private String flowId;
    private String flowChannel;
    private String bizId;
    private Date createTime;
    private Date updateTime;
}
