package org.group1418.easy.escm.common.config.properties;

import cn.dev33.satoken.config.SaCookieConfig;
import cn.dev33.satoken.config.SaSignConfig;
import cn.dev33.satoken.config.SaTokenConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author yq 2021/5/21 20:02
 * @description CustomMybatisPlusConfigProperties
 */
@ConfigurationProperties(prefix = "easy.escm")
@Data
@Component
@Slf4j
public class CustomConfigProperties {

    /**
     * 系统名称
     */
    private String name = "easy-escm";

    /**
     * 日志文件路径
     */
    private String logPath;
    /**
     * 日志打印级别
     */
    private String logLevel;

    /**
     * 数据库类型, 多个用逗号相隔
     */
    private String dbType = "mysql";

    /**
     * 新增用户的默认密码
     */
    private String userDefaultPassword = "123456";

    /**
     * 异步线程池配置, 自动注入spring上下文,系统中止自动关闭, 注入使用需配合@Lazy
     */
    private List<AsyncConfig> asyncConfigs;

    /**
     * sa-token 配置
     */
    private TokenConfig tokenConfig;

    @Data
    public static class AsyncConfig {
        /**
         * 线程池名称, 线程名称为 threadName_数字
         */
        private String threadName;
        /**
         * 核心线程数
         */
        private Integer coreSize = 2;
        /**
         * 线程池最大线程数
         */
        private Integer maxSize = 8;
        /**
         * 线程队列最大线程数
         */
        private Integer queueCapacity = 4096;
        /**
         * 线程池中线程最大空闲时间，默认：60，单位：秒
         */
        private Integer keepAliveSeconds = 60;
        /**
         * 核心线程是否允许超时，默认false
         */
        private boolean allowCoreThreadTimeout = true;
        /**
         * IOC容器关闭时是否阻塞等待剩余的任务执行完成，默认:false（必须设置setAwaitTerminationSeconds）
         */
        private boolean waitForTasksToCompleteOnShutdown = false;
        /**
         * 阻塞IOC容器关闭的时间，默认：10秒（必须设置setWaitForTasksToCompleteOnShutdown）
         */
        private int awaitTerminationSeconds = 10;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    public static class TokenConfig extends SaTokenConfig {

        /**
         * token 名称 （同时也是： cookie 名称、提交 token 时参数的名称、存储 token 时的 key 前缀）
         */
        private String tokenName = "easy-escm-token";

        /**
         * token 有效期（单位：秒） 默认30天，-1 代表永久有效
         */
        private long timeout = 60 * 60 * 24 * 30;

        /**
         * token 最低活跃频率（单位：秒），如果 token 超过此时间没有访问系统就会被冻结，默认-1 代表不限制，永不冻结
         * （例如可以设置为 1800 代表 30 分钟内无操作就冻结）
         */
        private long activeTimeout = -1;

        /**
         * 是否启用动态 activeTimeout 功能，如不需要请设置为 false，节省缓存请求次数
         */
        private Boolean dynamicActiveTimeout = false;

        /**
         * 是否允许同一账号多地同时登录 （为 true 时允许一起登录, 为 false 时新登录挤掉旧登录）
         */
        private Boolean isConcurrent = true;

        /**
         * 在多人登录同一账号时，是否共用一个 token （为 true 时所有登录共用一个 token, 为 false 时每次登录新建一个 token）
         */
        private Boolean isShare = true;

        /**
         * 同一账号最大登录数量，-1代表不限 （只有在 isConcurrent=true, isShare=false 时此配置项才有意义）
         */
        private int maxLoginCount = 12;

        /**
         * 在每次创建 token 时的最高循环次数，用于保证 token 唯一性（-1=不循环尝试，直接使用）
         */
        private int maxTryTimes = 12;

        /**
         * 是否尝试从请求体里读取 token
         */
        private Boolean isReadBody = true;

        /**
         * 是否尝试从 header 里读取 token
         */
        private Boolean isReadHeader = true;

        /**
         * 是否尝试从 cookie 里读取 token
         */
        private Boolean isReadCookie = true;

        /**
         * 是否在登录后将 token 写入到响应头
         */
        private Boolean isWriteHeader = false;

        /**
         * token 风格（默认可取值：uuid、simple-uuid、random-32、random-64、random-128、tik）
         */
        private String tokenStyle = "simple-uuid";

        /**
         * 默认 SaTokenDao 实现类中，每次清理过期数据间隔的时间（单位: 秒），默认值30秒，设置为 -1 代表不启动定时清理
         */
        private int dataRefreshPeriod = 30;

        /**
         * 获取 Token-Session 时是否必须登录（如果配置为true，会在每次获取 getTokenSession() 时校验当前是否登录）
         */
        private Boolean tokenSessionCheckLogin = true;

        /**
         * 是否打开自动续签 activeTimeout （如果此值为 true, 框架会在每次直接或间接调用 getLoginId() 时进行一次过期检查与续签操作）
         */
        private Boolean autoRenew = true;

        /**
         * token 前缀, 前端提交 token 时应该填写的固定前缀，格式样例(satoken: Bearer xxxx-xxxx-xxxx-xxxx)
         */
        private String tokenPrefix = "Bearer";

        /**
         * 是否在初始化配置时在控制台打印版本字符画
         */
        private Boolean isPrint = false;

        /**
         * 是否打印操作日志
         */
        private Boolean isLog = false;

        /**
         * 日志等级（trace、debug、info、warn、error、fatal），此值与 logLevelInt 联动
         */
        private String logLevel = "trace";

        /**
         * 日志等级 int 值（1=trace、2=debug、3=info、4=warn、5=error、6=fatal），此值与 logLevel 联动
         */
        private int logLevelInt = 1;

        /**
         * 是否打印彩色日志
         */
        private Boolean isColorLog = null;

        /**
         * jwt秘钥（只有集成 jwt 相关模块时此参数才会生效）
         */
        private String jwtSecretKey;

        /**
         * Http Basic 认证的默认账号和密码
         */
        private String basic = "";

        /**
         * 配置当前项目的网络访问地址
         */
        private String currDomain;

        /**
         * Same-Token 的有效期 (单位: 秒)
         */
        private long sameTokenTimeout = 60 * 60 * 24;

        /**
         * 是否校验 Same-Token（部分rpc插件有效）
         */
        private Boolean checkSameToken = false;

        /**
         * Cookie配置对象
         */
        public SaCookieConfig cookie = new SaCookieConfig();

        /**
         * API 签名配置对象
         */
        public SaSignConfig sign = new SaSignConfig();
    }
}
