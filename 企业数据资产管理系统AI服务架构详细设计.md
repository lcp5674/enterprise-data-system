# 企业数据资产管理系统AI服务架构详细设计

**文档版本**：V1.0
**编制日期**：2026年4月
**编制单位**：企业数据资产管理项目组

---

## 1 AI服务架构概述

### 1.1 AI能力全景

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          AI服务能力全景                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                      大语言模型服务 (LLM)                         │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────┐  │   │
│  │  │ GPT-4   │ │ Claude-3 │ │ 文心一言 │ │ 通义千问 │ │ DeepSeek│  │   │
│  │  │ GPT-3.5 │ │ Claude-haiku│ │ 智谱GLM │ │ 混元   │ │ Kimi   │  │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └────────┘  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                      AI应用服务层                                  │   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐            │   │
│  │  │ 智能注释生成 │ │ 智能搜索增强 │ │ 智能问答系统 │            │   │
│  │  │ Auto-Tag    │ │ RAG搜索      │ │ QA Bot       │            │   │
│  │  └──────────────┘ └──────────────┘ └──────────────┘            │   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐            │   │
│  │  │ 智能分类分级 │ │ 使用助手     │ │ 趋势预测     │            │   │
│  │  │ Auto-Classify│ │ Assistant    │ │ Forecasting  │            │   │
│  │  └──────────────┘ └──────────────┘ └──────────────┘            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                      支撑服务层                                    │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐            │   │
│  │  │ 向量数据库│ │ 提示词工程│ │ 模型路由  │ │ 缓存管理 │            │   │
│  │  │ Milvus   │ │ Prompt   │ │ Router   │ │ Cache    │            │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘            │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐            │   │
│  │  │ 监控告警  │ │ 成本控制  │ │ 限流熔断  │ │ 结果评估 │            │   │
│  │  │ Monitor  │ │ CostCtrl  │ │ RateLimit│ │ Evaluator│            │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.2 AI服务架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          AI服务架构图                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                        AI服务网关层                                │   │
│  │  ┌─────────────────────────────────────────────────────────┐     │   │
│  │  │              AI Gateway (Spring Cloud Gateway)            │     │   │
│  │  │  • 请求路由 • 认证鉴权 • 限流熔断 • 请求日志              │     │   │
│  │  └─────────────────────────────────────────────────────────┘     │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│                                    ▼                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                       AI服务编排层                                  │   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐            │   │
│  │  │ LLM Orchestrator │ │ Prompt Manager │ │ Chain Manager │        │   │
│  │  └──────────────┘ └──────────────┘ └──────────────┘            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│                                    ▼                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                       模型适配层                                   │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐     │   │
│  │  │OpenAI   │ │Claude   │ │百度文心  │ │阿里通义  │ │DeepSeek │     │   │
│  │  │Adapter  │ │Adapter  │ │Adapter  │ │Adapter  │ │Adapter  │     │   │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘     │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│                                    ▼                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                       模型服务层                                   │   │
│  │  ┌─────────────────────────────────────────────────────────┐     │   │
│  │  │     云端模型服务          │      本地模型服务            │     │   │
│  │  │  ┌──────────┐          │      ┌──────────┐            │     │   │
│  │  │  │ GPT-4   │          │      │ Llama2   │            │     │   │
│  │  │  │ Claude-3│          │      │ Qwen-72B │            │     │   │
│  │  │  │ 文心一言 │          │      │ ChatGLM  │            │     │   │
│  │  │  └──────────┘          │      └──────────┘            │     │   │
│  │  └─────────────────────────────────────────────────────────┘     │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2 大模型适配层设计

### 2.1 模型适配器接口

```java
// 模型适配器接口定义
public interface LLMAdapter {
    
    /**
     * 获取适配的模型名称
     */
    String getModelName();
    
    /**
     * 获取模型提供商
     */
    String getProvider();
    
    /**
     * 文本生成
     */
    LLMResponse generate(LLMRequest request);
    
    /**
     * 流式文本生成
     */
    Flux<String> generateStream(LLMRequest request);
    
    /**
     * 检查模型是否可用
     */
    boolean isAvailable();
    
    /**
     * 获取模型元信息
     */
    ModelMetadata getMetadata();
}

// 统一的请求/响应格式
@Data
public class LLMRequest {
    private String prompt;
    private String systemPrompt;
    private List<Message> messages;
    private Double temperature;
    private Integer maxTokens;
    private Double topP;
    private List<String> stop;
    private Map<String, Object> extraParams;
}

@Data
public class LLMResponse {
    private String content;
    private String model;
    private String finishReason;
    private Integer promptTokens;
    private Integer completionTokens;
    private Long latencyMs;
    private String error;
}
```

### 2.2 模型适配器实现

#### OpenAI适配器

```java
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAIAdapter implements LLMAdapter {
    
    private final RestTemplate restTemplate;
    private final OpenAIProperties properties;
    
    @Override
    public String getModelName() {
        return properties.getModel();
    }
    
    @Override
    public String getProvider() {
        return "openai";
    }
    
    @Override
    public LLMResponse generate(LLMRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getApiKey());
            
            Map<String, Object> body = buildRequestBody(request);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                properties.getBaseUrl() + "/chat/completions",
                entity,
                Map.class
            );
            
            return parseResponse(response.getBody(), System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            return buildErrorResponse(e, System.currentTimeMillis() - startTime);
        }
    }
    
    private Map<String, Object> buildRequestBody(LLMRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", properties.getModel());
        
        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        
        if (StringUtils.hasText(request.getSystemPrompt())) {
            messages.add(Map.of("role", "system", "content", request.getSystemPrompt()));
        }
        
        if (request.getMessages() != null) {
            request.getMessages().forEach(msg -> 
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()))
            );
        } else if (StringUtils.hasText(request.getPrompt())) {
            messages.add(Map.of("role", "user", "content", request.getPrompt()));
        }
        
        body.put("messages", messages);
        
        // 可选参数
        if (request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            body.put("max_tokens", request.getMaxTokens());
        }
        if (request.getTopP() != null) {
            body.put("top_p", request.getTopP());
        }
        if (request.getStop() != null) {
            body.put("stop", request.getStop());
        }
        
        return body;
    }
}
```

#### DeepSeek适配器

```java
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "deepseek")
public class DeepSeekAdapter implements LLMAdapter {
    
    private final RestTemplate restTemplate;
    private final DeepSeekProperties properties;
    
    @Override
    public String getModelName() {
        return properties.getModel();  // deepseek-chat
    }
    
    @Override
    public String getProvider() {
        return "deepseek";
    }
    
    @Override
    public LLMResponse generate(LLMRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getApiKey());
            
            Map<String, Object> body = buildRequestBody(request);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.deepseek.com/chat/completions",
                entity,
                Map.class
            );
            
            return parseResponse(response.getBody(), System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            return buildErrorResponse(e, System.currentTimeMillis() - startTime);
        }
    }
}
```

### 2.3 模型选择策略

```yaml
# 模型选择策略配置
ai:
  # 默认模型
  defaultProvider: deepseek
  defaultModel: deepseek-chat
  
  # 模型成本配置
  models:
    - name: gpt-4-turbo-preview
      provider: openai
      inputCost: 10.0    # $/1M tokens
      outputCost: 30.0
      latency: 3.0       # 秒
      contextWindow: 128000
      quality: high
      
    - name: gpt-3.5-turbo
      provider: openai
      inputCost: 0.5
      outputCost: 1.5
      latency: 1.0
      contextWindow: 16385
      quality: medium
      
    - name: claude-3-opus
      provider: anthropic
      inputCost: 15.0
      outputCost: 75.0
      latency: 4.0
      contextWindow: 200000
      quality: highest
      
    - name: deepseek-chat
      provider: deepseek
      inputCost: 1.0     # RMB/1M tokens
      outputCost: 2.0
      latency: 1.5
      contextWindow: 32768
      quality: high
      chineseSupport: excellent
      
    - name: qwen-turbo
      provider: alibaba
      inputCost: 0.8
      outputCost: 2.0
      latency: 1.2
      contextWindow: 8192
      quality: medium
      chineseSupport: excellent
  
  # 智能路由策略
  routing:
    strategy: quality-cost-balance  # quality-first | cost-first | balanced
    
    # 按场景选择模型
    sceneRouting:
      intelligent_annotation:
        high: gpt-4-turbo-preview
        medium: deepseek-chat
        low: qwen-turbo
        
      quality_analysis:
        high: claude-3-opus
        medium: gpt-4-turbo-preview
        low: deepseek-chat
        
      general_qa:
        high: gpt-3.5-turbo
        medium: deepseek-chat
        low: qwen-turbo
```

---

## 3 提示词工程设计

### 3.1 提示词模板管理

```yaml
# 提示词模板配置
prompts:
  # 资产注释生成
  asset_annotation:
    system: |
      你是一名数据资产管理专家，负责为数据资产生成高质量的注释和描述。
      你的职责是：
      1. 分析资产的名称、类型、数据结构
      2. 理解资产的业务含义和使用场景
      3. 生成简洁、准确、有价值的注释
      
    user_template: |
      请为以下数据资产生成注释：
      
      资产名称：{{asset_name}}
      资产类型：{{asset_type}}
      所属数据库：{{database}}
      所属Schema：{{schema}}
      
      字段信息：
      {{#each fields}}
      - {{field_name}} ({{data_type}}): {{description}}
      {{/each}}
      
      请生成：
      1. 资产整体描述（100-200字）
      2. 业务用途说明
      3. 使用建议和注意事项
      
  # 血缘解读
  lineage_interpretation:
    system: |
      你是一名数据血缘分析专家，负责解读复杂的数据血缘关系。
      重点关注：
      1. 数据流转路径
      2. 关键转换节点
      3. 潜在风险点
      4. 优化建议
      
  # 质量分析
  quality_analysis:
    system: |
      你是一名数据质量专家，负责分析数据质量问题并提供改进建议。
      分析维度：
      1. 完整性（空值、缺失）
      2. 准确性（错误值、异常值）
      3. 一致性（格式、编码）
      4. 及时性（延迟、更新频率）
      
  # 智能问答
  qa:
    system: |
      你是一个数据资产管理助手，基于提供的数据资产信息回答用户问题。
      回答要求：
      1. 准确基于给定的资产信息
      2. 如信息不足，明确说明
      3. 提供可操作的建议
      4. 保持专业但友好的语气
```

### 3.2 提示词模板代码实现

```java
@Component
public class PromptTemplateEngine {
    
    private final TemplateEngine templateEngine;
    
    public String render(String templateName, Map<String, Object> context) {
        Template template = templateEngine.getTemplate(templateName);
        return template.render(Context.of(context));
    }
    
    // 构建资产注释请求
    public LLMRequest buildAnnotationRequest(Asset asset, List<Field> fields) {
        Map<String, Object> context = new HashMap<>();
        context.put("asset_name", asset.getName());
        context.put("asset_type", asset.getType());
        context.put("database", asset.getDatabase());
        context.put("schema", asset.getSchema());
        context.put("fields", fields.stream()
            .map(f -> Map.of(
                "field_name", f.getName(),
                "data_type", f.getDataType(),
                "description", f.getComment() != null ? f.getComment() : "无"
            ))
            .collect(Collectors.toList()));
        
        return LLMRequest.builder()
            .systemPrompt(render("prompts.asset_annotation.system", context))
            .prompt(render("prompts.asset_annotation.user_template", context))
            .temperature(0.3)
            .maxTokens(1000)
            .build();
    }
}
```

---

## 4 RAG检索增强设计

### 4.1 RAG架构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          RAG检索增强架构                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Phase 1: 索引构建                                                      │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                                                                  │  │
│  │  数据源 ──▶ 文档解析 ──▶ 分块处理 ──▶ 向量化 ──▶ 索引存储       │  │
│  │                              │                                   │  │
│  │                   ┌───────────┴───────────┐                     │  │
│  │                   ▼                       ▼                     │  │
│  │              块元数据                 向量数据                    │  │
│  │              (MySQL)                (Milvus)                    │  │
│  │                                                                  │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                    │                                   │
│                                    ▼                                   │
│  Phase 2: 检索查询                                                      │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                                                                  │  │
│  │  用户查询 ──▶ 向量化 ──▶ 向量检索 ──▶ 重排序 ──▶ 上下文构建      │  │
│  │                         │                                        │  │
│  │               ┌─────────┴─────────┐                              │  │
│  │               ▼                   ▼                              │  │
│  │          Milvus               MySQL                              │  │
│  │         (向量)               (元数据)                             │  │
│  │                                                                  │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                    │                                   │
│                                    ▼                                   │
│  Phase 3: 生成回答                                                      │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                                                                  │  │
│  │  上下文 ──▶ Prompt组装 ──▶ LLM生成 ──▶ 回答输出                │  │
│  │                                                                  │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4.2 文档分块策略

```yaml
# 文档分块配置
chunking:
  # 分块策略
  strategies:
    - name: fixed_size
      description: 固定大小分块
      chunkSize: 512
      overlap: 50
      
    - name: semantic
      description: 语义分块（按段落）
      minChunkSize: 100
      maxChunkSize: 1000
      separator: "\n\n"
      
    - name: recursive
      description: 递归分块（标题->段落->句子）
      separators:
        - "\n\n"    # 段落
        - "\n"      # 行
        - "。"      # 句子
        - ","       # 短语
      
  # 元数据提取
  metadata:
    extraction:
      - field: asset_id
        source: path
        pattern: "asset_(\d+)"
      - field: asset_type
        source: path
        pattern: "/(\w+)/"
      - field: created_at
        source: filename
        pattern: "(\d{8})"
        
  # 向量化配置
  embedding:
    model: text-embedding-ada-002
    dimension: 1536
    batchSize: 100
```

### 4.3 向量检索实现

```java
@Service
public class VectorRetrievalService {
    
    private final MilvusClient milvusClient;
    private final EmbeddingClient embeddingClient;
    
    @Value("${ai.vector.collection.name:asset_knowledge}")
    private String collectionName;
    
    @Value("${ai.vector.topK:5}")
    private int topK;
    
    /**
     * 语义检索
     */
    public List<检索结果> semanticSearch(String query, String assetType) {
        // 1. 查询向量化
        List<Float> queryEmbedding = embeddingClient.embed(query);
        
        // 2. 向量检索
        SearchParameters searchParams = SearchParameters.newBuilder()
            .withCollectionName(collectionName)
            .withVector(queryEmbedding)
            .withTopK(topK)
            .withMetricType(MetricType.IP)  // 内积相似度
            .build();
        
        SearchResults searchResults = milvusClient.search(searchParams);
        
        // 3. 结果过滤和组装
        return searchResults.getResults().stream()
            .filter(r -> filterByAssetType(r, assetType))
            .map(r -> build检索结果(r, queryEmbedding))
            .collect(Collectors.toList());
    }
    
    /**
     * 混合检索（向量+关键词）
     */
    public List<检索结果> hybridSearch(String query, String assetType) {
        // 1. 向量检索
        List<检索结果> vectorResults = semanticSearch(query, assetType);
        
        // 2. 关键词检索
        List<检索结果> keywordResults = keywordSearch(query, assetType);
        
        // 3. RRF融合排序
        return rrfFusion(vectorResults, keywordResults, k: 60);
    }
    
    /**
     * RRF融合算法
     */
    private List<检索结果> rrfFusion(List<检索结果>... resultLists) {
        Map<String, Double> scores = new HashMap<>();
        
        for (List<检索结果> results : resultLists) {
            for (int i = 0; i < results.size(); i++) {
                String docId = results.get(i).getDocId();
                double rrfScore = 1.0 / (60 + i + 1);
                scores.merge(docId, rrfScore, Double::sum);
            }
        }
        
        return scores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(e -> getDocument(e.getKey()))
            .limit(topK)
            .collect(Collectors.toList());
    }
}
```

---

## 5 多轮对话管理

### 5.1 对话上下文管理

```java
@Service
public class ConversationService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${ai.conversation.ttl:3600}")
    private long conversationTtl;
    
    /**
     * 创建新对话
     */
    public String createConversation(String userId, String systemPrompt) {
        String conversationId = UUID.randomUUID().toString();
        
        ConversationContext context = new ConversationContext();
        context.setConversationId(conversationId);
        context.setUserId(userId);
        context.setSystemPrompt(systemPrompt);
        context.setMessages(new ArrayList<>());
        context.setCreatedAt(LocalDateTime.now());
        
        String key = "conversation:" + conversationId;
        redisTemplate.opsForValue().set(key, context, conversationTtl, TimeUnit.SECONDS);
        
        return conversationId;
    }
    
    /**
     * 添加消息
     */
    public void addMessage(String conversationId, Message message) {
        String key = "conversation:" + conversationId;
        ConversationContext context = (ConversationContext) redisTemplate.opsForValue().get(key);
        
        if (context == null) {
            throw new IllegalArgumentException("Conversation not found");
        }
        
        context.getMessages().add(message);
        
        // 限制历史消息数量
        if (context.getMessages().size() > 20) {
            // 保留系统提示 + 最近20条消息
            List<Message> recentMessages = context.getMessages().subList(
                context.getMessages().size() - 20, 
                context.getMessages().size()
            );
            context.setMessages(recentMessages);
        }
        
        redisTemplate.opsForValue().set(key, context, conversationTtl, TimeUnit.SECONDS);
    }
    
    /**
     * 获取对话上下文
     */
    public ConversationContext getConversation(String conversationId) {
        String key = "conversation:" + conversationId;
        return (ConversationContext) redisTemplate.opsForValue().get(key);
    }
}
```

### 5.2 对话状态机

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          对话状态机                                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│                              ┌──────────┐                               │
│                              │  INIT   │                               │
│                              └────┬─────┘                               │
│                                   │                                      │
│                    ┌─────────────┼─────────────┐                        │
│                    │             │             │                        │
│                    ▼             ▼             ▼                        │
│              ┌──────────┐  ┌──────────┐  ┌──────────┐                   │
│              │PROCESSING│  │ STREAMING│  │  WAITING │                   │
│              └────┬─────┘  └────┬─────┘  └────┬─────┘                   │
│                   │             │             │                          │
│                   └─────────────┴─────────────┘                          │
│                                │                                          │
│                                ▼                                          │
│                         ┌──────────┐                                     │
│                         │COMPLETED│                                     │
│                         └────┬─────┘                                     │
│                              │                                           │
│                              ▼                                           │
│                       ┌──────────┐                                      │
│                       │ TIMEOUT  │                                      │
│                       └────┬─────┘                                      │
│                              │                                           │
│                              ▼                                           │
│                       ┌──────────┐                                      │
│                       │  ERROR   │                                      │
│                       └──────────┘                                      │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 6 AI服务高可用设计

### 6.1 熔断降级策略

```java
@Component
public class AILoadBalancer {
    
    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    
    /**
     * 模型熔断器配置
     */
    @Bean
    public Map<String, CircuitBreakerConfig> modelCircuitBreakerConfigs() {
        Map<String, CircuitBreakerConfig> configs = new HashMap<>();
        
        configs.put("openai", CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slowCallRateThreshold(80)
            .slowCallDurationThreshold(Duration.ofSeconds(10))
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .build());
        
        configs.put("deepseek", CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slowCallRateThreshold(80)
            .slowCallDurationThreshold(Duration.ofSeconds(5))
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .build());
        
        return configs;
    }
    
    /**
     * 智能路由
     */
    public LLMAdapter selectModel(LLMRequest request, List<LLMAdapter> adapters) {
        // 1. 过滤可用模型
        List<LLMAdapter> availableAdapters = adapters.stream()
            .filter(this::isAvailable)
            .filter(this::meetsRequirements(request))
            .collect(Collectors.toList());
        
        if (availableAdapters.isEmpty()) {
            throw new AIException("No available model");
        }
        
        // 2. 按策略选择
        String strategy = aiProperties.getRouting().getStrategy();
        
        return switch (strategy) {
            case "quality-first" -> selectByQuality(availableAdapters);
            case "cost-first" -> selectByCost(availableAdapters);
            default -> selectByBalance(availableAdapters);
        };
    }
}
```

### 6.2 模型降级链路

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          模型降级链路                                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  用户请求                                                               │
│      │                                                                  │
│      ▼                                                                  │
│  ┌─────────┐  不可用   ┌─────────┐  不可用   ┌─────────┐  不可用  ┌───┐│
│  │ GPT-4   │─────────▶│Claude-3 │─────────▶│DeepSeek │─────────▶│Qwen││
│  │(最高质量)│          │(高质量) │          │(中高)   │          │    ││
│  └─────────┘          └─────────┘          └─────────┘          └─┬──┘│
│      │                    │                    │                  │    │
│      ▼                    ▼                    ▼                  ▼    │
│  响应成功               响应成功             响应成功           响应成功 │
│                                                                         │
│  降级条件:                                                              │
│  • 连续失败3次                                                         │
│  • 响应超时30秒                                                        │
│  • 错误率>10%                                                          │
│                                                                         │
│  降级响应:                                                              │
│  • 质量降级，用户感知延迟略增                                           │
│  • 返回友好提示: "当前使用备用模型，回复可能略有差异"                   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 7 AI服务监控与成本控制

### 7.1 监控指标

```yaml
# AI服务监控配置
ai:
  monitoring:
    # 指标采集
    metrics:
      enabled: true
      interval: 60  # 秒
      
    # 监控指标
    indicators:
      - name: request_count
        type: counter
        labels: [provider, model, scene]
        
      - name: request_latency
        type: histogram
        buckets: [0.5, 1, 2, 5, 10, 30, 60]
        labels: [provider, model]
        
      - name: error_rate
        type: gauge
        labels: [provider, model]
        
      - name: token_usage
        type: counter
        labels: [provider, model, type]  # type: input/output
        
      - name: cost_amount
        type: counter
        labels: [provider, model]
        
      - name: circuit_breaker_status
        type: gauge
        labels: [provider]
```

### 7.2 成本控制

```yaml
# AI成本控制配置
ai:
  cost:
    # 预算控制
    budget:
      dailyLimit: 10000      # 每日限额（美元）
      monthlyLimit: 200000   # 每月限额
      
    # 告警阈值
    alerts:
      dailyUsagePercent: 80   # 80%告警
      monthlyUsagePercent: 80
      
    # 自动熔断
    autoCutoff:
      enabled: true
      threshold: 95           # 95%预算时自动降级到免费模型
      
    # Token限制
    tokenLimits:
      maxInputTokens: 8192
      maxOutputTokens: 4096
      truncateStrategy: middle  # 截断策略：head | middle | tail
```

---

## 8 AI应用场景实现

### 8.1 智能注释生成

```java
@Service
public class IntelligentAnnotationService {
    
    private final LLMOrchestrator llmOrchestrator;
    private final VectorRetrievalService vectorRetrieval;
    
    /**
     * 生成资产注释
     */
    public Annotation generateAnnotation(Asset asset, List<Field> fields) {
        // 1. 检索相似资产的注释作为参考
        List<检索结果> similarAssets = vectorRetrieval.hybridSearch(
            asset.getName(), asset.getType());
        
        // 2. 构建提示词
        PromptTemplate template = PromptTemplate.builder()
            .system("你是一名数据资产管理专家...")
            .context("相似资产参考: " + formatSimilarAssets(similarAssets))
            .user("请为资产 {0} 生成注释，字段: {1}", asset.getName(), fields)
            .build();
        
        // 3. 调用LLM生成
        LLMResponse response = llmOrchestrator.generate(
            template.build(),
            Scene.AUTO_ANNOTATION
        );
        
        // 4. 解析结果
        return parseAnnotationResponse(response.getContent());
    }
}
```

### 8.2 智能问答

```java
@Service
public class IntelligentQAService {
    
    private final LLMOrchestrator llmOrchestrator;
    private final VectorRetrievalService vectorRetrieval;
    private final ConversationService conversationService;
    
    /**
     * 问答处理
     */
    public QAResponse answer(String question, String conversationId) {
        // 1. 获取对话上下文
        ConversationContext context = conversationId != null 
            ? conversationService.getConversation(conversationId) 
            : null;
        
        // 2. 检索相关资产信息
        List<检索结果> relevantAssets = vectorRetrieval.hybridSearch(
            question, null);
        
        // 3. 构建上下文
        String contextContent = buildContext(relevantAssets);
        
        // 4. 构建提示词
        String systemPrompt = "你是数据资产管理助手...";
        String userPrompt = String.format(
            "上下文信息:\n%s\n\n用户问题: %s", 
            contextContent, question);
        
        // 5. 调用LLM
        LLMResponse response = llmOrchestrator.generate(
            LLMRequest.builder()
                .systemPrompt(systemPrompt)
                .prompt(userPrompt)
                .temperature(0.7)
                .maxTokens(1000)
                .build(),
            Scene.INTELLIGENT_QA
        );
        
        // 6. 保存对话记录
        if (conversationId != null) {
            conversationService.addMessage(conversationId, 
                new Message("user", question));
            conversationService.addMessage(conversationId, 
                new Message("assistant", response.getContent()));
        }
        
        return QAResponse.builder()
            .answer(response.getContent())
            .sources(relevantAssets)
            .model(response.getModel())
            .latencyMs(response.getLatencyMs())
            .build();
    }
}
```
