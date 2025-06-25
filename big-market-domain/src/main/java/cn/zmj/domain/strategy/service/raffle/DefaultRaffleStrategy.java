package cn.zmj.domain.strategy.service.raffle;

import cn.zmj.domain.strategy.model.valobj.RuleTreeVO;
import cn.zmj.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.domain.strategy.service.AbstractRaffleStrategy;
import cn.zmj.domain.strategy.service.armory.IStrategyDispatch;
import cn.zmj.domain.strategy.service.rule.chain.ILogicChain;
import cn.zmj.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.zmj.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import cn.zmj.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 默认的抽奖策略实现
 * @create 2024-01-06 11:46
 */

@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {
    public DefaultRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory){
        super(strategyDispatch,repository,defaultChainFactory,defaultTreeFactory);
    }

    @Override
    public DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId, Long strategyId) {
        ILogicChain logiChain=defaultChainFactory.openLogicChain(strategyId);
        return logiChain.logic(userId,strategyId);
    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId) {
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModelVO(strategyId, awardId);
        if(null==strategyAwardRuleModelVO){
            return DefaultTreeFactory.StrategyAwardVO.builder().awardId(awardId).build();
        }
        RuleTreeVO ruleTreeVO = repository.queryRuleTreeVOByTreeId(strategyAwardRuleModelVO.getRuleModels());
        if(null==ruleTreeVO){
            throw new RuntimeException("存在抽奖策略配置的规则模型 Key，未在库表 rule_tree、rule_tree_node、rule_tree_line 配置对应的规则树信息 " + strategyAwardRuleModelVO.getRuleModels());
        }
        IDecisionTreeEngine treeEngine = defaultTreeFactory.openLogicTree(ruleTreeVO);
        return treeEngine.process(userId,strategyId,awardId);
    }
}
