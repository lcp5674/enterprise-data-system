package com.enterprise.edams.auth.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Feign请求拦截器
 * 用于添加认证信息等通用请求头
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    private static final String HEADER_X_SERVICE_NAME = "X-Service-Name";
    private static final String HEADER_X_REQUEST_ID = "X-Request-ID";

    @Override
    public void apply(RequestTemplate template) {
        // 添加服务标识
        template.header(HEADER_X_SERVICE_NAME, "edams-auth");

        // 添加请求追踪ID（如果存在）
        String requestId = java.util.UUID.randomUUID().toString();
        template.header(HEADER_X_REQUEST_ID, requestId);

        log.debug("Feign请求拦截: method={}, url={}, requestId={}",
                template.method(), template.url(), requestId);
    }
}
