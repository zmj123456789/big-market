package cn.zmj.domain.strategy.service.rule.filter.impl;

import cn.zmj.domain.strategy.model.entity.RuleActionEntity;
import cn.zmj.domain.strategy.model.entity.RuleMatterEntity;
import cn.zmj.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.domain.strategy.service.annotation.LogicStrategy;
import cn.zmj.domain.strategy.service.rule.filter.ILogicFilter;
import cn.zmj.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_LOCK)
public class RuleLockLogicFilter implements ILogicFilter<RuleActionEntity.RaffleCenterEntity> {
    @Resource
    private IStrategyRepository repository;
    private Long userRaffleCount=0L;
    @Override
    public RuleActionEntity<RuleActionEntity.RaffleCenterEntity> filter(RuleMatterEntity ruleMatterEntity) {
        log.info("规则过滤-次数锁 userId:{} strategyId:{} ruleModel:{}", ruleMatterEntity.getUserId(), ruleMatterEntity.getStrategyId(), ruleMatterEntity.getRuleModel());
        // 查询规则值配置；当前奖品ID，抽奖中规则对应的校验值。如；1、2、6
        String ruleValue = repository.queryStrategyRuleValue(ruleMatterEntity.getStrategyId(), ruleMatterEntity.getAwardId(), ruleMatterEntity.getRuleModel());
        long raffleCount=Long.parseLong(ruleValue);
        // 用户抽奖次数大于规则限定值，规则放行
        if(userRaffleCount>raffleCount){
            return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder().code(RuleLogicCheckTypeVO.ALLOW.getCode()).info(RuleLogicCheckTypeVO.ALLOW.getInfo()).build();

        }
        return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder().code(RuleLogicCheckTypeVO.TAKE_OVER.getCode()).info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo()).build();
    }
}
