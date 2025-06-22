package cn.zmj.infrastructure.persistent.repository;

import cn.zmj.domain.strategy.model.entity.StrategyAwardEntity;
import cn.zmj.domain.strategy.model.entity.StrategyEntity;
import cn.zmj.domain.strategy.model.entity.StrategyRuleEntity;
import cn.zmj.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.infrastructure.persistent.dao.IStrategyAwardDao;
import cn.zmj.infrastructure.persistent.dao.IStrategyDao;
import cn.zmj.infrastructure.persistent.dao.IStrategyRuleDao;
import cn.zmj.infrastructure.persistent.po.Strategy;
import cn.zmj.infrastructure.persistent.po.StrategyAward;
import cn.zmj.infrastructure.persistent.po.StrategyRule;
import cn.zmj.infrastructure.persistent.redis.IRedisService;
import cn.zmj.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Repository
public class StrateRepository implements IStrategyRepository {
    @Resource
    private IStrategyAwardDao strategyAwardDao;
    @Resource
    private IRedisService redisService;
    @Resource
    private IStrategyDao strategyDao;
    @Resource
    private IStrategyRuleDao strategyRuleDao;
//    从缓存或数据库中查询指定策略的奖品列表，并将结果缓存到 Redis 中，以提高后续访问效率。
    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {
        String cacheKey= Constants.RedisKey.STRATEGY_AWARD_KEY+strategyId;
        List<StrategyAwardEntity> strategyAwardEntities=redisService.getValue(cacheKey);
        if(null!=strategyAwardEntities&&!strategyAwardEntities.isEmpty())return strategyAwardEntities;
        //从库中读取
        strategyAwardEntities = new ArrayList<>();
        List<StrategyAward> strategyAwards = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);
        log.info("strategyAwards"+strategyAwards.toString());
        for(StrategyAward strategyAward:strategyAwards){
            StrategyAwardEntity strategyAwardEntity = StrategyAwardEntity.builder()
                        .strategy_id(strategyAward.getStrategyId())
                        .awardId(strategyAward.getAwardId())
                        .awardCount(strategyAward.getAwardCount())
                        .awardCountSurplus(strategyAward.getAwardCountSurplus())
                        .awardRate(strategyAward.getAwardRate())
                        .build();
            strategyAwardEntities.add(strategyAwardEntity);
        }
        redisService.setValue(cacheKey,strategyAwardEntities);
        return strategyAwardEntities;
    }

    @Override
    public void storeStrategyAwardSearchRateTables(String strategyId, BigDecimal rateRange, HashMap<Integer, Integer> shuffleStrategyAwardSearchRateTables) {
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY+strategyId,rateRange.intValue());
        Map<Integer,Integer> cacheRateTable=redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY+strategyId);
        cacheRateTable.putAll(shuffleStrategyAwardSearchRateTables);
    }

    @Override
    public int getRateRange(Long strategyId) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY+strategyId);
    }

    @Override
    public int getRateRange(String strategyId) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY+strategyId);
    }

    @Override
    public Integer getStrategyAwardAssemble(String strategyId, int rateKey) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY+strategyId,rateKey);
    }

    @Override
    public StrategyEntity queryStrategyByStrategyId(Long strategyId) {
        String cacheKey=Constants.RedisKey.STRATEGY_KEY+strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);
        if(null!=strategyEntity)return strategyEntity;
        Strategy strategy=strategyDao.queryStrategyByStrategyId(strategyId);
        if (null == strategy) return StrategyEntity.builder().build();
        strategyEntity = StrategyEntity.builder()
        .strategyId(strategy.getStrategyId())
        .strategyDesc(strategy.getStrategyDesc())
        .ruleModels(strategy.getRuleModels())
        .build();
        redisService.setValue(cacheKey,strategyEntity);
        return strategyEntity;
    }

    @Override
    public StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel) {
        StrategyRule strategyRuleReq=new StrategyRule();
        strategyRuleReq.setStrategyId(strategyId);
        strategyRuleReq.setRuleModel(ruleModel);
        StrategyRule strategyRule = strategyRuleDao.queryStrategyRule(strategyRuleReq);
        return StrategyRuleEntity.builder()
                .strategyId(strategyRule.getStrategyId())
                .awardId(strategyRule.getAwardId())
                .ruleType(strategyRule.getRuleType())
                .ruleModel(strategyRule.getRuleModel())
                .ruleValue(strategyRule.getRuleValue())
                .ruleDesc(strategyRule.getRuleDesc())
                .build();

    }

    @Override
    public String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel) {
        StrategyRule strategyRule=new StrategyRule();
        strategyRule.setStrategyId(strategyId);
        strategyRule.setAwardId(awardId);
        strategyRule.setRuleModel(ruleModel);
        return strategyRuleDao.queryStrategyRuleValue(strategyRule);
    }

    @Override
    public StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        String ruleModels = strategyAwardDao.queryStrategyAwardRuleModels(strategyAward);
        return StrategyAwardRuleModelVO.builder().ruleModels(ruleModels).build();

    }
}
