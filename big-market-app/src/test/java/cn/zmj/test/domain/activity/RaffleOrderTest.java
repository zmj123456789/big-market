package cn.zmj.test.domain.activity;

import cn.zmj.domain.activity.model.entity.ActivityOrderEntity;
import cn.zmj.domain.activity.model.entity.SkuRechargeEntity;
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
        SkuRechargeEntity skuRechargeEntity = new SkuRechargeEntity();
        skuRechargeEntity.setUserId("zmj");
        skuRechargeEntity.setSku(9011L);
        skuRechargeEntity.setOutBusinessNo("70091009120");
        String skuRechargeOrder = raffleOrder.createSkuRechargeOrder(skuRechargeEntity);
        log.info("测试结果{}",skuRechargeOrder);
    }
}
