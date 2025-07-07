package cn.zmj.test.infrastructure;

import cn.zmj.infrastructure.persistent.dao.IRaffleActivityOrderDao;
import cn.zmj.infrastructure.persistent.po.RaffleActivityOrder;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleActivityOrderTest {
    @Resource
    private IRaffleActivityOrderDao raffleActivityOrderDao;
    @Test
    public void TestRaffleActivityOrder(){
        RaffleActivityOrder raffleActivityOrder = new RaffleActivityOrder();
        raffleActivityOrder.setUserId("zmj");
        raffleActivityOrder.setActivityId(100301L);
        raffleActivityOrder.setActivityName("测试活动2");
        raffleActivityOrder.setStrategyId(100006L);
        raffleActivityOrder.setOrderId(RandomStringUtils.randomNumeric(12));
        raffleActivityOrder.setOrderTime(new Date());
        raffleActivityOrder.setState("not_used");
        raffleActivityOrderDao.insert(raffleActivityOrder);

    }
    @Test
    public void TestQueryRaffleActivityOrder(){
        List<RaffleActivityOrder> raffleActivityOrders = raffleActivityOrderDao.queryRaffleActivityOrderByUserId("zmj");
        log.info(JSON.toJSONString(raffleActivityOrders));
    }
}
