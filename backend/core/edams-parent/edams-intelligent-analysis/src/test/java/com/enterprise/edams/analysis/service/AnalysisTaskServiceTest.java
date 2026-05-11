package com.enterprise.edams.analysis.service;

import com.enterprise.edams.analysis.dto.request.CreateTaskRequest;
import com.enterprise.edams.analysis.dto.response.TaskResponse;
import com.enterprise.edams.analysis.entity.AnalysisTask;
import com.enterprise.edams.analysis.entity.ExecutionMode;
import com.enterprise.edams.analysis.entity.TaskStatus;
import com.enterprise.edams.analysis.exception.AnalysisException;
import com.enterprise.edams.analysis.repository.AnalysisResultRepository;
import com.enterprise.edams.analysis.repository.AnalysisTaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisTaskServiceTest {

    @Mock
    private AnalysisTaskRepository taskRepository;

    @Mock
    private AnalysisResultRepository resultRepository;

    @Mock
    private IntelligentAnalyzerService analyzerService;

    @Mock
    private DatasourceScannerService scannerService;

    @InjectMocks
    private AnalysisTaskService taskService;

    private AnalysisTask testTask;
    private CreateTaskRequest createRequest;

    @BeforeEach
    void setUp() {
        testTask = AnalysisTask.builder()
                .id(1L)
                .taskCode("TASK-20260511001")
                .taskName("测试任务")
                .datasourceId(1L)
                .modelConfigId(1L)
                .status(TaskStatus.PENDING)
                .executionMode(ExecutionMode.MANUAL)
                .batchSize(5)
                .enableLineageAnalysis(true)
                .enableIndicatorExtraction(true)
                .enableSubjectClassification(true)
                .autoRegister(false)
                .sampleRowCount(100)
                .build();

        createRequest = CreateTaskRequest.builder()
                .taskName("测试任务")
                .datasourceId(1L)
                .modelConfigId(1L)
                .executionMode(ExecutionMode.MANUAL)
                .batchSize(5)
                .enableLineageAnalysis(true)
                .enableIndicatorExtraction(true)
                .enableSubjectClassification(true)
                .autoRegister(false)
                .sampleRowCount(100)
                .build();
    }

    @Test
    void testCreateTask_Success() {
        when(taskRepository.save(any(AnalysisTask.class))).thenReturn(testTask);

        TaskResponse response = taskService.createTask(createRequest);

        assertNotNull(response);
        assertEquals("测试任务", response.getTaskName());
        assertEquals(TaskStatus.PENDING, response.getStatus());
        verify(taskRepository, times(1)).save(any(AnalysisTask.class));
    }

    @Test
    void testGetTask_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        TaskResponse response = taskService.getTask(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("TASK-20260511001", response.getTaskCode());
    }

    @Test
    void testGetTask_NotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AnalysisException.class, () -> {
            taskService.getTask(999L);
        });
    }

    @Test
    void testStartTask_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        assertDoesNotThrow(() -> taskService.startTask(1L));

        verify(taskRepository, times(1)).save(any(AnalysisTask.class));
    }

    @Test
    void testStartTask_AlreadyRunning() {
        testTask.setStatus(TaskStatus.RUNNING);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        assertThrows(AnalysisException.class, () -> {
            taskService.startTask(1L);
        });
    }

    @Test
    void testCancelTask_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        assertDoesNotThrow(() -> taskService.cancelTask(1L));

        verify(taskRepository, times(1)).save(any(AnalysisTask.class));
    }

    @Test
    void testCancelTask_AlreadyCompleted() {
        testTask.setStatus(TaskStatus.COMPLETED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        assertThrows(AnalysisException.class, () -> {
            taskService.cancelTask(1L);
        });
    }
}
