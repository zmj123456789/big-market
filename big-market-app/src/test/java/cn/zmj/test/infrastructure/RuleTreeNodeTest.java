package cn.zmj.test.infrastructure;

import cn.zmj.infrastructure.persistent.dao.IRuleTreeNodeDao;
import cn.zmj.infrastructure.persistent.po.RuleTreeNode;
import com.alibaba.fastjson.JSON;
import com.rabbitmq.tools.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.lang.annotation.Repeatable;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RuleTreeNodeTest {
    @Resource
    private IRuleTreeNodeDao ruleTreeNodeDao;
    @Test
    public void test_queryRuleLocks(){
        List<RuleTreeNode> ruleTreeNodes = ruleTreeNodeDao.queryRuleLocks(new String[]{"tree_lock_1", "tree_lock_2"});
        log.info(JSON.toJSONString(ruleTreeNodes));
    }
}
