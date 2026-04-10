package com.enterprise.edams.incentive.dto;

import com.enterprise.edams.incentive.entity.*;
import lombok.Data;

@Data
public class TransactionSearchRequest {
    
    private TransactionType type;
    private BizType bizType;
    private int pageNum = 1;
    private int pageSize = 20;
}
