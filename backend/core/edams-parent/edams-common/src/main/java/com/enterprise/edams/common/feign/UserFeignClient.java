package com.enterprise.edams.common.feign;

import com.enterprise.edams.common.result.Result;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign客户端
 *
 * <p>用于工作流服务、通知服务等获取用户信息</p>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@FeignClient(name = "edams-user", path = "/api/v1/users")
public interface UserFeignClient {

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/{id}")
    Result<UserBasicInfo> getUserById(@PathVariable("id") Long id);

    /**
     * 用户基本信息（用于Feign调用）
     */
    @Data
    class UserBasicInfo {
        private Long id;
        private String username;
        private String realName;
        private String nickname;
        private String email;
        private String phone;
        private String departmentId;
        private String departmentName;
        private String avatar;
        private Integer status;
    }
}
