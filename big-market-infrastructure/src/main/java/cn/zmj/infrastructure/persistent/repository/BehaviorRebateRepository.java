package cn.zmj.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import cn.zmj.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import cn.zmj.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import cn.zmj.domain.rebate.model.entity.TaskEntity;
import cn.zmj.domain.rebate.model.valobj.BehaviorTypeVO;
import cn.zmj.domain.rebate.model.valobj.DailyBehaviorRebateVO;
import cn.zmj.domain.rebate.repository.IBehaviorRebateRepository;
import cn.zmj.domain.rebate.service.IBehaviorRebateService;
import cn.zmj.infrastructure.event.EventPublisher;
import cn.zmj.infrastructure.persistent.dao.IDailyBehaviorRebateDao;
import cn.zmj.infrastructure.persistent.dao.ITaskDao;
import cn.zmj.infrastructure.persistent.dao.IUserBehaviorRebateOrderDao;
import cn.zmj.infrastructure.persistent.po.DailyBehaviorRebate;
import cn.zmj.infrastructure.persistent.po.Task;
import cn.zmj.infrastructure.persistent.po.UserBehaviorRebateOrder;
import cn.zmj.types.enums.ResponseCode;
import cn.zmj.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
public class BehaviorRebateRepository implements IBehaviorRebateRepository {
    @Resource
    private IDBRouterStrategy dbRouter;
    @Resource
    private IDailyBehaviorRebateDao dailyBehaviorRebateDao;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private ITaskDao taskDao;
    @Resource
    private IUserBehaviorRebateOrderDao userBehaviorRebateOrderDao;
    @Resource
    private EventPublisher eventPublisher;
    @Override
    public List<DailyBehaviorRebateVO> queryDailyBehaviorRebateConfig(BehaviorTypeVO behaviorTypeVO) {
        List<DailyBehaviorRebate> dailyBehaviorRebates = dailyBehaviorRebateDao.queryDailyBehaviorRebateByBehaviorType(behaviorTypeVO.getCode());
        List<DailyBehaviorRebateVO> dailyBehaviorRebateVOS=new ArrayList<>(dailyBehaviorRebates.size());
        for (DailyBehaviorRebate dailyBehaviorRebate : dailyBehaviorRebates) {
            dailyBehaviorRebateVOS.add(DailyBehaviorRebateVO.builder()
                                       .behaviorType(dailyBehaviorRebate.getBehaviorType())
                                       .rebateDesc(dailyBehaviorRebate.getRebateDesc())
                                       .rebateType(dailyBehaviorRebate.getRebateType())
                                       .rebateConfig(dailyBehaviorRebate.getRebateConfig()).build());
        }
        return dailyBehaviorRebateVOS;
    }

    @Override
    public void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates) {
        try {
            dbRouter.doRouter(userId);

            transactionTemplate.execute(
                    status -> {
                        try {
                            for (BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregates) {
                                BehaviorRebateOrderEntity behaviorRebateOrderEntity = behaviorRebateAggregate.getBehaviorRebateOrderEntity();
                                //        用户行为返利订单对象
                                UserBehaviorRebateOrder userBehaviorRebateOrder=new UserBehaviorRebateOrder();
                                userBehaviorRebateOrder.setUserId(behaviorRebateOrderEntity.getUserId());
                                userBehaviorRebateOrder.setOrderId(behaviorRebateOrderEntity.getOrderId());
                                userBehaviorRebateOrder.setBehaviorType(behaviorRebateOrderEntity.getBehaviorType());
                                userBehaviorRebateOrder.setRebateDesc(behaviorRebateOrderEntity.getRebateDesc());
                                userBehaviorRebateOrder.setRebateType(behaviorRebateOrderEntity.getRebateType());
                                userBehaviorRebateOrder.setOutBusinessNo(behaviorRebateOrderEntity.getOutBusinessNo());
                                userBehaviorRebateOrder.setRebateConfig(behaviorRebateOrderEntity.getRebateConfig());
                                userBehaviorRebateOrder.setBizId(behaviorRebateOrderEntity.getBizId());
                                userBehaviorRebateOrderDao.insert(userBehaviorRebateOrder);
                                //        任务对象
                                TaskEntity taskEntity = behaviorRebateAggregate.getTaskEntity();
                                Task task=new Task();
                                task.setUserId(taskEntity.getUserId());
                                task.setTopic(taskEntity.getTopic());
                                task.setMessageId(taskEntity.getMessageId());
                                task.setMessage(JSON.toJSONString(taskEntity.getMessage()));
                                task.setState(taskEntity.getState().getCode());
                                taskDao.insert(task);
                            }
                            return 1;
                        } catch (DuplicateKeyException e) {
                            status.setRollbackOnly();
                            log.error("写入返利记录，唯一索引冲突 userId: {}", userId, e);
                            throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                        }
                    }
            );
        } finally {
            dbRouter.clear();
        }

//        同步发送MQ消息
        for (BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregates) {
            TaskEntity taskEntity=behaviorRebateAggregate.getTaskEntity();
            Task task=new Task();
            task.setUserId(taskEntity.getUserId());
            task.setMessageId(taskEntity.getMessageId());
            try {
                eventPublisher.publish(taskEntity.getTopic(),taskEntity.getMessage());
                taskDao.updateTaskSendMessageCompleted(task);
            } catch (Exception e) {
                log.error("写入返利记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
                taskDao.updateTaskSendMessageFail(task);
            }
        }

    }

    @Override
    public List<BehaviorRebateOrderEntity> queryOrderByOutBusinessNo(String userId, String outBusinessNo) {
//        请求对象
        UserBehaviorRebateOrder userBehaviorRebateOrderReq=new UserBehaviorRebateOrder();
        userBehaviorRebateOrderReq.setUserId(userId);
        userBehaviorRebateOrderReq.setOutBusinessNo(outBusinessNo);
//        查询结果
        List<UserBehaviorRebateOrder> userBehaviorRebateOrderResList=userBehaviorRebateOrderDao.queryOrderByOutBusinessNo(userBehaviorRebateOrderReq);
        List<BehaviorRebateOrderEntity> behaviorRebateOrderEntities=new ArrayList<>(userBehaviorRebateOrderResList.size());
        for (UserBehaviorRebateOrder userBehaviorRebateOrder : userBehaviorRebateOrderResList) {
            BehaviorRebateOrderEntity behaviorRebateOrderEntity=new BehaviorRebateOrderEntity();
            behaviorRebateOrderEntity.setUserId(userBehaviorRebateOrder.getUserId());
            behaviorRebateOrderEntity.setOrderId(userBehaviorRebateOrder.getOrderId());
            behaviorRebateOrderEntity.setBehaviorType(userBehaviorRebateOrder.getBehaviorType());
            behaviorRebateOrderEntity.setRebateDesc(userBehaviorRebateOrder.getRebateDesc());
            behaviorRebateOrderEntity.setRebateType(userBehaviorRebateOrder.getRebateType());
            behaviorRebateOrderEntity.setRebateConfig(userBehaviorRebateOrder.getRebateConfig());
            behaviorRebateOrderEntity.setBizId(userBehaviorRebateOrder.getBizId());
            behaviorRebateOrderEntities.add(behaviorRebateOrderEntity);
        }
        return behaviorRebateOrderEntities;


    }
}
