package com.enterprise.edams.datasource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 数据源凭证实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("datasource_credential")
public class DatasourceCredential extends BaseEntity {

    /**
     * 数据源ID
     */
    private String datasourceId;

    /**
     * 凭证类型：USERNAME_PASSWORD/KEYTAB/TOKEN/VAULT
     */
    private String credentialType;

    /**
     * 凭证Key
     */
    private String credentialKey;

    /**
     * 加密后的凭证值
     */
    private String credentialValueEnc;

    /**
     * Vault路径
     */
    private String vaultPath;

    /**
     * Vault版本
     */
    private Integer vaultVersion;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 是否激活
     */
    private Integer isActive;
}
