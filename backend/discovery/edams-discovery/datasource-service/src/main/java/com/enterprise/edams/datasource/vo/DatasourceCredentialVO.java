package com.enterprise.edams.datasource.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 数据源凭证VO
 */
@Data
@SuperBuilder
public class DatasourceCredentialVO {

    /**
     * 凭证ID
     */
    private Long id;

    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * 凭证类型
     */
    private String credentialType;

    /**
     * 凭证类型描述
     */
    private String credentialTypeDesc;

    /**
     * 密钥库名称
     */
    private String keyStoreName;

    /**
     * 密钥标识
     */
    private String keyIdentifier;

    /**
     * 密钥版本
     */
    private String keyVersion;

    /**
     * 凭证状态
     */
    private String status;

    /**
     * 凭证状态描述
     */
    private String statusDesc;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 最后轮换时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastRotatedTime;

    /**
     * 下次轮换时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextRotationTime;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
