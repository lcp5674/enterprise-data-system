package com.enterprise.edams.lifecycle.service;

import com.enterprise.edams.lifecycle.dto.ArchivePolicyCreateRequest;
import com.enterprise.edams.lifecycle.dto.ArchivePolicyDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 归档策略服务测试
 *
 * @author EDAMS Team
 */
@SpringBootTest
@ActiveProfiles("test")
class ArchivePolicyServiceTest {

    @Autowired
    private ArchivePolicyService archivePolicyService;

    @Test
    void testCreateArchivePolicy() {
        ArchivePolicyCreateRequest request = new ArchivePolicyCreateRequest();
        request.setName("测试归档策略");
        request.setCode("test-archive-policy");
        request.setDescription("这是一个测试归档策略");
        request.setBusinessType("data_asset");
        request.setDataCategory("sensitive");
        request.setRetentionDays(365);
        request.setTriggerType(1);
        request.setArchiveTarget(1);
        request.setCompressionType(2);
        request.setEncryptionType(1);
        request.setDeleteSource(false);
        request.setPriority(1);

        ArchivePolicyDTO result = archivePolicyService.createPolicy(request);

        assertNotNull(result);
        assertEquals("测试归档策略", result.getName());
        assertEquals("test-archive-policy", result.getCode());
        assertEquals(365, result.getRetentionDays());
        assertTrue(result.getEnabled());
    }

    @Test
    void testGetArchivePolicy() {
        ArchivePolicyCreateRequest request = new ArchivePolicyCreateRequest();
        request.setName("测试获取策略");
        request.setCode("test-get-policy");
        request.setRetentionDays(180);
        request.setTriggerType(1);
        request.setArchiveTarget(1);

        ArchivePolicyDTO created = archivePolicyService.createPolicy(request);
        ArchivePolicyDTO retrieved = archivePolicyService.getPolicy(created.getId());

        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals(created.getName(), retrieved.getName());
    }

    @Test
    void testEnableDisablePolicy() {
        ArchivePolicyCreateRequest request = new ArchivePolicyCreateRequest();
        request.setName("测试启用禁用策略");
        request.setCode("test-enable-policy");
        request.setRetentionDays(90);
        request.setTriggerType(1);
        request.setArchiveTarget(1);

        ArchivePolicyDTO created = archivePolicyService.createPolicy(request);
        assertTrue(created.getEnabled());

        archivePolicyService.disablePolicy(created.getId());
        ArchivePolicyDTO disabled = archivePolicyService.getPolicy(created.getId());
        assertFalse(disabled.getEnabled());

        archivePolicyService.enablePolicy(created.getId());
        ArchivePolicyDTO enabled = archivePolicyService.getPolicy(created.getId());
        assertTrue(enabled.getEnabled());
    }
}
