package cn.zmj.domain.rebate.model.entity;

import cn.zmj.domain.rebate.model.valobj.BehaviorTypeVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行为实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorEntity {
//    用户id
    private String userId;
//    行为类型sign签到openai_pay支付
    private BehaviorTypeVO behaviorTypeVO;
//    业务id
    private String outBusinessNo;

}
