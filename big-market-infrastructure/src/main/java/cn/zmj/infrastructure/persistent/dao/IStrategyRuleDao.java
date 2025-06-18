package cn.zmj.infrastructure.persistent.dao;

import cn.zmj.infrastructure.persistent.po.StrategyRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IStrategyRuleDao {
    List<StrategyRule> queryStrategyRuleList();
    StrategyRule queryStrategyRule(StrategyRule strategyRule);
}
