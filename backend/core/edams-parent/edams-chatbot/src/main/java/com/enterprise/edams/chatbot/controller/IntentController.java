package com.enterprise.edams.chatbot.controller;

import com.enterprise.edams.chatbot.entity.ChatContext;
import com.enterprise.edams.chatbot.entity.ChatIntent;
import com.enterprise.edams.chatbot.service.IntentRecognizer;
import com.enterprise.edams.chatbot.service.ResponseGenerator;
import com.enterprise.edams.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 意图控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Tag(name = "意图识别", description = "意图识别相关接口")
@RestController
@RequestMapping("/api/chatbot/intent")
@RequiredArgsConstructor
public class IntentController {

    private final IntentRecognizer intentRecognizer;
    private final ResponseGenerator responseGenerator;

    @Operation(summary = "识别意图")
    @PostMapping("/recognize")
    public R<IntentRecognizer.RecognitionResult> recognizeIntent(
            @RequestParam String message,
            @RequestBody(required = false) ChatContext context) {
        if (context == null) {
            context = new ChatContext();
        }
        return R.ok(intentRecognizer.recognizeIntent(message, context));
    }

    @Operation(summary = "获取意图详情")
    @GetMapping("/{intentType}")
    public R<ChatIntent> getIntentDetails(@PathVariable String intentType) {
        return R.ok(intentRecognizer.getIntentDetails(intentType));
    }

    @Operation(summary = "提取槽位")
    @PostMapping("/slots/extract")
    public R<Map<String, String>> extractSlots(
            @RequestParam String message,
            @RequestParam String intentType) {
        return R.ok(intentRecognizer.extractSlots(message, intentType));
    }

    @Operation(summary = "验证槽位")
    @PostMapping("/slots/validate")
    public R<Boolean> validateSlots(
            @RequestParam String intentType,
            @RequestBody Map<String, String> slots) {
        return R.ok(intentRecognizer.validateSlots(intentType, slots));
    }

    @Operation(summary = "获取缺失槽位")
    @PostMapping("/slots/missing")
    public R<Map<String, String>> getMissingSlots(
            @RequestParam String intentType,
            @RequestBody Map<String, String> currentSlots) {
        return R.ok(intentRecognizer.getMissingRequiredSlots(intentType, currentSlots));
    }

    @Operation(summary = "获取后续意图")
    @GetMapping("/follow-up")
    public R<String> getFollowUpIntent(
            @RequestParam String currentIntent,
            @RequestBody ChatContext context) {
        return R.ok(intentRecognizer.getFollowUpIntent(currentIntent, context));
    }

    @Operation(summary = "判断是否确认")
    @PostMapping("/check/confirmation")
    public R<Boolean> isConfirmation(@RequestParam String message) {
        return R.ok(intentRecognizer.isConfirmation(message));
    }

    @Operation(summary = "判断是否取消")
    @PostMapping("/check/cancellation")
    public R<Boolean> isCancellation(@RequestParam String message) {
        return R.ok(intentRecognizer.isCancellation(message));
    }

    @Operation(summary = "生成响应")
    @PostMapping("/response/generate")
    public R<String> generateResponse(
            @RequestParam String intentType,
            @RequestBody(required = false) Map<String, String> slots,
            @RequestBody(required = false) ChatContext context) {
        if (slots == null) slots = Map.of();
        if (context == null) context = new ChatContext();
        return R.ok(responseGenerator.generateResponse(intentType, slots, context));
    }

    @Operation(summary = "生成确认请求")
    @PostMapping("/response/confirm")
    public R<String> generateConfirmation(
            @RequestParam String intentType,
            @RequestBody(required = false) Map<String, String> slots) {
        if (slots == null) slots = Map.of();
        return R.ok(responseGenerator.generateConfirmationRequest(intentType, slots));
    }

    @Operation(summary = "生成帮助消息")
    @GetMapping("/response/help")
    public R<String> generateHelp() {
        return R.ok(responseGenerator.generateHelpMessage());
    }

    @Operation(summary = "生成FAQ响应")
    @PostMapping("/response/faq")
    public R<String> generateFaqResponse(@RequestParam String question) {
        return R.ok(responseGenerator.generateFaqResponse(question));
    }

    @Operation(summary = "生成后续建议")
    @PostMapping("/response/followup")
    public R<List<String>> generateFollowUpSuggestions(
            @RequestParam String intentType,
            @RequestBody(required = false) ChatContext context) {
        if (context == null) context = new ChatContext();
        return R.ok(responseGenerator.generateFollowUpSuggestions(intentType, context));
    }
}
