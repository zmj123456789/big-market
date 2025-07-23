package cn.zmj.infrastructure.persistent.repository;

import cn.zmj.domain.strategy.model.entity.StrategyAwardEntity;
import cn.zmj.domain.strategy.model.entity.StrategyEntity;
import cn.zmj.domain.strategy.model.entity.StrategyRuleEntity;
import cn.zmj.domain.strategy.model.valobj.*;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.zmj.infrastructure.persistent.dao.*;
import cn.zmj.infrastructure.persistent.po.*;
import cn.zmj.infrastructure.persistent.redis.IRedisService;
import cn.zmj.types.common.Constants;
import cn.zmj.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static cn.zmj.types.enums.ResponseCode.UN_ASSEMBLED_STRATEGY_ARMORY;

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
    @Resource
    private IRuleTreeDao ruleTreeDao;
    @Resource
    private IRuleTreeNodeDao ruleTreeNodeDao;
    @Resource
    private IRuleTreeNodeLineDao ruleTreeNodeLineDao;
    @Resource
    private IRaffleActivityDao raffleActivityDao;
    @Resource
    private IRaffleActivityAccountDayDao raffleActivityAccountDayDao;
    @Resource
    private IRaffleActivityAccountDao raffleActivityAccountDao;
//    从缓存或数据库中查询指定策略的奖品列表，并将结果缓存到 Redis 中，以提高后续访问效率。
    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {
        String cacheKey= Constants.RedisKey.STRATEGY_AWARD_LIST_KEY+strategyId;
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
                        .awardTitle(strategyAward.getAwardTitle())
                        .awardSubtitle(strategyAward.getAwardSubtitle())
                        .awardCount(strategyAward.getAwardCount())
                        .awardCountSurplus(strategyAward.getAwardCountSurplus())
                        .awardRate(strategyAward.getAwardRate())
                        .sort(strategyAward.getSort())
                        .ruleModels(strategyAward.getRuleModels())
                        .build();
            strategyAwardEntities.add(strategyAwardEntity);
        }
        redisService.setValue(cacheKey,strategyAwardEntities);
        return strategyAwardEntities;
    }

    @Override
    public void storeStrategyAwardSearchRateTables(String strategyId, Integer rateRange, HashMap<Integer, Integer> shuffleStrategyAwardSearchRateTables) {
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY+strategyId,rateRange);
        Map<Integer,Integer> cacheRateTable=redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY+strategyId);
        cacheRateTable.putAll(shuffleStrategyAwardSearchRateTables);
    }

    @Override
    public int getRateRange(Long strategyId) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY+strategyId);
    }

    @Override
    public int getRateRange(String strategyId) {
        String cacheKey=Constants.RedisKey.STRATEGY_RATE_RANGE_KEY+strategyId;
        if(!redisService.isExists(cacheKey)){
            throw new AppException(UN_ASSEMBLED_STRATEGY_ARMORY.getCode(), cacheKey + Constants.COLON + UN_ASSEMBLED_STRATEGY_ARMORY.getInfo());

        }
        return redisService.getValue(cacheKey);
    }

    @Override
    public Integer getStrategyAwardAssemble(String strategyId, int rateKey) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY+strategyId,rateKey);
    }

    @Override
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) {
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
    public String queryStrategyRuleValue(Long strategyId, String ruleModel) {
        return queryStrategyRuleValue(strategyId,null,ruleModel);
    }

    @Override
    public StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        String ruleModels = strategyAwardDao.queryStrategyAwardRuleModels(strategyAward);
        return StrategyAwardRuleModelVO.builder().ruleModels(ruleModels).build();

    }

    @Override
    public RuleTreeVO queryRuleTreeVOByTreeId(String treeId) {
//        先从缓存获取
        String cacheKey=Constants.RedisKey.RULE_TREE_VO_KEY+treeId;
        RuleTreeVO ruleTreeVOCache = redisService.getValue(cacheKey);
        if(ruleTreeVOCache!=null){
            return ruleTreeVOCache;
        }
//        从数据库获取
        RuleTree ruleTree = ruleTreeDao.queryRuleTreeByTreeId(treeId);
        List<RuleTreeNode> ruleTreeNodes = ruleTreeNodeDao.queryRuleTreeNodeListByTreeId(treeId);
        List<RuleTreeNodeLine> ruleTreeNodeLines = ruleTreeNodeLineDao.queryRuleTreeNodeLineListByTreeId(treeId);
//        tree node line 转为Map结构
        Map<String,List<RuleTreeNodeLineVO>> ruleTreeNodeLineMap=new HashMap<>();
        for (RuleTreeNodeLine ruleTreeNodeLine : ruleTreeNodeLines) {
            RuleTreeNodeLineVO ruleTreeNodeLineVO = RuleTreeNodeLineVO.builder()
                    .treeId(ruleTreeNodeLine.getTreeId())
                    .ruleNodeFrom(ruleTreeNodeLine.getRuleNodeFrom())
                    .ruleNodeTo(ruleTreeNodeLine.getRuleNodeTo())
                    .ruleLimitType(RuleLimitTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitType()))
                    .ruleLimitValue(RuleLogicCheckTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitValue()))
                    .build();
//            检查 ruleTreeNodeLineMap 中是否存在以 ruleNodeFrom 为键的条目：
//若存在：直接返回对应的 List<RuleTreeNodeLineVO>。
//若不存在：执行第二个参数（Lambda表达式），新建一个空 ArrayList 并存入 Map，然后返回该列表。
            List<RuleTreeNodeLineVO> ruleTreeNodeLineVOList = ruleTreeNodeLineMap.computeIfAbsent(ruleTreeNodeLine.getRuleNodeFrom(), k -> new ArrayList<>());
            ruleTreeNodeLineVOList.add(ruleTreeNodeLineVO);

        }
//        treenode转换为map
        Map<String,RuleTreeNodeVO> treeNodeMap=new HashMap<>();
        for (RuleTreeNode ruleTreeNode : ruleTreeNodes) {
            RuleTreeNodeVO ruleTreeNodeVO = RuleTreeNodeVO.builder()
                    .treeId(ruleTreeNode.getTreeId())
                    .ruleKey(ruleTreeNode.getRuleKey())
                    .ruleDesc(ruleTreeNode.getRuleDesc())
                    .ruleValue(ruleTreeNode.getRuleValue())
                    .treeNodeLineVOList(ruleTreeNodeLineMap.get(ruleTreeNode.getRuleKey()))
                    .build();
            treeNodeMap.put(ruleTreeNode.getRuleKey(),ruleTreeNodeVO);

        }
//        构建ruletree
        RuleTreeVO ruleTreeVODB = RuleTreeVO.builder()
                .treeId(ruleTree.getTreeId())
                .treeName(ruleTree.getTreeName())
                .treeDesc(ruleTree.getTreeDesc())
                .treeRootRuleNode(ruleTree.getTreeRootRuleKey())
                .treeNodeMap(treeNodeMap)
                .build();
        redisService.setValue(cacheKey,ruleTreeVODB);
        return ruleTreeVODB;
    }

    @Override
    public void cacheStrategyAwardCount(String cacheKey, Integer awardCount) {
        if(null!=redisService.getValue(cacheKey))return;
        redisService.setAtomicLong(cacheKey,awardCount);
    }

    @Override
    public Boolean subtractionAwardStock(String cacheKey) {
        return subtractionAwardStock(cacheKey,null);
    }

    @Override
    public Boolean subtractionAwardStock(String cacheKey, Date endDateTime) {
        long surplus = redisService.decr(cacheKey);
        if(surplus<0){
            redisService.setAtomicLong(cacheKey,0);
            return false;
        }
        String lockKey=cacheKey+Constants.UNDERLINE+surplus;
        Boolean lock = false;
        if(endDateTime!=null){
            long expireMills=endDateTime.getTime()-System.currentTimeMillis()+TimeUnit.DAYS.toMillis(1);
            lock = redisService.setNx(lockKey, expireMills, TimeUnit.MILLISECONDS);
        }else{
            lock=redisService.setNx(lockKey);
        }
        if(!lock){
            log.info("策略奖品库存加锁失败{}",lockKey);
        }
        return lock;
    }

    @Override
    public void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<StrategyAwardStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(strategyAwardStockKeyVO,3, TimeUnit.SECONDS);
    }

    @Override
    public StrategyAwardStockKeyVO takeQueueValue() throws InterruptedException {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        return blockingQueue.poll();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        StrategyAward strategyAward=new StrategyAward();
        strategyAward.setAwardId(awardId);
        strategyAward.setStrategyId(strategyId);
        strategyAwardDao.updateStrategyAwardStock(strategyAward);
    }

    @Override
    public StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId) {
//        优先从缓存获取
        String cacheKey=Constants.RedisKey.STRATEGY_AWARD_KEY+strategyId+Constants.UNDERLINE+awardId;
        StrategyAwardEntity strategyAwardEntity = redisService.getValue(cacheKey);
        if(null!=strategyAwardEntity)return strategyAwardEntity;
//        查询数据
        StrategyAward strategyAwardReq = new StrategyAward();
        strategyAwardReq.setStrategyId(strategyId);
        strategyAwardReq.setAwardId(awardId);
        StrategyAward strategyAwardRes = strategyAwardDao.queryStrategyAward(strategyAwardReq);
//        转换数据
        strategyAwardEntity = StrategyAwardEntity.builder()
        .strategy_id(strategyAwardRes.getStrategyId())
        .awardId(strategyAwardRes.getAwardId())
        .awardTitle(strategyAwardRes.getAwardTitle())
        .awardSubtitle(strategyAwardRes.getAwardSubtitle())
        .awardCount(strategyAwardRes.getAwardCount())
        .awardCountSurplus(strategyAwardRes.getAwardCountSurplus())
        .awardRate(strategyAwardRes.getAwardRate())
        .sort(strategyAwardRes.getSort())
        .build();
        // 缓存结果
        redisService.setValue(cacheKey, strategyAwardEntity);
        // 返回数据
        return strategyAwardEntity;

    }

    @Override
    public Long queryStrategyIdByActivity(Long activityId) {
        return raffleActivityDao.queryStrategyIdByActivityId(activityId);
    }

    @Override
    public Integer queryTodayUserRaffleCount(String userId, Long strategyId) {
//        活动ID
        Long activityId = raffleActivityDao.queryActivityIdByStrategyId(strategyId);
//        封装参数
        RaffleActivityAccountDay raffleActivityAccountDayReq=new RaffleActivityAccountDay();
        raffleActivityAccountDayReq.setUserId(userId);
        raffleActivityAccountDayReq.setActivityId(activityId);
        raffleActivityAccountDayReq.setDay(raffleActivityAccountDayReq.currentDay());
        RaffleActivityAccountDay raffleActivityAccountDay = raffleActivityAccountDayDao.queryActivityAccountDayByUserId(raffleActivityAccountDayReq);
        if(raffleActivityAccountDay==null)return 0;
        return raffleActivityAccountDay.getDayCount()-raffleActivityAccountDay.getDayCountSurplus();
    }

    @Override
    public Map<String, Integer> queryAwardRuleLockCount(String[] treeIds) {
        if(treeIds==null||treeIds.length==0)return new HashMap<>();
        List<RuleTreeNode> ruleTreeNodes=ruleTreeNodeDao.queryRuleLocks(treeIds);
        Map<String,Integer> resultMap=new HashMap<>();
        for (RuleTreeNode ruleTreeNode : ruleTreeNodes) {
            String treeId=ruleTreeNode.getTreeId();
            Integer ruleValue = Integer.valueOf(ruleTreeNode.getRuleValue());
            resultMap.put(treeId,ruleValue);
        }
        return resultMap;


    }

    @Override
    public List<RuleWeightVO> queryAwardRuleWeight(Long strategyId) {
//        优先从缓存获取
        String cacheKey=Constants.RedisKey.STRATEGY_RULE_WEIGHT_KEY+strategyId;
        List<RuleWeightVO> ruleWeightVOS= redisService.getValue(cacheKey);
        if(ruleWeightVOS!=null)return ruleWeightVOS;
        ruleWeightVOS=new ArrayList<>();
//        查询权重规则配置
        StrategyRule strategyRule=new StrategyRule();
        strategyRule.setStrategyId(strategyId);
        strategyRule.setRuleModel(DefaultChainFactory.LogicModel.RULE_WEIGHT.getCode());
        String ruleValue = strategyRuleDao.queryStrategyRuleValue(strategyRule);
//        借助实体对象转换规则
        StrategyRuleEntity strategyRuleEntity=new StrategyRuleEntity();
        strategyRuleEntity.setRuleModel(DefaultChainFactory.LogicModel.RULE_WEIGHT.getCode());
        strategyRuleEntity.setRuleValue(ruleValue);
        Map<String,List<Integer>> ruleWeightValues=strategyRuleEntity.getRuleWeightValues();
//        遍历规则组装奖品配置
        Set<String> ruleWeightKeys=ruleWeightValues.keySet();
        for (String ruleWeightKey : ruleWeightKeys) {
            List<Integer> awardIds = ruleWeightValues.get(ruleWeightKey);
            List<RuleWeightVO.Award> awardList=new ArrayList<>();
//            也可以修改为一次数据库查询
            for (Integer awardId : awardIds) {
                StrategyAward strategyAwardReq=new StrategyAward();
                strategyAwardReq.setStrategyId(strategyId);
                strategyAwardReq.setAwardId(awardId);
                StrategyAward strategyAward = strategyAwardDao.queryStrategyAward(strategyAwardReq);
                awardList.add(RuleWeightVO.Award.builder().awardId(awardId).awardTitle(strategyAward.getAwardTitle()).build());
            }
            ruleWeightVOS.add(RuleWeightVO.builder().ruleValue(ruleValue).weight(Integer.valueOf(ruleWeightKey.split(Constants.COLON)[0])).awardIds(awardIds).awardList(awardList).build());

        }
        // 设置缓存 - 实际场景中，这类数据，可以在活动下架的时候统一清空缓存。
        redisService.setValue(cacheKey,ruleWeightValues);
        return ruleWeightVOS;
    }

    @Override
    public Integer queryActivityAccountTotalUseCount(String userId, Long strategyId) {
        Long activityId=raffleActivityDao.queryStrategyIdByActivityId(strategyId);
        RaffleActivityAccount raffleActivityAccount = raffleActivityAccountDao.queryActivityAccountByUserId(RaffleActivityAccount.builder().userId(userId).activityId(activityId).build());
        return raffleActivityAccount.getTotalCount()-raffleActivityAccount.getTotalCountSurplus();
    }
}
