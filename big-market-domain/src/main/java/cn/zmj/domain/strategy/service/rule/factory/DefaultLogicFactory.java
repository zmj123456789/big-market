package cn.zmj.domain.strategy.service.rule.factory;


import cn.zmj.domain.strategy.model.entity.RuleActionEntity;
import cn.zmj.domain.strategy.service.annotation.LogicStrategy;
import cn.zmj.domain.strategy.service.rule.ILogicFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 规则工厂
 * @create 2023-12-31 11:23
 */
@Service
public class DefaultLogicFactory {
    public Map<String, ILogicFilter<?>> logicFilterMap = new ConcurrentHashMap<>();
//@Service或@Component注解标记的类会被Spring自动扫描并注册为Bean
//
//当Bean实现了接口，Spring会将其识别为该接口的实现
//
//当需要注入接口类型的集合时，Spring会注入所有匹配的实现
    //Spring 会自动注入所有实现了 ILogicFilter 接口的 Bean，组成一个列表。每个元素是一个具体的规则逻辑实现类（如权重规则、黑名单规则等）。
    public DefaultLogicFactory(List<ILogicFilter<?>> logicFilters) {
        logicFilters.forEach(logic -> {
            //使用AnnotationUtils.findAnnotation()扫描每个过滤器的@LogicStrategy注解
            //            使用 Spring 的 AnnotationUtils 查找当前类上是否有 @LogicStrategy 注解。
            LogicStrategy strategy = AnnotationUtils.findAnnotation(logic.getClass(), LogicStrategy.class);
//这个注解用于标识该规则对应的模型类型（如权重、黑名单）
            if (null != strategy) {
                // 将注解值作为key，实例作为value存入map
                logicFilterMap.put(strategy.logicMode().getCode(), logic);
            }
        });
    }
//. (Map<?, ?>) logicFilterMap
//将原始的 Map<String, ILogicFilter<?>> 强制转换为原始类型 Map<?, ?>，这是为了绕过 Java 泛型系统的限制。
//这一步称为“类型擦除”处理，使后续可以进行更灵活的泛型转换。
//3. (Map<String, ILogicFilter<T>>) ...
//再次将 Map<?, ?> 转换为具体的泛型类型 Map<String, ILogicFilter<T>>。
//这样调用者在使用时可以按指定类型 T 来获取和使用对应的规则过滤器，比如 T 可以是 RaffleBeforeEntity。
    public <T extends RuleActionEntity.RaffleEntity> Map<String, ILogicFilter<T>> openLogicFilter() {
        return (Map<String, ILogicFilter<T>>) (Map<?, ?>) logicFilterMap;
    }

    @Getter
    @AllArgsConstructor
    public enum LogicModel {

        RULE_WIGHT("rule_weight","【抽奖前规则】根据抽奖权重返回可抽奖范围KEY"),
        RULE_BLACKLIST("rule_blacklist","【抽奖前规则】黑名单规则过滤，命中黑名单则直接返回"),

        ;

        private final String code;
        private final String info;

    }

}
