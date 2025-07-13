package cn.zmj.infrastructure.persistent.repository;

import cn.zmj.domain.task.model.entity.TaskEntity;
import cn.zmj.domain.task.repository.ITaskRepository;
import cn.zmj.infrastructure.event.EventPublisher;
import cn.zmj.infrastructure.persistent.dao.ITaskDao;
import cn.zmj.infrastructure.persistent.po.Task;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 任务服务仓储实现
 * @create 2024-04-06 10:57
 */
@Repository
public class TaskRepository implements ITaskRepository {
    @Resource
    private ITaskDao taskDao;
    @Resource
    private EventPublisher eventPublisher;
    @Override
    public List<TaskEntity> queryNoSendMessageTaskList() {
        List<Task> tasks=taskDao.queryNoSendMessageTaskList();
        List<TaskEntity> tasksEntity=new ArrayList<>();
        for (Task task : tasks) {
            TaskEntity taskEntity = TaskEntity.builder()
                        .userId(task.getUserId())
                        .topic(task.getTopic())
                        .message(task.getMessage())
                        .messageId(task.getMessageId())
                        .build();
            tasksEntity.add(taskEntity);
        }
        return tasksEntity;
    }

    @Override
    public void sendMessage(TaskEntity taskEntity) {
        eventPublisher.publish(taskEntity.getTopic(),taskEntity.getMessage());

    }

    @Override
    public void updateTaskSendMessageCompleted(String userId, String messageId) {
        Task taskReq=new Task();
        taskReq.setUserId(userId);
        taskReq.setMessageId(messageId);
        taskDao.updateTaskSendMessageCompleted(taskReq);
    }

    @Override
    public void updateTaskSendMessageFail(String userId, String messageId) {
        Task taskReq=new Task();
        taskReq.setUserId(userId);
        taskReq.setMessageId(messageId);
        taskDao.updateTaskSendMessageFail(taskReq);
    }
}
