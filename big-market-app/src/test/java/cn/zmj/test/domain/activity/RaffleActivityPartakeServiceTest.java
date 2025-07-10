package cn.zmj.test.domain.activity;

import cn.zmj.domain.activity.model.entity.PartakeRaffleActivityEntity;
import cn.zmj.domain.activity.model.entity.UserRaffleOrderEntity;
import cn.zmj.domain.activity.service.IRaffleActivityPartakeService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动订单单测
 * @create 2024-03-16 11:51
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleActivityPartakeServiceTest {
    @Resource
    private IRaffleActivityPartakeService raffleActivityPartakeService;
    @Test
    public void test_createOrder(){
        PartakeRaffleActivityEntity partakeRaffleActivityEntity=new PartakeRaffleActivityEntity();
        partakeRaffleActivityEntity.setActivityId(100301L);
        partakeRaffleActivityEntity.setUserId("xiaofuge");
        UserRaffleOrderEntity order = raffleActivityPartakeService.createOrder(partakeRaffleActivityEntity);
        log.info("请求参数：{}", JSON.toJSONString(partakeRaffleActivityEntity));
        log.info("测试结果：{}", JSON.toJSONString(order));

    }
}
