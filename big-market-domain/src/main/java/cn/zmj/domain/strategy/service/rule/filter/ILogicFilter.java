package cn.zmj.domain.strategy.service.rule.filter;

import cn.zmj.domain.strategy.model.entity.RuleActionEntity;
import cn.zmj.domain.strategy.model.entity.RuleMatterEntity;

public interface ILogicFilter<T extends RuleActionEntity.RaffleEntity>{
    RuleActionEntity<T> filter(RuleMatterEntity ruleMatterEntity);
}
