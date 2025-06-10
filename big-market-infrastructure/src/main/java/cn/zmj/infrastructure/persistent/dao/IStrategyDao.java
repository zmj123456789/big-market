package cn.zmj.infrastructure.persistent.dao;

import cn.zmj.infrastructure.persistent.po.Strategy;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IStrategyDao {
    List<Strategy> queryStrategy();
}
