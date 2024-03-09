package org.group1418.easy.escm.common.saToken;


import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import org.group1418.easy.escm.common.saToken.obj.CurrentUser;

import java.util.function.Supplier;

/**
 * @author yq
 * @date 2024年3月7日 17:38:34
 * @description
 * @since V1.0.0 当前登录用户上下文
 */
public class LoginHelper {

    public static final String LOGIN_USER_KEY = "loginUser";
    public static final String TENANT_ID_KEY = "tenantId";
    public static final String USER_ID_KEY = "userId";
    public static final String CLIENT_ID_KEY = "clientId";
    public static final String TENANT_ADMIN_KEY = "beTenantAdmin";
    public static final String SUPER_ADMIN_KEY = "beSuperAdmin";

    /**
     * 登录系统 基于 设备类型
     * 针对相同用户体系不同设备
     *
     * @param currentUser 登录用户信息
     * @param model     配置参数
     */
    public static void login(CurrentUser currentUser, SaLoginModel model) {
        SaStorage storage = SaHolder.getStorage();
        storage.set(LOGIN_USER_KEY, currentUser);
        storage.set(TENANT_ID_KEY, currentUser.getTenantId());
        storage.set(USER_ID_KEY, currentUser.getId());
        model = ObjectUtil.defaultIfNull(model, new SaLoginModel());
        StpUtil.login(currentUser.getId(),
                model.setExtra(TENANT_ID_KEY, currentUser.getTenantId())
                        .setExtra(USER_ID_KEY, currentUser.getId()));
        StpUtil.getTokenSession().set(LOGIN_USER_KEY, currentUser);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getStorageIfAbsentSet(String key, Supplier<T> handle) {
        try {
            SaStorage storage = SaHolder.getStorage();
            Object obj = storage.get(key);
            if (ObjectUtil.isNull(obj)) {
                obj = handle.get();
                storage.set(key, obj);
            }
            return (T) obj;
        } catch (Exception e) {
            return null;
        }
    }
}
