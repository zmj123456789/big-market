package cn.zmj.domain.rebate.service;

import cn.zmj.domain.rebate.model.entity.BehaviorEntity;
import cn.zmj.domain.rebate.model.entity.BehaviorRebateOrderEntity;

import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 行为返利服务接口
 * @create 2024-04-30 14:05
 */

public interface IBehaviorRebateService {
//    创建行为动作入账订单
    List<String> createOrder(BehaviorEntity behaviorEntity);

    List<BehaviorRebateOrderEntity> queryOrderByOutBusinessNo(String userId, String outBusinessNo);
}
