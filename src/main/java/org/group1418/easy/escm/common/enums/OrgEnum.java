package org.group1418.easy.escm.common.enums;

import cn.hutool.core.util.StrUtil;

import java.util.Arrays;

/**
 * 组织枚举
 * @author yq 2021年3月29日 14:49:18
 */
public enum OrgEnum {
    /**华富洋*/
    HFY("深圳市华富洋供应链有限公司","sjd7ryZIQBKruyU6goHVqsznrtQ="),
    /**智慧*/
    ZH("深圳市华富洋智慧供应链有限公司","QJ4NY93jRGmQZI78ABBjS8znrtQ="),
    /**hope sea*/
    HOPESEA("HOPE SEA IMPORT & EXPORT LIMITED","Ilu0FHDaTwK+qv5oOZ9gysznrtQ="),
    /**上海华富洋*/
    SHFY("上海华富洋供应链管理有限公司","e/fpw+KMRgSuRUHA6KBjvcznrtQ="),
    /**深圳电商*/
    HFYDS("深圳市华富洋电商有限公司","4ZZMy6+ZSGWkETwGA5StUsznrtQ="),
    /**北京华富洋*/
    BJHFY("北京华富洋供应链管理有限公司","srTA4lk3RUyhDq1ae9MX7cznrtQ="),
    /**深圳华富洋投资*/
    HFYTZ("深圳市华富洋投资顾问有限公司","uVD34d37SS+hzFY3CsnFw8znrtQ="),
    ;

    String organizationName;
    String easCompanyId;

    OrgEnum(String organizationName, String easCompanyId) {
        this.organizationName = organizationName;
        this.easCompanyId = easCompanyId;
    }

    public String getEasCompanyId() {
        return easCompanyId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public static OrgEnum parse(String org) {
        if (StrUtil.isBlank(org)) {
            return null;
        }
        return Arrays.stream(OrgEnum.values())
                .filter(oe -> oe.name().equalsIgnoreCase(org)
                        || oe.getEasCompanyId().equals(org)
                        || oe.getOrganizationName().equals(org)
                )
                .findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return organizationName;
    }
}
