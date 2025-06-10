package cn.zmj.test.infrastructure;

import cn.zmj.infrastructure.persistent.dao.IAwardDao;
import cn.zmj.infrastructure.persistent.po.Award;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AwardDaoTest {
    @Resource
    private IAwardDao iAwardDao;
    @Test
    public void test_queryAwardList(){
        List<Award> awards = iAwardDao.queryAward();
        log.info(JSON.toJSONString(awards));
    }
}
