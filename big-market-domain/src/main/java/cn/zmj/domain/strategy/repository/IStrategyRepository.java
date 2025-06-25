package cn.zmj.domain.strategy.repository;

import cn.zmj.domain.strategy.model.entity.StrategyAwardEntity;
import cn.zmj.domain.strategy.model.entity.StrategyEntity;
import cn.zmj.domain.strategy.model.entity.StrategyRuleEntity;
import cn.zmj.domain.strategy.model.valobj.RuleTreeVO;
import cn.zmj.domain.strategy.model.valobj.StrategyAwardRuleModelVO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface IStrategyRepository {
    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    void storeStrategyAwardSearchRateTables(String key, BigDecimal rateRange, HashMap<Integer, Integer> shuffleStrategyAwardSearchRateTables);

    int getRateRange(Long strategyId);
    int getRateRange(String strategyId);

    Integer getStrategyAwardAssemble(String strategyId, int rateKey);

    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleWeight);

    String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel);
    String queryStrategyRuleValue(Long strategyId, String ruleModel);

    StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId);
    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);
}
