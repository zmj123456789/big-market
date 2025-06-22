package cn.zmj.infrastructure.persistent.dao;

import cn.zmj.infrastructure.persistent.po.StrategyAward;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IStrategyAwardDao {
    List<StrategyAward> queryStrategyAward();

    List<StrategyAward> queryStrategyAwardListByStrategyId(Long strategyId);

    String queryStrategyAwardRuleModels(StrategyAward strategyAward);
}
