package cn.zmj.test.domain.rebate;

import cn.zmj.domain.rebate.model.entity.BehaviorEntity;
import cn.zmj.domain.rebate.model.valobj.BehaviorTypeVO;
import cn.zmj.domain.rebate.repository.IBehaviorRebateRepository;
import cn.zmj.domain.rebate.service.IBehaviorRebateService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 行为返利单测
 * @create 2024-04-30 17:53
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class BehaviorRebateServiceTest {
    @Resource
    private IBehaviorRebateService behaviorRebateService;


    @Test
    public void test_createOrder(){
        BehaviorEntity behaviorEntity=new BehaviorEntity();
        behaviorEntity.setUserId("xiaofuge");
        behaviorEntity.setBehaviorTypeVO(BehaviorTypeVO.SIGN);
        behaviorEntity.setOutBusinessNo("20240715");
        List<String> orderIds = behaviorRebateService.createOrder(behaviorEntity);
        log.info("请求参数：{}", JSON.toJSONString(behaviorEntity));
        log.info("测试结果：{}", JSON.toJSONString(orderIds));
    }
}
