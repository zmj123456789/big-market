package cn.zmj.domain.strategy.service;

import cn.zmj.domain.strategy.model.valobj.RuleWeightVO;

import java.util.List;
import java.util.Map;

/**
 * 抽奖规则接口提供对规则的业务功能查询
 */
public interface IRaffleRule {
    /**
     * 根据规则ID集合查询奖品中加锁数量的配置【部分奖品需要抽奖N次解锁】
     */
    Map<String,Integer> queryAwardRuleLockCount(String[] treeIds);

    List<RuleWeightVO> queryAwardRuleWeightByActivityId(Long activityId);
    List<RuleWeightVO> queryAwardRuleWeight(Long strategyId);
}
