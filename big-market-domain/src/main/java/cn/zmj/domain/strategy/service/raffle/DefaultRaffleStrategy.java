package cn.zmj.domain.strategy.service.raffle;

import cn.zmj.domain.strategy.model.entity.RaffleFactorEntity;
import cn.zmj.domain.strategy.model.entity.RuleActionEntity;
import cn.zmj.domain.strategy.model.entity.RuleMatterEntity;
import cn.zmj.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.domain.strategy.service.AbstractRaffleStrategy;
import cn.zmj.domain.strategy.service.armory.IStrategyDispatch;
import cn.zmj.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.zmj.domain.strategy.service.rule.filter.ILogicFilter;
import cn.zmj.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 默认的抽奖策略实现
 * @create 2024-01-06 11:46
 */

@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {
    @Resource
    private DefaultLogicFactory logicFactory;
    public DefaultRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory){
        super(strategyDispatch,repository,defaultChainFactory);
    }


    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity raffleFactorEntity, String... logics) {
        if (logics == null || 0 == logics.length) return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                .build();
        Map<String, ILogicFilter<RuleActionEntity.RaffleCenterEntity>> logicFilterGroup = logicFactory.openLogicFilter();
        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleActionEntity=null;
        for (String ruleModel : logics) {
            ILogicFilter<RuleActionEntity.RaffleCenterEntity> logicFilter = logicFilterGroup.get(ruleModel);
            RuleMatterEntity ruleMatterEntity=new RuleMatterEntity();
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setAwardId(raffleFactorEntity.getAwardId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleMatterEntity.setRuleModel(ruleModel);
            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            // 非放行结果则顺序过滤,给个参与奖
            log.info("抽奖中规则过滤 userId: {} ruleModel: {} code: {} info: {}", raffleFactorEntity.getUserId(), ruleModel, ruleActionEntity.getCode(), ruleActionEntity.getInfo());
            if(!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode()))return ruleActionEntity;
        }
        return ruleActionEntity;

    }
}
