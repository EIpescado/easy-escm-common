package org.group1418.easy.escm.common.utils;

import cn.hutool.core.lang.func.VoidFunc0;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author yq 2023/11/2 9:31
 * @description DbUtil 数据库,事务相关工具
 */
public class DbUtil {

    /**
     * 注册事务回调方法
     * @param synchronization 回调对象
     */
    public static void registerSynchronization(TransactionSynchronization synchronization) {
        TransactionSynchronizationManager.registerSynchronization(synchronization);
    }

    /**
     * 事务提交后回调
     * @param voidFunc0 处理方法
     */
    public static void afterTransactionCommit(VoidFunc0 voidFunc0) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                voidFunc0.callWithRuntimeException();
            }
        });
    }
}
