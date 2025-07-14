package cn.zmj.domain.strategy.service;

import cn.zmj.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;

public interface IRaffleAward {
    /**
     * 根据策略ID查询抽奖奖品列表配置
     *
     * @param strategyId 策略ID
     * @return 奖品列表
     */
    List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId);
    /**
     * 根据活动ID查询抽奖奖品列表配置
     *
     * @param activityId 策略ID
     * @return 奖品列表
     */

    List<StrategyAwardEntity> queryRaffleStrategyAwardListByActivityId(Long activityId);
}
