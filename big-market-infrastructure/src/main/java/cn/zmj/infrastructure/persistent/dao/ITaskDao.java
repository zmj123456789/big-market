package cn.zmj.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.zmj.infrastructure.persistent.po.Task;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author 29622
* @description 针对表【task(任务表，发送MQ)】的数据库操作Mapper
* @createDate 2025-07-09 10:34:50
* @Entity cn.zmj.infrastructure.persistent.po.Task
*/
@Mapper
public interface ITaskDao {

    List<Task> queryNoSendMessageTaskList();
    @DBRouter
    void updateTaskSendMessageCompleted(Task task);

    @DBRouter
    void updateTaskSendMessageFail(Task task);

    void insert(Task task);
}




