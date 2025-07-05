package cn.zmj.domain.strategy.service.rule.tree.impl;

import cn.zmj.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import cn.zmj.domain.strategy.model.valobj.StrategyAwardStockKeyVO;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.domain.strategy.service.armory.IStrategyDispatch;
import cn.zmj.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.zmj.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component("rule_stock")
public class RuleStockLogicTreeNode implements ILogicTreeNode {
    @Resource
    private IStrategyDispatch strategyDispatch;
    @Resource
    private IStrategyRepository strategyRepository;
    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId,String ruleValue) {
        log.info("规则过滤-库存扣减 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
        Boolean status = strategyDispatch.subtractionAwardStock(strategyId, awardId);
        if(status){
            log.info("规则过滤-库存扣减-成功 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
            strategyRepository.awardStockConsumeSendQueue(StrategyAwardStockKeyVO.builder().strategyId(strategyId).awardId(awardId).build());
            return DefaultTreeFactory.TreeActionEntity.builder().ruleLogicCheckType(RuleLogicCheckTypeVO.TAKE_OVER).strategyAwardVO(DefaultTreeFactory.StrategyAwardVO.builder().awardId(awardId).awardRuleValue(ruleValue).build()).build();
        }
//        库存不足，返回放行
        return DefaultTreeFactory.TreeActionEntity.builder().ruleLogicCheckType(RuleLogicCheckTypeVO.TAKE_OVER).build();
    }
}
