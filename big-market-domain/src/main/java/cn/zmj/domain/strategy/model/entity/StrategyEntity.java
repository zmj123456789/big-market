package cn.zmj.domain.strategy.model.entity;

import cn.zmj.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyEntity {
    private Long strategyId;
    private String strategyDesc;
    /**
     * 抽奖规则模型 rule_weight,rule_blacklist
     */

    private String ruleModels;
    //对rulemodels字符串分割，返回策略列表
    public String[] ruleModels(){
        if(StringUtils.isBlank(ruleModels)) return null;
        return ruleModels.split(Constants.SPLIT);
    }
    public String getRuleWeight(){
        String[] ruleModels=this.ruleModels();
        if (null == ruleModels) return null;
        for(String ruleModel:ruleModels){
            if("rule_weight".equals(ruleModel))return ruleModel;
        }
        return null;
    }
}
