package cn.zmj.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import cn.zmj.infrastructure.persistent.po.UserAwardRecord;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 29622
* @description 针对表【user_award_record_000(用户中奖记录表)】的数据库操作Mapper
* @createDate 2025-07-09 10:34:50
* @Entity cn.zmj.infrastructure.persistent.po.UserAwardRecord000
*/
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserAwardRecordDao {

    void insert(UserAwardRecord userAwardRecord);


}




