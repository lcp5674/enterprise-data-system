package com.enterprise.edams.version.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.version.entity.VersionRecord;

import java.util.List;
import java.util.Map;

/**
 * 版本服务接口
 *
 * @author EDAMS Team
 */
public interface VersionService extends IService<VersionRecord> {

    /**
     * 创建版本
     */
    VersionRecord createVersion(String businessType, String businessId, Map<String, Object> dataContent,
                                String versionTag, String versionComment, String userId, String userName);

    /**
     * 获取版本详情
     */
    VersionRecord getVersion(String id);

    /**
     * 查询业务的所有版本
     */
    List<VersionRecord> getVersionsByBusiness(String businessType, String businessId);

    /**
     * 获取最新版本
     */
    VersionRecord getLatestVersion(String businessType, String businessId);

    /**
     * 回滚到指定版本
     */
    Map<String, Object> rollbackVersion(String versionId, String userId, String userName);

    /**
     * 对比两个版本
     */
    Map<String, Object> compareVersions(String versionId1, String versionId2);

    /**
     * 删除版本
     */
    void deleteVersion(String id);
}
