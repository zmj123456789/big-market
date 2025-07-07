package cn.zmj.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;
/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动表 持久化对象
 * @create 2024-03-02 13:06
 */

@Data
public class RaffleActivity {
    private Long id;
    private Long activityId;
    private String activityName;
    private String activityDesc;
    private Date beginDateTime;
    private Date endDateTime;
    private Long strategyId;
    private String state;
    private Date createTime;
    private Date updateTime;
}
