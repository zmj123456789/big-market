package cn.zmj.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardEntity {
    /**抽奖策略id*/
    private Long strategy_id;
    /**抽奖奖品id*/
    private Integer awardId;
    /**奖品库存数量*/
    private Integer awardCount;
    /**抽奖库存剩余*/
    private Integer awardCountSurplus;
    /**抽奖中奖概率*/
    private BigDecimal awardRate;
}
