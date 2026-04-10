package com.enterprise.edams.workflow.service;

import com.enterprise.edams.workflow.dto.ProcessDefinitionCreateRequest;
import com.enterprise.edams.workflow.dto.ProcessDefinitionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 流程定义服务测试
 *
 * @author EDAMS Team
 */
@SpringBootTest
@ActiveProfiles("test")
class ProcessDefinitionServiceTest {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Test
    void testCreateProcessDefinition() {
        ProcessDefinitionCreateRequest request = new ProcessDefinitionCreateRequest();
        request.setProcessKey("test-process");
        request.setName("测试流程");
        request.setDescription("这是一个测试流程");
        request.setCategory("test");

        ProcessDefinitionDTO result = processDefinitionService.createProcessDefinition(request);

        assertNotNull(result);
        assertEquals("test-process", result.getProcessKey());
        assertEquals("测试流程", result.getName());
        assertEquals(1, result.getVersion());
        assertEquals(0, result.getStatus());
        assertTrue(result.getIsLatest());
    }

    @Test
    void testGetProcessDefinition() {
        ProcessDefinitionCreateRequest request = new ProcessDefinitionCreateRequest();
        request.setProcessKey("test-get-process");
        request.setName("测试获取流程");

        ProcessDefinitionDTO created = processDefinitionService.createProcessDefinition(request);
        ProcessDefinitionDTO retrieved = processDefinitionService.getProcessDefinition(created.getId());

        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals(created.getProcessKey(), retrieved.getProcessKey());
    }
}
