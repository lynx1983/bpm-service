package com.example.bpm.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingListener;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingResult;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.externaltask.LockedExternalTaskImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;

@Slf4j
public class LockExternalTaskByIdCmd implements Command<List<LockedExternalTask>> {

    List<String> ids;
    String workerId;
    long lockDuration;
    List<String> variablesToFetch;

    public LockExternalTaskByIdCmd() {
        variablesToFetch = new ArrayList<>();
    }

    @Override
    public List<LockedExternalTask> execute(CommandContext commandContext) {

        List<LockedExternalTask> list = new ArrayList<>();

        // validate input
        if (!validateInput()) {
            throw new IllegalArgumentException("Invalid parameters for locking tasks");
        }
        // lock each task from lost
        for (String id : ids) {
            ExternalTaskEntity entity = commandContext.getExternalTaskManager().findExternalTaskById(id);
            // check if task is already locked
            if (entity.getLockExpirationTime() == null || entity.getLockExpirationTime().before(ClockUtil.getCurrentTime())) {
                entity.lock(workerId, lockDuration);
                // add task to list
                list.add(LockedExternalTaskImpl.fromEntity(entity, variablesToFetch, false, false, false));
            }
        }

        // add optimistic locking listener to confirm camunda entity management
        filterOnOptimisticLockingFailure(commandContext, list);

        return list;
    }

    /**
     * filter task ist with optimistic looking to remove not existing tasks from
     * list
     *
     * @param commandContext
     *            the command conext given from the executor
     * @param tasks
     *            the task list
     */
    private void filterOnOptimisticLockingFailure(CommandContext commandContext, final List<LockedExternalTask> tasks) {
        commandContext.getDbEntityManager().registerOptimisticLockingListener(new OptimisticLockingListener() {

            public Class<? extends DbEntity> getEntityType() {
                return ExternalTaskEntity.class;
            }

            public OptimisticLockingResult failedOperation(DbOperation operation) {
                if (operation instanceof DbEntityOperation) {
                    DbEntityOperation dbEntityOperation = (DbEntityOperation) operation;
                    DbEntity dbEntity = dbEntityOperation.getEntity();

                    boolean failedOperationEntityInList = false;

                    Iterator<LockedExternalTask> it = tasks.iterator();
                    while (it.hasNext()) {
                        LockedExternalTask resultTask = it.next();
                        if (resultTask.getId().equals(dbEntity.getId())) {
                            it.remove();
                            failedOperationEntityInList = true;
                            break;
                        }
                    }

                    if (!failedOperationEntityInList) {
                        log.error("" + operation);
                    }
                }

                return OptimisticLockingResult.IGNORE;
            }
        });
    }

    public LockExternalTaskByIdCmd setIds(List<String> ids) {
        this.ids = ids;
        return this;
    }

    public LockExternalTaskByIdCmd setWorkerId(String workerId) {
        this.workerId = workerId;
        return this;
    }

    public LockExternalTaskByIdCmd setLockDuration(Integer lockDuration) {
        this.lockDuration = lockDuration;
        return this;
    }

    public boolean validateInput() {
        return ids.stream().anyMatch(StringUtils::isNoneEmpty) && StringUtils.isNotEmpty(workerId)
                && lockDuration > 0;
    }
}
