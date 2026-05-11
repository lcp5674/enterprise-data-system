package com.enterprise.edams.analysis.controller;

import com.enterprise.edams.analysis.dto.request.CreateTaskRequest;
import com.enterprise.edams.analysis.dto.request.TaskQueryDTO;
import com.enterprise.edams.analysis.dto.request.UpdateTaskRequest;
import com.enterprise.edams.analysis.dto.response.AnalysisResultResponse;
import com.enterprise.edams.analysis.dto.response.TaskProgressVO;
import com.enterprise.edams.analysis.dto.response.TaskResponse;
import com.enterprise.edams.analysis.service.AnalysisTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/analysis/tasks")
@RequiredArgsConstructor
public class AnalysisTaskController {

    private final AnalysisTaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request) {
        log.info("Creating analysis task: {}", request.getTaskName());
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> listTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String executionMode,
            @RequestParam(required = false) Long datasourceId,
            @RequestParam(required = false) String executor,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.debug("Listing tasks with filters");
        
        TaskQueryDTO query = TaskQueryDTO.builder()
                .keyword(keyword)
                .status(status)
                .executionMode(executionMode)
                .datasourceId(datasourceId)
                .executor(executor)
                .page(page)
                .size(size)
                .build();

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TaskResponse> tasks = taskService.listTasks(query, pageRequest);
        
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {
        log.debug("Getting task: {}", id);
        TaskResponse task = taskService.getTask(id);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        log.info("Updating task: {}", id);
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("Deleting task: {}", id);
        taskService.cancelTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> startTask(@PathVariable Long id) {
        log.info("Starting task: {}", id);
        taskService.startTask(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<Void> pauseTask(@PathVariable Long id) {
        log.info("Pausing task: {}", id);
        taskService.pauseTask(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<Void> resumeTask(@PathVariable Long id) {
        log.info("Resuming task: {}", id);
        taskService.resumeTask(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTask(@PathVariable Long id) {
        log.info("Cancelling task: {}", id);
        taskService.cancelTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<TaskProgressVO> getTaskProgress(@PathVariable Long id) {
        log.debug("Getting task progress: {}", id);
        TaskProgressVO progress = taskService.getTaskProgress(id);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<Page<AnalysisResultResponse>> getTaskResults(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.debug("Getting task results: {}", id);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "analyzedAt"));
        Page<AnalysisResultResponse> results = taskService.getTaskResults(id, pageRequest);
        
        return ResponseEntity.ok(results);
    }
}
