package cn.zmj.domain.strategy.service.rule.tree.factory;

import cn.zmj.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import cn.zmj.domain.strategy.model.valobj.RuleTreeVO;
import cn.zmj.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.zmj.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.zmj.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import cn.zmj.domain.strategy.service.rule.tree.factory.engine.impl.DecisionTreeEngine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
public class DefaultTreeFactory {
    private final Map<String, ILogicTreeNode> logicTreeNodeGroup;

    public DefaultTreeFactory(Map<String, ILogicTreeNode> logicTreeNodeGroup) {
        this.logicTreeNodeGroup = logicTreeNodeGroup;
    }
    public IDecisionTreeEngine openLogicTree(RuleTreeVO ruleTreeVO){
        return new DecisionTreeEngine(logicTreeNodeGroup,ruleTreeVO);
    }
    /**
     * 决策树个动作实习
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TreeActionEntity {
        private RuleLogicCheckTypeVO ruleLogicCheckType;
        private StrategyAwardVO strategyAwardVO;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyAwardVO {
        /** 抽奖奖品ID - 内部流转使用 */
        private Integer awardId;
        /** 抽奖奖品规则 */
        private String awardRuleValue;
    }

}
