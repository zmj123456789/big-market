package cn.zmj.infrastructure.persistent.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class StrategyAward {
    /**自增id*/
    private Long id;
    /**抽奖策略id*/
    private Long strategyId;
    /**抽奖奖品id*/
    private Integer awardId;
    /**抽奖奖品标题*/
    private String awardTitle;
    /**抽奖奖品副标题*/
    private String awardSubtitle;
    /**奖品库存数量*/
    private Integer awardCount;
    /**抽奖库存剩余*/
    private Integer awardCountSurplus;
    /**抽奖中奖概率*/
    private BigDecimal awardRate;
    /**排序*/
    private Integer sort;
    /**创建时间*/
    private Date createTime;
    /**更新时间*/
    private Date updateTime;
    /**规则模型，rule配置记录*/
    private String ruleModels;
}
