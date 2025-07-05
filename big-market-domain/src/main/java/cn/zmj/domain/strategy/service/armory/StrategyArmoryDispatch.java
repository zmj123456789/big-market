package cn.zmj.domain.strategy.service.armory;

import cn.zmj.domain.strategy.model.entity.StrategyAwardEntity;
import cn.zmj.domain.strategy.model.entity.StrategyEntity;
import cn.zmj.domain.strategy.model.entity.StrategyRuleEntity;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.types.common.Constants;
import cn.zmj.types.enums.ResponseCode;
import cn.zmj.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
public class StrategyArmoryDispatch implements IStrategyArmory,IStrategyDispatch{
    @Resource
    private IStrategyRepository repository;
    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        //查询策略配置
        List<StrategyAwardEntity> strategyAwardEntities=repository.queryStrategyAwardList(strategyId);
//        缓存奖品库存【用于decr扣减库存】
        for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntities) {
            Integer awardId = strategyAwardEntity.getAwardId();
            Integer awardCount = strategyAwardEntity.getAwardCount();
            cacheStrategyAwardCount(strategyId,awardId,awardCount);
        }
        //先将所有策略的奖品范围存入redis
        assembleLotteryStrategy(String.valueOf(strategyId),strategyAwardEntities);
        //权重策略配置-适用于权重规则配置
        StrategyEntity strategyEntity=repository.queryStrategyEntityByStrategyId(strategyId);
        String ruleWeight=strategyEntity.getRuleWeight();
        if(null==ruleWeight)return true;
        StrategyRuleEntity strategyRuleEntity=repository.queryStrategyRule(strategyId,ruleWeight);
        if(null==strategyRuleEntity){
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(), ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getInfo());
        }
        Map<String,List<Integer>> ruleWeightValueMap=strategyRuleEntity.getRuleWeightValues();
        Set<String> keys=ruleWeightValueMap.keySet();
        for (String key : keys) {
            List<Integer> ruleWeightValues = ruleWeightValueMap.get(key);
            ArrayList<StrategyAwardEntity> strategyAwardEntitiesClone = new ArrayList<>(strategyAwardEntities);
            strategyAwardEntitiesClone.removeIf(strategyAwardEntity -> !ruleWeightValues.contains(strategyAwardEntity.getAwardId()));
            assembleLotteryStrategy(String.valueOf(strategyId).concat(Constants.UNDERLINE).concat(key),strategyAwardEntitiesClone);


        }
        return true;

    }
    public void assembleLotteryStrategy(String key,List<StrategyAwardEntity> strategyAwardEntities){
        //获取最小概率值，使用 BigDecimal 是为了避免浮点数精度问题，适用于金融、抽奖等对精度要求高的场景
        BigDecimal minAwardRate= strategyAwardEntities.stream().map(StrategyAwardEntity::getAwardRate).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        //获得概率总和，reduce方法累加
        BigDecimal totalAwardRate = strategyAwardEntities.stream().map(StrategyAwardEntity::getAwardRate).reduce(BigDecimal.ZERO, BigDecimal::add);
        //用1%0.0001获取概率范围，百分位，千分位，万分位
        BigDecimal rateRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);
        ArrayList<Object> strategyAwardSearchRateTables=new ArrayList<>(rateRange.intValue());
        for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntities) {
            Integer awardId=strategyAwardEntity.getAwardId();
            BigDecimal awardRate = strategyAwardEntity.getAwardRate();
            //每个奖品的概率*最大范围，填充每个奖品的Id
            for(int i=0;i<rateRange.multiply(awardRate).setScale(0,RoundingMode.CEILING).intValue();i++){
                strategyAwardSearchRateTables.add(awardId);
            }

        }
        //6乱序
        Collections.shuffle(strategyAwardSearchRateTables);
        HashMap<Integer,Integer> shuffleStrategyAwardSearchRateTables=new HashMap<>();
        for (int i = 0; i < strategyAwardSearchRateTables.size(); i++) {
            shuffleStrategyAwardSearchRateTables.put(i, (Integer) strategyAwardSearchRateTables.get(i));
        }

        //存储到redis
        repository.storeStrategyAwardSearchRateTables(key,shuffleStrategyAwardSearchRateTables.size(),shuffleStrategyAwardSearchRateTables);
    }

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        int rateRange=repository.getRateRange(strategyId);
        return repository.getStrategyAwardAssemble(strategyId.toString(),new SecureRandom().nextInt(rateRange));
    }


    @Override
    public Integer getRandomAwardId(Long strategyId,String ruleWeightValue) {
        String key=String.valueOf(strategyId).concat(Constants.UNDERLINE).concat(ruleWeightValue);

        return getRandomAwardId(key);
    }

    @Override
    public Integer getRandomAwardId(String key) {
        int rateRange=repository.getRateRange(key);
        return repository.getStrategyAwardAssemble(key,new SecureRandom().nextInt(rateRange));
    }

    @Override
    public Boolean subtractionAwardStock(Long strategyId, Integer awardId) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + strategyId + Constants.UNDERLINE + awardId;
        return repository.subtractionAwardStock(cacheKey);
    }

    private void cacheStrategyAwardCount(Long strategyId,Integer awardId,Integer awardCount){
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + strategyId + Constants.UNDERLINE + awardId;
        repository.cacheStrategyAwardCount(cacheKey,awardCount);
    }
}
