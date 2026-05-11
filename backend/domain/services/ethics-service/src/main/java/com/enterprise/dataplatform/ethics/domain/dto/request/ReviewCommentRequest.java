package com.enterprise.dataplatform.ethics.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCommentRequest {

    @NotBlank(message = "评论内容不能为空")
    private String comment;

    private List<String> attachments;

    private String category;
}
