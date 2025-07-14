package cn.zmj.domain.strategy.service;

import cn.zmj.domain.strategy.model.entity.RaffleAwardEntity;
import cn.zmj.domain.strategy.model.entity.RaffleFactorEntity;
import cn.zmj.domain.strategy.model.entity.StrategyAwardEntity;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.domain.strategy.service.armory.IStrategyDispatch;
import cn.zmj.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.zmj.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import cn.zmj.types.enums.ResponseCode;
import cn.zmj.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖策略抽象类，定义抽奖的标准流程
 * @create 2024-01-06 09:26
 */
@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy{
    protected IStrategyRepository repository;
    protected IStrategyDispatch strategyDispatch;
    protected final DefaultChainFactory defaultChainFactory;
    protected final DefaultTreeFactory defaultTreeFactory;
    public AbstractRaffleStrategy(IStrategyDispatch strategyDispatch, IStrategyRepository repository,DefaultChainFactory defaultChainFactory,DefaultTreeFactory defaultTreeFactory) {
        this.strategyDispatch = strategyDispatch;
        this.repository = repository;
        this.defaultChainFactory=defaultChainFactory;
        this.defaultTreeFactory=defaultTreeFactory;
    }
    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactoryEntity){
        //参数校验
        String userId=raffleFactoryEntity.getUserId();
        Long strategyId = raffleFactoryEntity.getStrategyId();
        if (null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode()  , ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
//        责任链抽奖
        DefaultChainFactory.StrategyAwardVO chainStrategyAwardVO=raffleLogicChain(userId,strategyId);
        log.info("抽奖策略计算-责任链 {} {} {} {}", userId, strategyId, chainStrategyAwardVO.getAwardId(), chainStrategyAwardVO.getLogicModel());
        if(!DefaultChainFactory.LogicModel.RULE_DEFAULT.getCode().equals(chainStrategyAwardVO.getLogicModel())){
            return buildRaffleAwardEntity(strategyId,chainStrategyAwardVO.getAwardId(),null);
        }
        // 3. 规则树抽奖过滤【奖品ID，会根据抽奖次数判断、库存判断、兜底兜里返回最终的可获得奖品信息】
        DefaultTreeFactory.StrategyAwardVO treeStrategyAwardVO=raffleLogicTree(userId,strategyId,chainStrategyAwardVO.getAwardId(),raffleFactoryEntity.getEndTime());
        log.info("抽奖策略计算-规则树 {} {} {} {}", userId, strategyId, treeStrategyAwardVO.getAwardId(), treeStrategyAwardVO.getAwardRuleValue());

        return buildRaffleAwardEntity(strategyId,treeStrategyAwardVO.getAwardId(),treeStrategyAwardVO.getAwardRuleValue());
    }
    public abstract DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId,Long strategyId);
    public abstract DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId,Long strategyId,Integer awardId);
    /**
     * 抽奖结果过滤，决策树抽象方法
     *
     * @param userId      用户ID
     * @param strategyId  策略ID
     * @param awardId     奖品ID
     * @param endDateTime 活动结束时间 - 用于设定缓存有效期
     * @return 过滤结果【奖品ID，会根据抽奖次数判断、库存判断、兜底兜里返回最终的可获得奖品信息】
     */

    public abstract DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId, Date endDateTime);

    private RaffleAwardEntity buildRaffleAwardEntity(Long strategyId,Integer awardId,String awardConfig){
        StrategyAwardEntity strategyAward = repository.queryStrategyAwardEntity(strategyId, awardId);
        return RaffleAwardEntity.builder().awardId(awardId).awardTitle(strategyAward.getAwardTitle()).awardConfig(awardConfig).sort(strategyAward.getSort()).build();
    }
}
