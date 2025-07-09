package cn.zmj.test.domain.activity;

import cn.zmj.domain.activity.model.entity.ActivityOrderEntity;
import cn.zmj.domain.activity.model.entity.SkuRechargeEntity;
import cn.zmj.domain.activity.service.IRaffleOrder;
import cn.zmj.domain.activity.service.armory.IActivityArmory;
import cn.zmj.types.exception.AppException;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class RaffleOrderTest {
    @Resource
    private IRaffleOrder raffleOrder;
    @Resource
    private IActivityArmory activityArmory;
    @Before
    public void setUp(){
        log.info("装配活动{}",activityArmory.assembleActivitySku(9011L));
    }
    @Test
    public void test_createRaffleActivityOrder(){
        SkuRechargeEntity skuRechargeEntity = new SkuRechargeEntity();
        skuRechargeEntity.setUserId("zmj");
        skuRechargeEntity.setSku(9011L);
        skuRechargeEntity.setOutBusinessNo("70091009120");
        String skuRechargeOrder = raffleOrder.createSkuRechargeOrder(skuRechargeEntity);
        log.info("测试结果{}",skuRechargeOrder);
    }
    @Test
    public void test_createSkuRechargeOrder() throws InterruptedException {
        try {
            for(int i=0;i<20;i++){
                SkuRechargeEntity skuRechargeEntity = new SkuRechargeEntity();
                skuRechargeEntity.setUserId("zmj");
                skuRechargeEntity.setSku(9011L);
                skuRechargeEntity.setOutBusinessNo(RandomStringUtils.random(12));
                String skuRechargeOrder = raffleOrder.createSkuRechargeOrder(skuRechargeEntity);
                log.info("测试结果{}",skuRechargeOrder);
            }
        } catch (AppException e) {
            log.warn(e.getInfo());
        }
        new CountDownLatch(1).await();
    }
}
