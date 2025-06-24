package cn.zmj.domain.strategy.service.rule.chain.impl;

import cn.zmj.domain.strategy.repository.IStrategyRepository;
import cn.zmj.domain.strategy.service.rule.chain.AbstractLogicChain;
import cn.zmj.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component("rule_blacklist")
public class BackListLogicChain extends AbstractLogicChain {
    @Resource
    private IStrategyRepository repository;
    @Override
    protected String ruleModel() {
        return "rule_blacklist";
    }

    @Override
    public Integer logic(String userId, Long strategyId) {
        log.info("抽奖责任链-黑名单开始 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
        String ruleValue = repository.queryStrategyRuleValue(strategyId, ruleModel());
        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        Integer awardId = Integer.parseInt(splitRuleValue[0]);

        // 过滤其他规则
        String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);
        for (String userBlackId : userBlackIds) {
            if (userId.equals(userBlackId)) {
                return awardId;
            }
        }
        // 过滤其他责任链
        log.info("抽奖责任链-黑名单放行 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
        return next().logic(userId,strategyId);
    }
}
