package cn.zmj.domain.strategy.service.raffle;

import cn.zmj.domain.strategy.model.entity.RaffleAwardEntity;
import cn.zmj.domain.strategy.model.entity.RaffleFactorEntity;
import cn.zmj.domain.strategy.model.entity.RuleActionEntity;
import cn.zmj.domain.strategy.model.entity.StrategyEntity;
import cn.zmj.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.domain.strategy.service.IRaffleStrategy;
import cn.zmj.domain.strategy.service.armory.IStrategyDispatch;
import cn.zmj.domain.strategy.service.rule.factory.DefaultLogicFactory;
import cn.zmj.types.enums.ResponseCode;
import cn.zmj.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖策略抽象类，定义抽奖的标准流程
 * @create 2024-01-06 09:26
 */

public abstract class AbstractRaffleStrategy implements IRaffleStrategy {
    protected IStrategyRepository repository;
    protected IStrategyDispatch strategyDispatch;

    public AbstractRaffleStrategy(IStrategyDispatch strategyDispatch, IStrategyRepository repository) {
        this.strategyDispatch = strategyDispatch;
        this.repository = repository;
    }
    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactoryEntity){
        //参数校验
        String userId=raffleFactoryEntity.getUserId();
        Long strategyId = raffleFactoryEntity.getStrategyId();
        if (null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        //策略查询
        StrategyEntity strategy = repository.queryStrategyByStrategyId(strategyId);
        //抽奖前,规则过滤
        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionBeforeEntity = this.doCheckRaffleBeforeLogic(RaffleFactorEntity.builder().userId(userId).strategyId(strategyId).build(), strategy.ruleModels());
        if(RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(ruleActionBeforeEntity.getCode())){
            if(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode().equals(ruleActionBeforeEntity.getRuleModel())){
                //黑名单返回固定的奖品id
                return RaffleAwardEntity.builder().awardId(ruleActionBeforeEntity.getData().getAwardId()).build();
            }else if(DefaultLogicFactory.LogicModel.RULE_WIGHT.getCode().equals(ruleActionBeforeEntity.getRuleModel())){
                // 权重根据返回的信息进行抽奖
                RuleActionEntity.RaffleBeforeEntity raffleBeforeEntity=ruleActionBeforeEntity.getData();
                String ruleWeightValueKey = raffleBeforeEntity.getRuleWeightValueKey();
                Integer randomAwardId = strategyDispatch.getRandomAwardId(strategyId, ruleWeightValueKey);
                return RaffleAwardEntity.builder().awardId(randomAwardId).build();


            }
        }
        //默认抽奖流程
        Integer awardId=strategyDispatch.getRandomAwardId(strategyId);
        //查询奖品规则「抽奖中（拿到奖品ID时，过滤规则）、抽奖后（扣减完奖品库存后过滤，抽奖中拦截和无库存则走兜底）」
        repository.queryStrategyAwardRuleModelVO(strategyId,awardId);
        return RaffleAwardEntity.builder().awardId(awardId).build();
    }
    protected  abstract RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity,String... logics);
}
