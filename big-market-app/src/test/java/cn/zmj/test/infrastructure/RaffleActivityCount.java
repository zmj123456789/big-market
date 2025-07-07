package cn.zmj.test.infrastructure;

import cn.zmj.infrastructure.persistent.dao.IRaffleActivityCountDao;
import cn.zmj.infrastructure.persistent.dao.IRaffleActivityDao;
import cn.zmj.infrastructure.persistent.po.RaffleActivity;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class RaffleActivityCount {
    @Resource
    private IRaffleActivityCountDao raffleActivityCountDao;
    @Test
    public void TestRaffleActivityCount(){
        cn.zmj.infrastructure.persistent.po.RaffleActivityCount raffleActivityCount = raffleActivityCountDao.queryRaffleActivityCountByActivityCountId(11101L);
        log.info(JSON.toJSONString(raffleActivityCount));
    }
}
