package cn.zmj.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.zmj.infrastructure.persistent.po.RaffleActivityAccountMonth;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 29622
* @description 针对表【raffle_activity_account_month(抽奖活动账户表-月次数)】的数据库操作Mapper
* @createDate 2025-07-09 10:34:50
* @Entity cn.zmj.infrastructure.persistent.po.RaffleActivityAccountMonth
*/
@Mapper
public interface IRaffleActivityAccountMonthDao {
    @DBRouter
    RaffleActivityAccountMonth queryActivityAccountMonthByUserId(RaffleActivityAccountMonth raffleActivityAccountMonthReq);

    int updateActivityAccountMonthSubstractionQuota(RaffleActivityAccountMonth raffleActivityAccountMonth);
    void insertActivityAccountMonth(RaffleActivityAccountMonth raffleActivityAccountMonth);

    void addAccountQuota(RaffleActivityAccountMonth raffleActivityAccountMonth);
}




