package com.enterprise.edams.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.knowledge.config.KnowledgeConfig;
import com.enterprise.edams.knowledge.dto.DataImportDTO;
import com.enterprise.edams.knowledge.dto.DataImportResultDTO;
import com.enterprise.edams.knowledge.dto.GraphNodeDTO;
import com.enterprise.edams.knowledge.entity.GraphNode;
import com.enterprise.edams.knowledge.entity.KnowledgeGraph;
import com.enterprise.edams.knowledge.mapper.GraphNodeMapper;
import com.enterprise.edams.knowledge.mapper.KnowledgeGraphMapper;
import com.enterprise.edams.knowledge.service.DataImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据导入服务实现类
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportServiceImpl implements DataImportService {

    private final KnowledgeGraphMapper knowledgeGraphMapper;
    private final GraphNodeMapper graphNodeMapper;
    private final KnowledgeConfig knowledgeConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String IMPORT_STATUS_PREFIX = "knowledge:import:status:";
    private final Map<String, Boolean> cancelFlags = new ConcurrentHashMap<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataImportResultDTO importData(DataImportDTO dto) {
        log.info("开始导入数据: graphId={}, type={}", dto.getGraphId(), dto.getImportType());

        String taskId = UUID.randomUUID().toString().replace("-", "");
        long startTime = System.currentTimeMillis();

        DataImportResultDTO result = DataImportResultDTO.builder()
                .taskId(taskId)
                .graphId(dto.getGraphId())
                .status("RUNNING")
                .totalRecords(0L)
                .successCount(0L)
                .failureCount(0L)
                .skippedCount(0L)
                .newNodes(0L)
                .updatedNodes(0L)
                .newEdges(0L)
                .updatedEdges(0L)
                .startTime(LocalDateTime.now())
                .creator(dto.getNodeConfig().getExtraProperties().get("creator").toString())
                .errors(new ArrayList<>())
                .build();

        try {
            // 保存状态到Redis
            saveImportStatus(result);

            // 根据导入类型执行导入
            switch (dto.getImportType().toUpperCase()) {
                case "CSV" -> result = importFromCSV(dto, result);
                case "EXCEL" -> result = importFromExcel(dto, result);
                case "JSON" -> result = importFromJSON(dto, result);
                default -> throw new IllegalArgumentException("不支持的导入类型: " + dto.getImportType());
            }

            result.setStatus("SUCCESS");
            result.setEndTime(LocalDateTime.now());
            result.setDuration(System.currentTimeMillis() - startTime);

            // 更新图谱统计
            updateGraphStatistics(dto.getGraphId());

        } catch (Exception e) {
            log.error("数据导入失败", e);
            result.setStatus("FAILED");
            result.setDetails("导入失败: " + e.getMessage());
            result.setEndTime(LocalDateTime.now());
            result.setDuration(System.currentTimeMillis() - startTime);
            result.getErrors().add(DataImportResultDTO.ImportError.builder()
                    .rowNumber(0)
                    .errorType("SYSTEM")
                    .errorMessage(e.getMessage())
                    .build());
        }

        saveImportStatus(result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataImportResultDTO importFromFile(String graphId, String nodeType, MultipartFile file) {
        String filename = file.getOriginalFilename();
        String extension = filename != null ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "";

        DataImportDTO dto = DataImportDTO.builder()
                .graphId(graphId)
                .importType(extension.equals("xlsx") || extension.equals("xls") ? "EXCEL" : "CSV")
                .fileUrl(filename)
                .nodeConfig(DataImportDTO.NodeImportConfig.builder()
                        .nodeType(nodeType)
                        .nameField("name")
                        .build())
                .options(DataImportDTO.ImportOptions.builder()
                        .deduplicate(true)
                        .updateExisting(true)
                        .batchSize(knowledgeConfig.getImportConfig().getBatchSize())
                        .build())
                .build();

        return importData(dto);
    }

    @Override
    public DataImportResultDTO getImportStatus(String taskId) {
        String key = IMPORT_STATUS_PREFIX + taskId;
        Object status = redisTemplate.opsForValue().get(key);

        if (status != null) {
            return objectMapper.convertValue(status, DataImportResultDTO.class);
        }

        return DataImportResultDTO.builder()
                .taskId(taskId)
                .status("NOT_FOUND")
                .build();
    }

    @Override
    public void cancelImport(String taskId) {
        cancelFlags.put(taskId, true);
        log.info("导入任务已标记取消: {}", taskId);
    }

    @Override
    public DataImportResultDTO importFromDatabase(String graphId, String datasourceId, String sql,
                                                   String nodeType, String nameField) {
        // 实现数据库导入逻辑
        log.info("从数据库导入: graphId={}, datasourceId={}", graphId, datasourceId);

        // 实际实现中需要连接数据源执行SQL查询
        // 这里返回占位结果
        return DataImportResultDTO.builder()
                .taskId(UUID.randomUUID().toString().replace("-", ""))
                .graphId(graphId)
                .status("NOT_IMPLEMENTED")
                .details("数据库导入功能待实现")
                .build();
    }

    private DataImportResultDTO importFromCSV(DataImportDTO dto, DataImportResultDTO result) throws IOException {
        log.info("从CSV导入数据");

        // 解析CSV内容
        List<Map<String, String>> records = parseCSV(dto.getDataContent(), dto.getNodeConfig());

        return processRecords(dto, result, records);
    }

    private DataImportResultDTO importFromExcel(DataImportDTO dto, DataImportResultDTO result) throws IOException {
        log.info("从Excel导入数据");

        // 解析Excel内容
        List<Map<String, String>> records = parseExcel(dto.getDataContent(), dto.getNodeConfig());

        return processRecords(dto, result, records);
    }

    private DataImportResultDTO importFromJSON(DataImportDTO dto, DataImportResultDTO result) {
        log.info("从JSON导入数据");

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = objectMapper.readValue(
                    dto.getDataContent(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            List<Map<String, String>> stringRecords = records.stream()
                    .map(m -> {
                        Map<String, String> converted = new HashMap<>();
                        m.forEach((k, v) -> converted.put(k, v != null ? v.toString() : null));
                        return converted;
                    })
                    .toList();

            return processRecords(dto, result, stringRecords);
        } catch (Exception e) {
            throw new RuntimeException("JSON解析失败", e);
        }
    }

    private DataImportResultDTO processRecords(DataImportDTO dto, DataImportResultDTO result,
                                                   List<Map<String, String>> records) {
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong failureCount = new AtomicLong(0);
        AtomicLong skippedCount = new AtomicLong(0);
        AtomicLong newNodes = new AtomicLong(0);
        AtomicLong updatedNodes = new AtomicLong(0);

        int batchSize = dto.getOptions().getBatchSize();
        String nodeType = dto.getNodeConfig().getNodeType();
        String nameField = dto.getNodeConfig().getNameField();
        Map<String, String> fieldMapping = dto.getNodeConfig().getFieldMapping();
        Map<String, Object> extraProperties = dto.getNodeConfig().getExtraProperties();

        result.setTotalRecords((long) records.size());

        // 批量处理
        for (int i = 0; i < records.size(); i += batchSize) {
            // 检查取消标记
            if (Boolean.TRUE.equals(cancelFlags.get(result.getTaskId()))) {
                result.setStatus("CANCELLED");
                break;
            }

            int end = Math.min(i + batchSize, records.size());
            List<Map<String, String>> batch = records.subList(i, end);

            for (int j = 0; j < batch.size(); j++) {
                Map<String, String> record = batch.get(j);
                int rowNum = i + j + 1;

                try {
                    String nodeName = record.get(nameField);
                    if (nodeName == null || nodeName.isEmpty()) {
                        failureCount.incrementAndGet();
                        result.getErrors().add(DataImportResultDTO.ImportError.builder()
                                .rowNumber(rowNum)
                                .errorType("VALIDATION")
                                .errorMessage("节点名称不能为空")
                                .recordData(record.toString())
                                .build());
                        continue;
                    }

                    // 检查是否已存在
                    List<GraphNode> existing = graphNodeMapper.selectByNameLike(dto.getGraphId(), nodeName);

                    if (!existing.isEmpty() && dto.getOptions().getDeduplicate()) {
                        if (dto.getOptions().getUpdateExisting()) {
                            // 更新已存在的节点
                            GraphNode existingNode = existing.get(0);
                            updateNodeProperties(existingNode, record, fieldMapping, extraProperties);
                            graphNodeMapper.updateById(existingNode);
                            updatedNodes.incrementAndGet();
                        } else {
                            skippedCount.incrementAndGet();
                        }
                    } else {
                        // 创建新节点
                        GraphNode node = createNode(dto.getGraphId(), nodeType, nodeName, record,
                                fieldMapping, extraProperties);
                        graphNodeMapper.insert(node);
                        newNodes.incrementAndGet();
                    }

                    successCount.incrementAndGet();

                } catch (Exception e) {
                    log.error("处理记录失败: row={}", rowNum, e);
                    failureCount.incrementAndGet();
                    result.getErrors().add(DataImportResultDTO.ImportError.builder()
                            .rowNumber(rowNum)
                            .errorType("PROCESSING")
                            .errorMessage(e.getMessage())
                            .recordData(record.toString())
                            .build());
                }
            }

            // 更新进度
            result.setSuccessCount(successCount.get());
            result.setFailureCount(failureCount.get());
            result.setSkippedCount(skippedCount.get());
            result.setNewNodes(newNodes.get());
            result.setUpdatedNodes(updatedNodes.get());
            saveImportStatus(result);
        }

        return result;
    }

    private List<Map<String, String>> parseCSV(String content, DataImportDTO.NodeImportConfig config) {
        List<Map<String, String>> records = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(
                new java.io.ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))))) {

            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) return records;

            String[] headers = allRows.get(0);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].trim(), i);
            }

            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                Map<String, String> record = new HashMap<>();

                for (String header : headers) {
                    Integer idx = headerIndex.get(header);
                    if (idx != null && idx < row.length) {
                        record.put(header.trim(), row[idx]);
                    }
                }

                records.add(record);
            }
        } catch (IOException | CsvException e) {
            throw new RuntimeException("CSV解析失败", e);
        }

        return records;
    }

    private List<Map<String, String>> parseExcel(String content, DataImportDTO.NodeImportConfig config) {
        List<Map<String, String>> records = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(
                new java.io.ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return records;

            String[] headers = new String[headerRow.getLastCellNum()];
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                headers[i] = getCellValue(cell);
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> record = new HashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    Cell cell = row.getCell(j);
                    record.put(headers[j], getCellValue(cell));
                }

                records.add(record);
            }
        } catch (IOException e) {
            throw new RuntimeException("Excel解析失败", e);
        }

        return records;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private GraphNode createNode(String graphId, String nodeType, String nodeName,
                                  Map<String, String> record, Map<String, String> fieldMapping,
                                  Map<String, Object> extraProperties) {
        GraphNode node = new GraphNode();
        node.setNodeId(UUID.randomUUID().toString().replace("-", ""));
        node.setGraphId(graphId);
        node.setName(nodeName);
        node.setNodeType(nodeType);
        node.setLabels(nodeType);
        node.setStatus("ACTIVE");
        node.setTenantId(extraProperties.get("tenantId") != null ?
                extraProperties.get("tenantId").toString() : null);
        node.setCreator(extraProperties.get("creator") != null ?
                extraProperties.get("creator").toString() : null);
        node.setDataSource("IMPORT");
        node.setQualityScore(1.0);

        // 转换属性
        Map<String, Object> properties = new HashMap<>();
        fieldMapping.forEach((k, v) -> {
            if (record.containsKey(k)) {
                properties.put(v, record.get(k));
            }
        });
        extraProperties.forEach(properties::put);

        node.setProperties(toJson(properties));

        return node;
    }

    private void updateNodeProperties(GraphNode node, Map<String, String> record,
                                        Map<String, String> fieldMapping,
                                        Map<String, Object> extraProperties) {
        Map<String, Object> properties = parseJson(node.getProperties());
        fieldMapping.forEach((k, v) -> {
            if (record.containsKey(k)) {
                properties.put(v, record.get(k));
            }
        });
        node.setProperties(toJson(properties));
    }

    private void updateGraphStatistics(String graphId) {
        Long nodeCount = graphNodeMapper.countByGraphId(graphId);
        KnowledgeGraph graph = knowledgeGraphMapper.selectByGraphId(graphId);
        if (graph != null) {
            graph.setNodeCount(nodeCount);
            knowledgeGraphMapper.updateById(graph);
        }
    }

    private void saveImportStatus(DataImportResultDTO result) {
        String key = IMPORT_STATUS_PREFIX + result.getTaskId();
        redisTemplate.opsForValue().set(key, result);
        redisTemplate.expire(key, 24 * 60 * 60); // 24小时过期
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON转换失败", e);
            return null;
        }
    }

    private Map<String, Object> parseJson(String json) {
        if (json == null) return new HashMap<>();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("JSON解析失败", e);
            return new HashMap<>();
        }
    }
}
