package org.group1418.easy.escm.common.utils;

import ch.qos.logback.core.PropertyDefinerBase;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author yq 2024/2/21 11:17
 * @description LogIpConverter
 */
@Slf4j
public class LogIpDefiner extends PropertyDefinerBase {

    private static String webIP;

    private String getIp() {
        if (StrUtil.isBlank(webIP)) {
            try {
                webIP = InetAddress.getLocalHost().getHostAddress();
                log.info("当前系统IP[{}]", webIP);
            } catch (UnknownHostException e) {
                log.error("获取日志Ip异常[{}]", e.getLocalizedMessage());
                webIP = null;
            }
        }
        return webIP;
    }

    @Override
    public String getPropertyValue() {
        return getIp();
    }
}
