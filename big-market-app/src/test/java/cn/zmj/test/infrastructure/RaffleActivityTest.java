package cn.zmj.test.infrastructure;

import cn.zmj.infrastructure.persistent.dao.IRaffleActivitySkuDao;
import cn.zmj.infrastructure.persistent.dao.IRaffleActivityDao;
import cn.zmj.infrastructure.persistent.po.RaffleActivity;
import cn.zmj.infrastructure.persistent.po.RaffleActivitySku;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleActivityTest {
    @Resource
    private IRaffleActivityDao raffleActivityDao;
    @Resource
    private IRaffleActivitySkuDao raffleActivityAccountSkuDao;
    @Test
    public void testQueryRaffleActivity(){
        RaffleActivity raffleActivity = raffleActivityDao.queryRaffleActivityByActivityId(100301L);
        log.info(JSON.toJSONString(raffleActivity));
    }
    @Test
    public void testQueryRaffleActivitySku(){
        RaffleActivitySku raffleActivitySku = raffleActivityAccountSkuDao.queryActivitySku(9011L);
        log.info(JSON.toJSONString(raffleActivitySku));
    }
}
