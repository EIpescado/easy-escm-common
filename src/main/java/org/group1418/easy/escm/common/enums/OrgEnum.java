package org.group1418.easy.escm.common.enums;

import cn.hutool.core.util.StrUtil;

import java.util.Arrays;

/**
 * 组织枚举
 *
 * @author yq 2021年3月29日 14:49:18
 */
public enum OrgEnum {
    /**
     * 1418工作室
     */
    STUDIO_1418("1418工作室", "1418"),
    ;

    String organizationName;
    String companyId;

    OrgEnum(String organizationName, String companyId) {
        this.organizationName = organizationName;
        this.companyId = companyId;
    }

    public String getCompanyId() {
        return companyId;
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
                        || oe.getCompanyId().equals(org)
                        || oe.getOrganizationName().equals(org)
                )
                .findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return organizationName;
    }
}
