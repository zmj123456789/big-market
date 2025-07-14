package cn.zmj.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.zmj.infrastructure.persistent.po.RaffleActivityAccountDay;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 29622
* @description 针对表【raffle_activity_account_day(抽奖活动账户表-日次数)】的数据库操作Mapper
* @createDate 2025-07-09 10:34:50
* @Entity cn.zmj.infrastructure.persistent.po.RaffleActivityAccountDay
*/
@Mapper
public interface IRaffleActivityAccountDayDao {
    @DBRouter
    RaffleActivityAccountDay queryActivityAccountDayByUserId(RaffleActivityAccountDay raffleActivityAccountDayReq);

    int updateActivityAccountDaySubtractionQuota(RaffleActivityAccountDay build);

    void insertActivityAccountDay(RaffleActivityAccountDay build);
    @DBRouter
    Integer queryRaffleActivityAccountDayPartakeCount(RaffleActivityAccountDay raffleActivityAccountDay);
}




