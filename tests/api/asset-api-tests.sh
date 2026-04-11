#!/bin/bash
# ============================================
# EDAMS 资产服务 API 测试脚本
# ============================================

set -e

BASE_URL="${BASE_URL:-http://localhost:8080}"
TOKEN_FILE="/tmp/edams_token.txt"

echo "=============================================="
echo "EDAMS 资产服务 API 测试"
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
    else
        echo -e "${RED}✗ FAIL${NC}: $name (Expected $expected_status, Got $status)"
        echo "Response: $body"
        ((FAILED++))
    fi
}

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
echo "=== 1. 资产注册测试 ==="
test_api "注册新资产" "200" \
    -X POST "$BASE_URL/api/assets" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"assetCode":"TEST-ASSET-001","assetName":"测试资产","assetType":"TABLE","owner":"admin"}'

test_api "批量注册资产" "200" \
    -X POST "$BASE_URL/api/assets/batch" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '[{"assetCode":"TEST-001","assetName":"资产1"},{"assetCode":"TEST-002","assetName":"资产2"}]'

echo ""
echo "=== 2. 资产查询测试 ==="
test_api "获取资产列表" "200" \
    -X GET "$BASE_URL/api/assets" \
    -H "Authorization: Bearer $TOKEN"

test_api "分页查询资产" "200" \
    -X GET "$BASE_URL/api/assets/page?page=0&size=10" \
    -H "Authorization: Bearer $TOKEN"

test_api "搜索资产" "200" \
    -X GET "$BASE_URL/api/assets/search?keyword=用户" \
    -H "Authorization: Bearer $TOKEN"

test_api "按类型查询" "200" \
    -X GET "$BASE_URL/api/assets/type/TABLE" \
    -H "Authorization: Bearer $TOKEN"

echo ""
echo "=== 3. 资产详情测试 ==="
test_api "获取资产详情" "200" \
    -X GET "$BASE_URL/api/assets/TEST-ASSET-001" \
    -H "Authorization: Bearer $TOKEN"

test_api "获取资产Schema" "200" \
    -X GET "$BASE_URL/api/assets/TEST-ASSET-001/schema" \
    -H "Authorization: Bearer $TOKEN"

test_api "获取资产统计" "200" \
    -X GET "$BASE_URL/api/assets/TEST-ASSET-001/stats" \
    -H "Authorization: Bearer $TOKEN"

echo ""
echo "=== 4. 资产更新测试 ==="
test_api "更新资产信息" "200" \
    -X PUT "$BASE_URL/api/assets/TEST-ASSET-001" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"assetName":"更新后的资产名称","description":"更新描述"}'

test_api "更新资产状态" "200" \
    -X PUT "$BASE_URL/api/assets/TEST-ASSET-001/status" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"status":"ARCHIVED"}'

echo ""
echo "=== 5. 生命周期管理 ==="
test_api "创建生命周期记录" "200" \
    -X POST "$BASE_URL/api/assets/TEST-ASSET-001/lifecycle" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"phase":"CREATED","description":"创建阶段"}'

test_api "获取生命周期历史" "200" \
    -X GET "$BASE_URL/api/assets/TEST-ASSET-001/lifecycle/history" \
    -H "Authorization: Bearer $TOKEN"

test_api "归档资产" "200" \
    -X POST "$BASE_URL/api/assets/TEST-ASSET-001/archive" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"archiveReason":"不再使用"}'

echo ""
echo "=== 6. 资产版本管理 ==="
test_api "获取资产版本列表" "200" \
    -X GET "$BASE_URL/api/assets/TEST-ASSET-001/versions" \
    -H "Authorization: Bearer $TOKEN"

test_api "获取指定版本" "200" \
    -X GET "$BASE_URL/api/assets/TEST-ASSET-001/versions/1" \
    -H "Authorization: Bearer $TOKEN"

test_api "回滚版本" "200" \
    -X POST "$BASE_URL/api/assets/TEST-ASSET-001/rollback" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"targetVersion":1}'

echo ""
echo "=============================================="
echo "通过: $PASSED | 失败: $FAILED"
echo "=============================================="

rm -f "$TOKEN_FILE"
[ $FAILED -eq 0 ]
