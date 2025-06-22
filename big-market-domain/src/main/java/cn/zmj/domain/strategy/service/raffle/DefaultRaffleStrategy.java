package cn.zmj.domain.strategy.service.raffle;

import cn.zmj.domain.strategy.model.entity.RaffleFactorEntity;
import cn.zmj.domain.strategy.model.entity.RuleActionEntity;
import cn.zmj.domain.strategy.model.entity.RuleMatterEntity;
import cn.zmj.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.domain.strategy.service.armory.IStrategyDispatch;
import cn.zmj.domain.strategy.service.rule.ILogicFilter;
import cn.zmj.domain.strategy.service.rule.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 默认的抽奖策略实现
 * @create 2024-01-06 11:46
 */

@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy{
    @Resource
    private DefaultLogicFactory logicFactory;

    public DefaultRaffleStrategy(IStrategyDispatch strategyDispatch, IStrategyRepository repository) {
        super(strategyDispatch, repository);
    }
    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity,String... logics){
        if (logics == null || 0 == logics.length) return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                .build();

        //从 DefaultLogicFactory 获取所有注册的规则过滤器
        //返回一个 Map，key 是规则类型，value 是对应的过滤器实现
        Map<String, ILogicFilter<RuleActionEntity.RaffleBeforeEntity>> logicFilterGroup =logicFactory.openLogicFilter();
        //黑名单规则优先过滤
        //使用流式操作查找是否有黑名单规则
        String ruleBackList = Arrays.stream(logics).filter(str -> str.contains(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode())).findFirst().orElse(null);
        //如果找到黑名单规则：
        if (StringUtils.isNotBlank(ruleBackList)) {
            //获取对应的过滤器
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode());
            //构建规则物料 RuleMatterEntity
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setAwardId(raffleFactorEntity.getAwardId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleMatterEntity.setRuleModel(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode());
            //执行过滤
            RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            //如果结果是 TAKE_OVER（非 ALLOW），直接返回结果
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) {
                return ruleActionEntity;
            }
        }

        // 顺序过滤剩余规则
        List<String> ruleList = Arrays.stream(logics)
                .filter(s -> !s.equals(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode()))
                .collect(Collectors.toList());
        //过滤掉黑名单规则，得到其他需要检查的规则列表
        //遍历这些规则：
        //获取对应的过滤器
        //构建规则物料
        //执行过滤
        //记录日志
        //如果遇到非 ALLOW 的结果，立即返回
        //如果所有规则都通过，返回最后一个规则的结果
        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = null;
        for (String ruleModel : ruleList) {
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(ruleModel);
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setAwardId(ruleMatterEntity.getAwardId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleMatterEntity.setRuleModel(ruleModel);
            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            // 非放行结果则顺序过滤
            log.info("抽奖前规则过滤 userId: {} ruleModel: {} code: {} info: {}", raffleFactorEntity.getUserId(), ruleModel, ruleActionEntity.getCode(), ruleActionEntity.getInfo());
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) return ruleActionEntity;
        }

        return ruleActionEntity;
    }

    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity raffleFactorEntity, String... logics) {
        if (logics == null || 0 == logics.length) return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                .build();
        Map<String, ILogicFilter<RuleActionEntity.RaffleCenterEntity>> logicFilterGroup = logicFactory.openLogicFilter();
        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleActionEntity=null;
        for (String ruleModel : logics) {
            ILogicFilter<RuleActionEntity.RaffleCenterEntity> logicFilter = logicFilterGroup.get(ruleModel);
            RuleMatterEntity ruleMatterEntity=new RuleMatterEntity();
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setAwardId(raffleFactorEntity.getAwardId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleMatterEntity.setRuleModel(ruleModel);
            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            // 非放行结果则顺序过滤,给个参与奖
            log.info("抽奖中规则过滤 userId: {} ruleModel: {} code: {} info: {}", raffleFactorEntity.getUserId(), ruleModel, ruleActionEntity.getCode(), ruleActionEntity.getInfo());
            if(!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode()))return ruleActionEntity;
        }
        return ruleActionEntity;

    }
}
