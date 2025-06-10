package cn.zmj.infrastructure.persistent.dao;

import cn.zmj.infrastructure.persistent.po.Award;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IAwardDao {
    List<Award> queryAward();
}
