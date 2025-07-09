package cn.zmj.domain.activity.service;

import cn.zmj.domain.activity.model.aggregate.CreateOrderAggregate;
import cn.zmj.domain.activity.model.entity.*;
import cn.zmj.domain.activity.repository.IActivityRepository;
import cn.zmj.domain.activity.service.rule.IActionChain;
import cn.zmj.domain.activity.service.rule.factory.DefaultActivityChainFactory;
import cn.zmj.types.enums.ResponseCode;
import cn.zmj.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 流程比较复杂时使用抽象类，定义出标准流程，给子类实现
 */
@Slf4j
public abstract class AbstractRaffleActivity extends RaffleActivitySupport implements IRaffleOrder{
    public AbstractRaffleActivity(DefaultActivityChainFactory defaultActivityChainFactory, IActivityRepository activityRepository) {
        super(defaultActivityChainFactory, activityRepository);
    }

    @Override
    public String createSkuRechargeOrder(SkuRechargeEntity skuRechargeEntity) {
//        参数校验
        String userId = skuRechargeEntity.getUserId();
        Long sku = skuRechargeEntity.getSku();
        String outBusinessNo = skuRechargeEntity.getOutBusinessNo();
        if(sku==null|| StringUtils.isBlank(userId)||StringUtils.isBlank(outBusinessNo)){
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
//        查询基础信息
//        通过sku查询活动信息
        ActivitySkuEntity activitySkuEntity = queryActivitySku(sku);
//        查询活动信息
        ActivityEntity activityEntity = queryRaffleActivityByActivityId(activitySkuEntity.getActivityId());
//        查询次数信息
        ActivityCountEntity activityCountEntity = queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());
//        活动动作规则校验 todo 后续处理规则过滤流程，暂时也不处理责任链结果
        IActionChain actionChain = defaultActivityChainFactory.openActionChain();
        actionChain.action(activitySkuEntity, activityEntity, activityCountEntity);
//        构建订单聚合对象
        CreateOrderAggregate createOrderAggregate = buildOrderAggregate(skuRechargeEntity, activitySkuEntity, activityEntity, activityCountEntity);
//        保存订单
        doSaveOrder(createOrderAggregate);
//        返回单号
        return createOrderAggregate.getActivityOrderEntity().getOrderId();
    }

    protected abstract CreateOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) ;
    protected abstract void doSaveOrder(CreateOrderAggregate createOrderAggregate);
}
