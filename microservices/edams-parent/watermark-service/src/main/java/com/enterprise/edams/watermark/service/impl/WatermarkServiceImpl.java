package com.enterprise.edams.watermark.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.watermark.dto.*;
import com.enterprise.edams.watermark.entity.*;
import com.enterprise.edams.watermark.mapper.*;
import com.enterprise.edams.watermark.service.WatermarkService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 水印服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class WatermarkServiceImpl extends ServiceImpl<WatermarkTemplateMapper, WatermarkTemplate>
        implements WatermarkService {
    
    private final WatermarkRecordMapper recordMapper;
    private final LeakTraceMapper leakTraceMapper;
    
    @Value("${minio.endpoint}")
    private String minioEndpoint;
    
    @Value("${minio.access-key}")
    private String minioAccessKey;
    
    @Value("${minio.secret-key}")
    private String minioSecretKey;
    
    @Value("${minio.bucket}")
    private String minioBucket;
    
    @Override
    public WatermarkRecord addWatermark(AddWatermarkRequest request) {
        log.info("添加水印: assetId={}, type={}", request.getAssetId(), request.getWatermarkType());
        
        long startTime = System.currentTimeMillis();
        
        WatermarkRecord record = WatermarkRecord.builder()
                .recordNo(generateRecordNo())
                .assetId(request.getAssetId())
                .assetName(request.getAssetName())
                .fileType(request.getFileType())
                .originalPath(request.getFilePath())
                .watermarkType(request.getWatermarkType())
                .templateId(request.getTemplateId())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .deptId(request.getDeptId())
                .watermarkContent(request.getWatermarkContent())
                .status(WatermarkStatus.PROCESSING)
                .build();
        
        try {
            // 实际项目中应该调用水印处理服务
            // 这里简化处理，模拟水印添加
            String watermarkedPath = processWatermark(request);
            
            record.setWatermarkedPath(watermarkedPath);
            record.setStatus(WatermarkStatus.SUCCESS);
            record.setProcessTime(System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            log.error("添加水印失败", e);
            record.setStatus(WatermarkStatus.FAILED);
            record.setProcessTime(System.currentTimeMillis() - startTime);
        }
        
        recordMapper.insert(record);
        return record;
    }
    
    @Override
    public List<WatermarkRecord> batchAddWatermark(BatchWatermarkRequest request) {
        List<WatermarkRecord> results = new ArrayList<>();
        for (AddWatermarkRequest item : request.getRequests()) {
            try {
                WatermarkRecord record = addWatermark(item);
                results.add(record);
            } catch (Exception e) {
                log.error("批量添加水印失败: assetId={}", item.getAssetId(), e);
            }
        }
        return results;
    }
    
    @Override
    public WatermarkRecord getRecord(Long id) {
        return recordMapper.selectById(id);
    }
    
    @Override
    public Page<WatermarkRecord> searchRecords(RecordSearchRequest request) {
        Page<WatermarkRecord> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<WatermarkRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getAssetId() != null) {
            wrapper.eq(WatermarkRecord::getAssetId, request.getAssetId());
        }
        if (request.getUserId() != null) {
            wrapper.eq(WatermarkRecord::getUserId, request.getUserId());
        }
        if (request.getWatermarkType() != null) {
            wrapper.eq(WatermarkRecord::getWatermarkType, request.getWatermarkType());
        }
        if (request.getStatus() != null) {
            wrapper.eq(WatermarkRecord::getStatus, request.getStatus());
        }
        
        wrapper.orderByDesc(WatermarkRecord::getCreatedTime);
        return recordMapper.selectPage(page, wrapper);
    }
    
    @Override
    public WatermarkTemplate createTemplate(TemplateCreateRequest request) {
        WatermarkTemplate template = WatermarkTemplate.builder()
                .templateCode(generateTemplateCode())
                .templateName(request.getTemplateName())
                .watermarkType(request.getWatermarkType())
                .content(request.getContent())
                .opacity(request.getOpacity() != null ? request.getOpacity() : 0.3)
                .fontSize(request.getFontSize() != null ? request.getFontSize() : 12)
                .fontColor(request.getFontColor() != null ? request.getFontColor() : "#CCCCCC")
                .rotation(request.getRotation() != null ? request.getRotation() : -30)
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .repeatable(request.getRepeatable() != null ? request.getRepeatable() : true)
                .status(TemplateStatus.ACTIVE)
                .description(request.getDescription())
                .build();
        
        this.save(template);
        return template;
    }
    
    @Override
    public WatermarkTemplate updateTemplate(Long id, TemplateUpdateRequest request) {
        WatermarkTemplate template = this.getById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在: " + id);
        }
        
        if (request.getTemplateName() != null) template.setTemplateName(request.getTemplateName());
        if (request.getContent() != null) template.setContent(request.getContent());
        if (request.getOpacity() != null) template.setOpacity(request.getOpacity());
        if (request.getFontSize() != null) template.setFontSize(request.getFontSize());
        if (request.getFontColor() != null) template.setFontColor(request.getFontColor());
        if (request.getRotation() != null) template.setRotation(request.getRotation());
        if (request.getStatus() != null) template.setStatus(request.getStatus());
        if (request.getDescription() != null) template.setDescription(request.getDescription());
        
        this.updateById(template);
        return template;
    }
    
    @Override
    public List<WatermarkTemplate> getTemplates(WatermarkType type) {
        LambdaQueryWrapper<WatermarkTemplate> wrapper = new LambdaQueryWrapper<WatermarkTemplate>()
                .eq(WatermarkTemplate::getStatus, TemplateStatus.ACTIVE);
        
        if (type != null) {
            wrapper.eq(WatermarkTemplate::getWatermarkType, type);
        }
        
        return this.list(wrapper);
    }
    
    @Override
    public LeakTrace traceLeakage(String fileName, String watermarkContent) {
        log.info("泄露溯源: fileName={}, content={}", fileName, watermarkContent);
        
        // 解析水印内容，提取用户信息
        String userName = extractUserName(watermarkContent);
        String dept = extractDepartment(watermarkContent);
        
        LeakTrace trace = LeakTrace.builder()
                .traceNo(generateTraceNo())
                .fileName(fileName)
                .leakType(LeakType.SUSPECTED_LEAK)
                .discoveryChannel("系统检测")
                .discoveryTime(LocalDateTime.now())
                .suspectUserName(userName)
                .suspectDept(dept)
                .watermarkContent(watermarkContent)
                .confidence(0.85)
                .status(TraceStatus.REPORTED)
                .build();
        
        leakTraceMapper.insert(trace);
        return trace;
    }
    
    @Override
    public Page<LeakTrace> searchLeakTraces(LeakTraceSearchRequest request) {
        Page<LeakTrace> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<LeakTrace> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getStatus() != null) {
            wrapper.eq(LeakTrace::getStatus, request.getStatus());
        }
        if (request.getLeakType() != null) {
            wrapper.eq(LeakTrace::getLeakType, request.getLeakType());
        }
        if (request.getSuspectUserId() != null) {
            wrapper.eq(LeakTrace::getSuspectUserId, request.getSuspectUserId());
        }
        
        wrapper.orderByDesc(LeakTrace::getDiscoveryTime);
        return leakTraceMapper.selectPage(page, wrapper);
    }
    
    @Override
    public LeakStatistics getStatistics() {
        long total = leakTraceMapper.selectCount(null);
        long investigating = leakTraceMapper.selectCount(
                new LambdaQueryWrapper<LeakTrace>()
                        .eq(LeakTrace::getStatus, TraceStatus.INVESTIGATING));
        long confirmed = leakTraceMapper.selectCount(
                new LambdaQueryWrapper<LeakTrace>()
                        .eq(LeakTrace::getStatus, TraceStatus.CONFIRMED));
        long disposed = leakTraceMapper.selectCount(
                new LambdaQueryWrapper<LeakTrace>()
                        .eq(LeakTrace::getStatus, TraceStatus.DISPOSED));
        
        return LeakStatistics.builder()
                .totalCases(total)
                .investigating(investigating)
                .confirmed(confirmed)
                .disposed(disposed)
                .resolutionRate(total > 0 ? (double) disposed / total * 100 : 100)
                .build();
    }
    
    // ============ 私有方法 ============
    
    private String processWatermark(AddWatermarkRequest request) {
        // 实际项目中应该调用图片/PDF处理服务添加水印
        // 这里返回模拟的输出路径
        return "watermarked/" + request.getFilePath();
    }
    
    private String extractUserName(String watermarkContent) {
        // 从水印内容中提取用户名
        if (watermarkContent.contains("用户:")) {
            int start = watermarkContent.indexOf("用户:") + 3;
            int end = watermarkContent.indexOf(";", start);
            return end > start ? watermarkContent.substring(start, end) : watermarkContent.substring(start);
        }
        return "未知";
    }
    
    private String extractDepartment(String watermarkContent) {
        // 从水印内容中提取部门
        if (watermarkContent.contains("部门:")) {
            int start = watermarkContent.indexOf("部门:") + 3;
            int end = watermarkContent.indexOf(";", start);
            return end > start ? watermarkContent.substring(start, end) : watermarkContent.substring(start);
        }
        return "未知";
    }
    
    private String generateRecordNo() {
        return "WM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
    
    private String generateTemplateCode() {
        return "TPL" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateTraceNo() {
        return "TRACE" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
