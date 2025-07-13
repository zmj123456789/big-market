package cn.zmj.domain.award.service;

import cn.zmj.domain.award.event.SendAwardMessageEvent;
import cn.zmj.domain.award.model.aggregate.UserAwardRecordAggregate;
import cn.zmj.domain.award.model.entity.TaskEntity;
import cn.zmj.domain.award.model.entity.UserAwardRecordEntity;
import cn.zmj.domain.award.model.valobj.TaskStateVO;
import cn.zmj.domain.award.repository.IAwardRepository;
import cn.zmj.types.event.BaseEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 奖品服务
 * @create 2024-04-06 09:39
 */
@Service
public class AwardService implements IAwardService{
    @Resource
    private IAwardRepository awardRepository;
    @Resource
    private SendAwardMessageEvent sendAwardMessageEvent;
    @Override
    public void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity) {
//        构建消息对象
        SendAwardMessageEvent.SendAwardMessage sendAwardMessage=new SendAwardMessageEvent.SendAwardMessage();
        sendAwardMessage.setUserId(userAwardRecordEntity.getUserId());
        sendAwardMessage.setAwardId(userAwardRecordEntity.getAwardId());
        sendAwardMessage.setAwardTitle(userAwardRecordEntity.getAwardTitle());
        BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> sendAwardMessageEventMessage = sendAwardMessageEvent.buildEvent(sendAwardMessage);
//        构建任务对象
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setUserId(userAwardRecordEntity.getUserId());
        taskEntity.setTopic(sendAwardMessageEvent.topic());
        taskEntity.setMessageId(sendAwardMessageEventMessage.getId());
        taskEntity.setMessage(sendAwardMessageEventMessage);
        taskEntity.setState(TaskStateVO.create);
//        构建聚合对象
        UserAwardRecordAggregate userAwardRecordAggregate=UserAwardRecordAggregate.builder().taskEntity(taskEntity).userAwardRecordEntity(userAwardRecordEntity).build();
//        存储聚合对象
        awardRepository.saveUserAwardRecord(userAwardRecordAggregate);

    }
}
