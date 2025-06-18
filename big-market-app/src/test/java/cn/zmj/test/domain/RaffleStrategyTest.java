package cn.zmj.test.domain;

import cn.zmj.domain.strategy.model.entity.RaffleAwardEntity;
import cn.zmj.domain.strategy.model.entity.RaffleFactorEntity;
import cn.zmj.domain.strategy.service.IRaffleStrategy;
import cn.zmj.domain.strategy.service.rule.impl.RuleWeightLogicFilter;
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
public class RaffleStrategyTest {
    @Resource
    private IRaffleStrategy raffleStrategy;
    @Resource
    private RuleWeightLogicFilter ruleWeightLogicFilter;
    @Test
    public void test_performRaffle(){
        RaffleFactorEntity raffleFactorEntity=RaffleFactorEntity.builder().userId("xiaofuge").strategyId(100001L).build();
        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);
        log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
        log.info("请求结果：{}",JSON.toJSONString(raffleAwardEntity));

    }
    @Test
    public void test_performRaffle_blacklist(){
        RaffleFactorEntity raffleFactorEntity=RaffleFactorEntity.builder().userId("user003").strategyId(100001L).build();
        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);
        log.info("请求参数{}",raffleFactorEntity);
        log.info("请求结果{}",raffleAwardEntity);

    }
}
