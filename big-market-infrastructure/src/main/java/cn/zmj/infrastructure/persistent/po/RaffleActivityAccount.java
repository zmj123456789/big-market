package cn.zmj.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;
/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动账户表 持久化对象
 * @create 2024-03-02 13:15
 */

@Data
public class RaffleActivityAccount {
    private Long id;
    private Long userId;
    private Long activityId;
    private Integer totalCount;
    private Integer totalCountSurplus;
    private Integer dayCount;
    private Integer dayCountSurplus;
    private Integer monthCount;
    private Integer monthCountSurplus;
    private Date createTime;
    private Date updateTime;
}
