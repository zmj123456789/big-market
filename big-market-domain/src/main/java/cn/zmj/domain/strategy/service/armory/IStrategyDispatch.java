package cn.zmj.domain.strategy.service.armory;

public interface IStrategyDispatch {
    Integer getRandomAwardId(Long strategyId);
    Integer getRandomAwardId(Long strategyId,String ruleWeightValue);
    /**
     * 获取抽奖策略装配的随机结果
     *
     * @param key = strategyId + _ + ruleWeightValue；
     * @return 抽奖结果
     */
    Integer getRandomAwardId(String key);

}
