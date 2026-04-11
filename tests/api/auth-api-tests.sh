#!/bin/bash
# ============================================
# EDAMS 认证服务 API 测试脚本
# 使用方式: ./auth-api-tests.sh
# ============================================

set -e

BASE_URL="${BASE_URL:-http://localhost:8080}"
TOKEN_FILE="/tmp/edams_token.txt"

echo "=============================================="
echo "EDAMS 认证服务 API 测试"
echo "Base URL: $BASE_URL"
echo "=============================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试结果计数
PASSED=0
FAILED=0

# 测试函数
test_api() {
    local name=$1
    local expected_status=$2
    shift 2
    local response=$(curl -s -w "\n%{http_code}" "$@")
    local body=$(echo "$response" | head -n -1)
    local status=$(echo "$response" | tail -n 1)

    if [ "$status" == "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC}: $name (HTTP $status)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC}: $name (Expected $expected_status, Got $status)"
        echo "Response: $body"
        ((FAILED++))
        return 1
    fi
}

echo ""
echo "=== 1. 用户注册测试 ==="
test_api "注册新用户" "200" \
    -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d '{"username":"testuser","password":"Test123456","email":"test@example.com","fullName":"Test User"}'

test_api "注册重复用户名" "400" \
    -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d '{"username":"testuser","password":"Test123456","email":"test2@example.com","fullName":"Test User"}'

echo ""
echo "=== 2. 用户登录测试 ==="
test_api "正确凭据登录" "200" \
    -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}'

test_api "错误密码登录" "401" \
    -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"wrongpassword"}'

test_api "不存在的用户" "404" \
    -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"nonexistent","password":"password"}'

echo ""
echo "=== 3. Token验证测试 ==="
# 先登录获取token
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}')
TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    echo "获取到Token: ${TOKEN:0:20}..."
    echo "$TOKEN" > "$TOKEN_FILE"

    test_api "验证有效Token" "200" \
        -X GET "$BASE_URL/api/auth/validate" \
        -H "Authorization: Bearer $TOKEN"

    test_api "验证过期Token" "401" \
        -X GET "$BASE_URL/api/auth/validate" \
        -H "Authorization: Bearer invalid.token.here"

    test_api "无Token访问" "401" \
        -X GET "$BASE_URL/api/auth/validate"
else
    echo -e "${YELLOW}⚠ 跳过Token测试: 无法获取Token${NC}"
fi

echo ""
echo "=== 4. Token刷新测试 ==="
if [ -f "$TOKEN_FILE" ]; then
    TOKEN=$(cat "$TOKEN_FILE")
    test_api "刷新Token" "200" \
        -X POST "$BASE_URL/api/auth/refresh" \
        -H "Authorization: Bearer $TOKEN"
else
    echo -e "${YELLOW}⚠ 跳过刷新测试: 无Token文件${NC}"
fi

echo ""
echo "=== 5. MFA测试 ==="
test_api "发送MFA验证码" "200" \
    -X POST "$BASE_URL/api/auth/mfa/send" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin"}'

test_api "验证MFA码" "200" \
    -X POST "$BASE_URL/api/auth/mfa/verify" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","code":"123456"}'

echo ""
echo "=== 6. 登录日志测试 ==="
if [ -f "$TOKEN_FILE" ]; then
    TOKEN=$(cat "$TOKEN_FILE")
    test_api "获取登录日志" "200" \
        -X GET "$BASE_URL/api/auth/login-logs" \
        -H "Authorization: Bearer $TOKEN"
fi

echo ""
echo "=============================================="
echo "测试结果汇总"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
echo "=============================================="

# 清理
rm -f "$TOKEN_FILE"

# 返回退出码
[ $FAILED -eq 0 ]
