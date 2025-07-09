package cn.zmj.domain.activity.service.rule.imp;

import cn.zmj.domain.activity.model.entity.ActivityCountEntity;
import cn.zmj.domain.activity.model.entity.ActivityEntity;
import cn.zmj.domain.activity.model.entity.ActivitySkuEntity;
import cn.zmj.domain.activity.model.valobj.ActivityStateVO;
import cn.zmj.domain.activity.service.rule.AbstractActionChain;
import cn.zmj.types.enums.ResponseCode;
import cn.zmj.types.exception.AppException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component("activity_base_action")
public class ActivityBaseActionChain extends AbstractActionChain {

    @Override
    public boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        log.info("活动责任链-基础信息【有效期、状态】校验开始。sku:{},activityId:{}",activitySkuEntity.getSku(),activityEntity.getActivityId());
//        校验活动开始
        if(!ActivityStateVO.open.equals(activityEntity.getState())){
            throw new AppException(ResponseCode.ACTIVITY_STATE_ERROR.getCode(), ResponseCode.ACTIVITY_STATE_ERROR.getInfo());
        }
//        校验：活动日期【开始时间<当前时间->结束时间】
        Date currentDate=new Date();
        if(activityEntity.getBeginDateTime().after(currentDate)||activityEntity.getEndDateTime().before(currentDate)){
            throw new AppException(ResponseCode.ACTIVITY_DATE_ERROR.getCode(), ResponseCode.ACTIVITY_DATE_ERROR.getInfo());
        }
//        校验：活动库存
        if(activitySkuEntity.getStockCount()<=0){
            throw new AppException(ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getCode(), ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getInfo());
        }
        return next().action(activitySkuEntity,activityEntity,activityCountEntity);
    }
}
