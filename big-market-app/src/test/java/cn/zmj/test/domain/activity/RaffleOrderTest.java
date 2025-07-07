package cn.zmj.test.domain.activity;

import cn.zmj.domain.activity.model.entity.ActivityOrderEntity;
import cn.zmj.domain.activity.model.entity.ActivityShopCartEntity;
import cn.zmj.domain.activity.service.IRaffleOrder;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class RaffleOrderTest {
    @Resource
    private IRaffleOrder raffleOrder;
    @Test
    public void test_createRaffleActivityOrder(){
        ActivityShopCartEntity activityShopCartEntity=new ActivityShopCartEntity();
        activityShopCartEntity.setUserId("zmj");
        activityShopCartEntity.setSku(9011L);
        ActivityOrderEntity raffleActivityOrder = raffleOrder.createRaffleActivityOrder(activityShopCartEntity);
        log.info(JSON.toJSONString(raffleActivityOrder));
    }
}
