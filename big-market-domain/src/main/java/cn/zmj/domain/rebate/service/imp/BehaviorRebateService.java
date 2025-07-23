package cn.zmj.domain.rebate.service.imp;

import cn.zmj.domain.rebate.event.SendRebateMessageEvent;
import cn.zmj.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import cn.zmj.domain.rebate.model.entity.BehaviorEntity;
import cn.zmj.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import cn.zmj.domain.rebate.model.entity.TaskEntity;
import cn.zmj.domain.rebate.model.valobj.DailyBehaviorRebateVO;
import cn.zmj.domain.rebate.model.valobj.TaskStateVO;
import cn.zmj.domain.rebate.repository.IBehaviorRebateRepository;
import cn.zmj.domain.rebate.service.IBehaviorRebateService;
import cn.zmj.types.common.Constants;
import cn.zmj.types.event.BaseEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 行为返利服务实现
 * @create 2024-04-30 15:31
 */
@Service
public class BehaviorRebateService implements IBehaviorRebateService {
    @Resource
    private IBehaviorRebateRepository behaviorRebateRepository;
    @Autowired
    private SendRebateMessageEvent sendRebateMessageEvent;

    @Override
    public List<String> createOrder(BehaviorEntity behaviorEntity) {
//        查询返利配置
        List<DailyBehaviorRebateVO> dailyBehaviorRebateVOS = behaviorRebateRepository.queryDailyBehaviorRebateConfig(behaviorEntity.getBehaviorTypeVO());
        if(dailyBehaviorRebateVOS==null||dailyBehaviorRebateVOS.isEmpty())return new ArrayList<>();

//        构建聚合对象
        List<String> orderIds=new ArrayList<>();
        List<BehaviorRebateAggregate> behaviorRebateAggregates=new ArrayList<>();
        for (DailyBehaviorRebateVO dailyBehaviorRebateVO : dailyBehaviorRebateVOS) {
            // 拼装业务ID；用户ID_返利类型_外部透彻业务ID
            String bizId=behaviorEntity.getUserId()+ Constants.UNDERLINE+dailyBehaviorRebateVO.getRebateType()+Constants.UNDERLINE+behaviorEntity.getOutBusinessNo();
            BehaviorRebateOrderEntity behaviorRebateOrderEntity=new BehaviorRebateOrderEntity();
            behaviorRebateOrderEntity.setUserId(behaviorEntity.getUserId());
            behaviorRebateOrderEntity.setOrderId(RandomStringUtils.randomNumeric(12));
            behaviorRebateOrderEntity.setBehaviorType(dailyBehaviorRebateVO.getBehaviorType());
            behaviorRebateOrderEntity.setRebateDesc(dailyBehaviorRebateVO.getRebateDesc());
            behaviorRebateOrderEntity.setRebateType(dailyBehaviorRebateVO.getRebateType());
            behaviorRebateOrderEntity.setRebateConfig(dailyBehaviorRebateVO.getRebateConfig());
            behaviorRebateOrderEntity.setOutBusinessNo(behaviorEntity.getOutBusinessNo());
            behaviorRebateOrderEntity.setBizId(bizId);
            orderIds.add(behaviorRebateOrderEntity.getOrderId());
//            MQ消息对象
            SendRebateMessageEvent.RebateMessage rebateMessage=new SendRebateMessageEvent.RebateMessage();
           rebateMessage.setUserId(behaviorEntity.getUserId());
           rebateMessage.setRebateType(dailyBehaviorRebateVO.getRebateType());
           rebateMessage.setRebateConfig(dailyBehaviorRebateVO.getRebateConfig());
           rebateMessage.setBizId(bizId);
//           构建事件消息
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> rebateMessageEventMessage=sendRebateMessageEvent.buildEvent(rebateMessage);
//            组装任务对象
            TaskEntity taskEntity=new TaskEntity();
            taskEntity.setUserId(behaviorEntity.getUserId());
            taskEntity.setTopic(sendRebateMessageEvent.topic());
            taskEntity.setMessageId(rebateMessageEventMessage.getId());
            taskEntity.setMessage(rebateMessageEventMessage);
            taskEntity.setState(TaskStateVO.create);
            BehaviorRebateAggregate behaviorRebateAggregate=new BehaviorRebateAggregate();
            behaviorRebateAggregate.setBehaviorRebateOrderEntity(behaviorRebateOrderEntity);
            behaviorRebateAggregate.setUserId(behaviorEntity.getUserId());
            behaviorRebateAggregate.setTaskEntity(taskEntity);
            behaviorRebateAggregates.add(behaviorRebateAggregate);

        }
//        存储聚合对象数据
        behaviorRebateRepository.saveUserRebateRecord(behaviorEntity.getUserId(),behaviorRebateAggregates);
//        返回订单ID集合
        return orderIds;
    }

    @Override
    public List<BehaviorRebateOrderEntity> queryOrderByOutBusinessNo(String userId, String outBusinessNo) {
        return behaviorRebateRepository.queryOrderByOutBusinessNo(userId,outBusinessNo);
    }
}
