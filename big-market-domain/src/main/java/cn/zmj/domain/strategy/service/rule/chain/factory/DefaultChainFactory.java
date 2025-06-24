package cn.zmj.domain.strategy.service.rule.chain.factory;

import ch.qos.logback.classic.spi.EventArgUtil;
import cn.zmj.domain.strategy.model.entity.StrategyEntity;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.domain.strategy.service.rule.chain.ILogiChain;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DefaultChainFactory{
    public final Map<String, ILogiChain> logicChainGroup;
    protected IStrategyRepository repository;

    public DefaultChainFactory(Map<String, ILogiChain> logicChainGroup, IStrategyRepository repository) {
        this.logicChainGroup = logicChainGroup;
        this.repository = repository;
    }
    public ILogiChain openLogicChain(Long strategyId){
        StrategyEntity strategyEntity = repository.queryStrategyEntityByStrategyId(strategyId);
        String[] ruleModels = strategyEntity.ruleModels();
        if(null==ruleModels||0==ruleModels.length)return logicChainGroup.get("default");
        ILogiChain logicChain = logicChainGroup.get(ruleModels[0]);
        ILogiChain current=logicChain;
        for(int i=1;i<ruleModels.length;i++){
            ILogiChain nextChain= logicChainGroup.get(ruleModels[i]);
            current = current.appendNext(nextChain);
        }
        current.appendNext(logicChainGroup.get("default"));
        return logicChain;



    }
}
