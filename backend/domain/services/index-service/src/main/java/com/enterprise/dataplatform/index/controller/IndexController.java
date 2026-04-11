package com.enterprise.dataplatform.index.controller;

import com.enterprise.dataplatform.index.document.AssetIndexDocument;
import com.enterprise.dataplatform.index.dto.request.SearchRequest;
import com.enterprise.dataplatform.index.dto.response.SearchResponse;
import com.enterprise.dataplatform.index.service.IndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索索引REST控制器
 *
 * @author EDAMS Team
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "全文搜索", description = "数据资产的全文搜索、索引管理")
public class IndexController {

    private final IndexService indexService;

    @GetMapping
    @Operation(summary = "全文搜索", description = "多字段全文搜索，支持类型/域/状态过滤")
    public ResponseEntity<Map<String, Object>> search(SearchRequest request) {
        SearchResponse response = indexService.search(request);
        return ResponseEntity.ok(wrapResponse(response));
    }

    @GetMapping("/suggest")
    @Operation(summary = "搜索建议", description = "输入提示/自动补全")
    public ResponseEntity<Map<String, Object>> suggest(
            @RequestParam String keyword) {
        List<String> suggestions = indexService.suggest(keyword);
        return ResponseEntity.ok(wrapResponse(suggestions));
    }

    @PostMapping("/index")
    @Operation(summary = "索引单个资产")
    public ResponseEntity<Map<String, Object>> indexAsset(@RequestBody AssetIndexDocument doc) {
        indexService.indexAsset(doc);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapResponse("索引成功"));
    }

    @DeleteMapping("/index/{assetId}")
    @Operation(summary = "从索引删除资产")
    public ResponseEntity<Map<String, Object>> deleteIndex(@PathVariable String assetId) {
        indexService.deleteIndex(assetId);
        return ResponseEntity.ok(wrapResponse("删除成功"));
    }

    @PostMapping("/reindex")
    @Operation(summary = "重建全量索引")
    public ResponseEntity<Map<String, Object>> reindexAll() {
        indexService.reindexAll();
        return ResponseEntity.ok(wrapResponse("全量索引重建已启动"));
    }

    private Map<String, Object> wrapResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }
}
