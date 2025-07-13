package cn.zmj.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import cn.zmj.infrastructure.persistent.po.UserRaffleOrder;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 29622
* @description 针对表【user_raffle_order_000(用户抽奖订单表)】的数据库操作Mapper
* @createDate 2025-07-09 10:34:50
* @Entity cn.zmj.infrastructure.persistent.po.UserRaffleOrder000
*/
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserRaffleOrderDao {
    @DBRouter
    UserRaffleOrder queryNoUsedRaffleOrder(UserRaffleOrder userRaffleOrder);
    void insert(UserRaffleOrder userRaffleOrder);
    int updateUserRaffleOrderStateUsed(UserRaffleOrder userRaffleOrderReq);
}




