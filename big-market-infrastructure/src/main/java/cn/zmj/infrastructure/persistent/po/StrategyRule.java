package cn.zmj.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;
@Data
public class StrategyRule {
    /**自增id*/
    private Long id;
    /**抽奖策略id*/
    private Long strategyId;
    /**抽奖奖品id*/
    private Integer awardId;
    /**抽奖规则类型【1-策略规则2-奖品规则】*/
    private Integer ruleType;
    /**抽奖规则类型【rule_lock】*/
    private String ruleModel;
    /**抽奖规则比值*/
    private String ruleValue;
    /**抽奖规则描述*/
    private String ruleDesc;
    /**创建时间*/
    private Date createTime;
    /**修改时间*/
    private Date updateTime;
}
