package cn.zmj.domain.strategy.service.rule.chain.factory;

import cn.zmj.domain.strategy.model.entity.StrategyEntity;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.domain.strategy.service.rule.chain.ILogicChain;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DefaultChainFactory{
    public final Map<String, ILogicChain> logicChainGroup;
    protected IStrategyRepository repository;

    public DefaultChainFactory(Map<String, ILogicChain> logicChainGroup, IStrategyRepository repository) {
        this.logicChainGroup = logicChainGroup;
        this.repository = repository;
    }
    public ILogicChain openLogicChain(Long strategyId){
        StrategyEntity strategyEntity = repository.queryStrategyEntityByStrategyId(strategyId);
        String[] ruleModels = strategyEntity.ruleModels();
        if(null==ruleModels||0==ruleModels.length)return logicChainGroup.get(LogicModel.RULE_DEFAULT.getCode());
        ILogicChain logicChain = logicChainGroup.get(ruleModels[0]);
        ILogicChain current=logicChain;
        for(int i=1;i<ruleModels.length;i++){
            ILogicChain nextChain= logicChainGroup.get(ruleModels[i]);
            current = current.appendNext(nextChain);
        }
        current.appendNext(logicChainGroup.get(LogicModel.RULE_DEFAULT.getCode()));
        return logicChain;



    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyAwardVO{
        /** 抽奖奖品ID - 内部流转使用 */
        private Integer awardId;
        private String logicModel;
    }

    @Getter
    @AllArgsConstructor
    public enum LogicModel{
        RULE_DEFAULT("rule_default", "默认抽奖"),
        RULE_BLACKLIST("rule_blacklist", "黑名单抽奖"),
        RULE_WEIGHT("rule_weight", "权重规则"),
        ;
        private final String code;
        private final String info;

    }
}
