package cn.zmj.test.domain;

import cn.zmj.domain.strategy.model.valobj.RuleTreeVO;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyRepositoryTest {
    @Resource
    private IStrategyRepository strategyRepository;
    @Test
    public void queryRuleTreeVOByTreeId(){
        RuleTreeVO ruleTreeVO = strategyRepository.queryRuleTreeVOByTreeId("tree_lock");
        log.info("测试结果{}", JSON.toJSONString(ruleTreeVO));
    }
}
