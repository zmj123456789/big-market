package cn.zmj.infrastructure.persistent.repository;


import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import cn.zmj.domain.award.model.aggregate.UserAwardRecordAggregate;
import cn.zmj.domain.award.model.entity.TaskEntity;
import cn.zmj.domain.award.model.entity.UserAwardRecordEntity;
import cn.zmj.domain.award.repository.IAwardRepository;
import cn.zmj.infrastructure.event.EventPublisher;
import cn.zmj.infrastructure.persistent.dao.ITaskDao;
import cn.zmj.infrastructure.persistent.dao.IUserAwardRecordDao;
import cn.zmj.infrastructure.persistent.dao.IUserRaffleOrderDao;
import cn.zmj.infrastructure.persistent.po.Task;
import cn.zmj.infrastructure.persistent.po.UserAwardRecord;
import cn.zmj.infrastructure.persistent.po.UserRaffleOrder;
import cn.zmj.types.enums.ResponseCode;
import cn.zmj.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 奖品仓储服务
 * @create 2024-04-06 10:09
 */
@Slf4j
@Repository
public class AwardRespoitory implements IAwardRepository {
    @Resource
    private IDBRouterStrategy dbRouter;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private IUserAwardRecordDao userAwardRecordDao;
    @Resource
    private ITaskDao taskDao;
    @Autowired
    private EventPublisher eventPublisher;
    @Resource
    private IUserRaffleOrderDao userRaffleOrderDao;

    @Override
    public void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate) {
//        从聚合对象中获取奖品记录实体和任务实体
        UserAwardRecordEntity userAwardRecordEntity = userAwardRecordAggregate.getUserAwardRecordEntity();
        TaskEntity taskEntity = userAwardRecordAggregate.getTaskEntity();
        String userId = userAwardRecordEntity.getUserId();
        Long activityId = userAwardRecordEntity.getActivityId();
        Integer awardId = userAwardRecordEntity.getAwardId();
        UserAwardRecord userAwardRecord = UserAwardRecord.builder()
                .userId(userAwardRecordEntity.getUserId())
                .activityId(userAwardRecordEntity.getActivityId())
                .strategyId(userAwardRecordEntity.getStrategyId())
                .orderId(userAwardRecordEntity.getOrderId())
                .awardId(userAwardRecordEntity.getAwardId())
                .awardTitle(userAwardRecordEntity.getAwardTitle())
                .awardTime(userAwardRecordEntity.getAwardTime())
                .awardState(userAwardRecordEntity.getAwardState().getCode())
                .build();
        Task task=new Task();
        task.setUserId(taskEntity.getUserId());
        task.setTopic(taskEntity.getTopic());
        task.setMessageId(taskEntity.getMessageId());
        task.setMessage(JSON.toJSONString(taskEntity.getMessage()));
        task.setState(taskEntity.getState().getCode());
        UserRaffleOrder userRaffleOrderReq=new UserRaffleOrder();
        userRaffleOrderReq.setUserId(userAwardRecordEntity.getUserId());
        userRaffleOrderReq.setOrderId(userAwardRecord.getOrderId());
        try {
            dbRouter.doRouter(userId);
            transactionTemplate.execute(new TransactionCallback<Integer>() {
                @Override
                public Integer doInTransaction(TransactionStatus status) {
                    try {
    //                写入奖品记录
                        userAwardRecordDao.insert(userAwardRecord);
    //                写入消息记录
                        taskDao.insert(task);
//                        更新抽奖单
                        int count = userRaffleOrderDao.updateUserRaffleOrderStateUsed(userRaffleOrderReq);
                        if(count!=1){
                            status.setRollbackOnly();
                            log.error("写入中奖记录用户抽奖已使用过，不可重复抽userId: {} activityId: {} awardId: {}", userId, activityId, awardId);
                            throw new AppException(ResponseCode.ACTIVITY_ORDER_ERROR.getCode(), ResponseCode.ACTIVITY_ORDER_ERROR.getInfo());
                        }
                        return 1;
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        log.error("写入中奖记录，唯一索引冲突 userId: {} activityId: {} awardId: {}", userId, activityId, awardId, e);
                        throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                    }
                }
            });
        } finally {
            dbRouter.clear();
        }
        try {
//        发送消息【在事务外执行如果失败有任务补偿】
            eventPublisher.publish(task.getTopic(),task.getMessage());
//        更新数据库记录，task任务表
            taskDao.updateTaskSendMessageCompleted(task);
        } catch (Exception e) {
            log.error("写入中奖记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
            taskDao.updateTaskSendMessageFail(task);
        }


    }
}
