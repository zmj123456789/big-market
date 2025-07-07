package cn.zmj.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;
/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动次数配置表 持久化对象
 * @create 2024-03-02 13:13
 */
@Data
public class RaffleActivityCount {
    private Long id;
    private Long activityCountId;
    private Integer totalCount;
    private Integer dayCount;
    private Integer monthCount;
    private Date createTime;
    private Date updateTime;
}
