#!/bin/bash
# ============================================
# EDAMS 用户服务 API 测试脚本
# ============================================

set -e

BASE_URL="${BASE_URL:-http://localhost:8080}"
TOKEN_FILE="/tmp/edams_token.txt"

echo "=============================================="
echo "EDAMS 用户服务 API 测试"
echo "Base URL: $BASE_URL"
echo "=============================================="

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

PASSED=0
FAILED=0

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

# 获取Token
get_token() {
    if [ -f "$TOKEN_FILE" ]; then
        cat "$TOKEN_FILE"
    else
        LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
            -H "Content-Type: application/json" \
            -d '{"username":"admin","password":"admin123"}')
        TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        echo "$TOKEN" > "$TOKEN_FILE"
        echo "$TOKEN"
    fi
}

TOKEN=$(get_token)

echo ""
echo "=== 1. 用户CRUD测试 ==="
test_api "创建用户" "200" \
    -X POST "$BASE_URL/api/users" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"username":"newuser","email":"newuser@test.com","fullName":"New User","department":"IT"}'

test_api "获取用户列表" "200" \
    -X GET "$BASE_URL/api/users" \
    -H "Authorization: Bearer $TOKEN"

test_api "分页查询用户" "200" \
    -X GET "$BASE_URL/api/users/page?page=0&size=10" \
    -H "Authorization: Bearer $TOKEN"

test_api "搜索用户" "200" \
    -X GET "$BASE_URL/api/users/search?keyword=admin" \
    -H "Authorization: Bearer $TOKEN"

echo ""
echo "=== 2. 用户详情测试 ==="
test_api "获取用户详情" "200" \
    -X GET "$BASE_URL/api/users/1" \
    -H "Authorization: Bearer $TOKEN"

test_api "获取不存在的用户" "404" \
    -X GET "$BASE_URL/api/users/9999" \
    -H "Authorization: Bearer $TOKEN"

test_api "按用户名查询" "200" \
    -X GET "$BASE_URL/api/users/username/admin" \
    -H "Authorization: Bearer $TOKEN"

echo ""
echo "=== 3. 用户更新测试 ==="
test_api "更新用户信息" "200" \
    -X PUT "$BASE_URL/api/users/1" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"fullName":"更新后的名称","department":"新部门"}'

test_api "修改密码" "200" \
    -X PUT "$BASE_URL/api/users/1/password" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"oldPassword":"admin123","newPassword":"NewPass123"}'

echo ""
echo "=== 4. 用户状态管理 ==="
test_api "禁用用户" "200" \
    -X PUT "$BASE_URL/api/users/1/disable" \
    -H "Authorization: Bearer $TOKEN"

test_api "启用用户" "200" \
    -X PUT "$BASE_URL/api/users/1/enable" \
    -H "Authorization: Bearer $TOKEN"

test_api "重置密码" "200" \
    -X POST "$BASE_URL/api/users/1/reset-password" \
    -H "Authorization: Bearer $TOKEN"

echo ""
echo "=== 5. 部门管理测试 ==="
test_api "获取部门列表" "200" \
    -X GET "$BASE_URL/api/departments" \
    -H "Authorization: Bearer $TOKEN"

test_api "创建部门" "200" \
    -X POST "$BASE_URL/api/departments" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"departmentName":"新部门","parentId":null}'

echo ""
echo "=== 6. 角色管理测试 ==="
test_api "获取角色列表" "200" \
    -X GET "$BASE_URL/api/roles" \
    -H "Authorization: Bearer $TOKEN"

test_api "分配角色" "200" \
    -X POST "$BASE_URL/api/users/1/roles" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"roleIds":[1,2]}'

echo ""
echo "=============================================="
echo "通过: $PASSED | 失败: $FAILED"
echo "=============================================="

rm -f "$TOKEN_FILE"
[ $FAILED -eq 0 ]
