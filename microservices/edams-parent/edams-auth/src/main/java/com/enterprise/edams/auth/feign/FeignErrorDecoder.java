package com.enterprise.edams.auth.feign;

import com.enterprise.edams.common.result.Result;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Feign错误解码器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign调用异常: methodKey={}, status={}, reason={}",
                methodKey, response.status(), response.reason());

        if (response.status() >= 500) {
            return new RuntimeException("权限服务暂时不可用，请稍后重试");
        } else if (response.status() == 404) {
            return new RuntimeException("权限服务资源未找到");
        } else if (response.status() == 401) {
            return new RuntimeException("权限服务认证失败");
        } else if (response.status() == 403) {
            return new RuntimeException("权限服务访问被拒绝");
        }

        return new RuntimeException("权限服务调用失败: " + response.status());
    }
}
