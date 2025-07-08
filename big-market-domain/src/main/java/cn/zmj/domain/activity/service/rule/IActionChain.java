package cn.zmj.domain.activity.service.rule;

import cn.zmj.domain.activity.model.entity.ActivityCountEntity;
import cn.zmj.domain.activity.model.entity.ActivityEntity;
import cn.zmj.domain.activity.model.entity.ActivitySkuEntity;

public interface IActionChain extends IActionChainArmory {
    boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);
}
