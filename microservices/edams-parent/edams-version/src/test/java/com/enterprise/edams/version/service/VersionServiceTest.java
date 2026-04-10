package com.enterprise.edams.version.service;

import com.enterprise.edams.version.entity.VersionRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 版本服务测试
 *
 * @author EDAMS Team
 */
@SpringBootTest
@ActiveProfiles("test")
class VersionServiceTest {

    @Autowired
    private VersionService versionService;

    @Test
    void testCreateVersion() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "测试数据");
        data.put("value", 100);

        VersionRecord result = versionService.createVersion(
                "data_asset", "asset-001", data, "v1.0", "初始版本", "user-001", "测试用户"
        );

        assertNotNull(result);
        assertEquals("data_asset", result.getBusinessType());
        assertEquals("asset-001", result.getBusinessId());
        assertEquals(1, result.getVersion());
        assertEquals("v1.0", result.getVersionTag());
        assertEquals("user-001", result.getCreatedBy());
    }

    @Test
    void testGetVersionsByBusiness() {
        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "测试数据1");
        
        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", "测试数据2");

        versionService.createVersion("data_asset", "asset-002", data1, "v1.0", "版本1", "user-001", "测试用户");
        versionService.createVersion("data_asset", "asset-002", data2, "v2.0", "版本2", "user-001", "测试用户");

        List<VersionRecord> versions = versionService.getVersionsByBusiness("data_asset", "asset-002");

        assertNotNull(versions);
        assertEquals(2, versions.size());
    }

    @Test
    void testGetLatestVersion() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "最新数据");

        versionService.createVersion("data_asset", "asset-003", data, "v1.0", "版本1", "user-001", "测试用户");
        versionService.createVersion("data_asset", "asset-003", data, "v2.0", "版本2", "user-001", "测试用户");

        VersionRecord latest = versionService.getLatestVersion("data_asset", "asset-003");

        assertNotNull(latest);
        assertEquals(2, latest.getVersion());
    }
}
