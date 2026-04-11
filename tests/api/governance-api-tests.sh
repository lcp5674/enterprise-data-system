#!/bin/bash
# ============================================
# EDAMS 治理服务 API 测试脚本
# ============================================

set -e

BASE_URL="${BASE_URL:-http://localhost:8080}"
TOKEN_FILE="/tmp/edams_token.txt"

echo "=============================================="
echo "EDAMS 治理服务 API 测试"
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
echo "=== 1. 数据质量规则测试 ==="
test_api "创建质量规则" "200" \
    -X POST "$BASE_URL/api/quality/rules" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"ruleCode":"TEST-RULE-001","ruleName":"测试规则","ruleType":"COMPLETENESS"}'

test_api "获取规则列表" "200" \
    -X GET "$BASE_URL/api/quality/rules" \
    -H "Authorization: Bearer $TOKEN"

test_api "执行质量检测" "200" \
    -X POST "$BASE_URL/api/quality/checks/execute" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"assetId":"ASSET-001","ruleId":1}'

echo ""
echo "=== 2. 数据血缘测试 ==="
test_api "获取血缘关系" "200" \
    -X GET "$BASE_URL/api/lineage/asset/ASSET-001" \
    -H "Authorization: Bearer $TOKEN"

test_api "解析SQL血缘" "200" \
    -X POST "$BASE_URL/api/lineage/parse" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"sql":"SELECT * FROM table1 JOIN table2 ON table1.id = table2.id"}'

test_api "获取影响分析" "200" \
    -X GET "$BASE_URL/api/lineage/impact/ASSET-001" \
    -H "Authorization: Bearer $TOKEN"

echo ""
echo "=== 3. 数据标准测试 ==="
test_api "创建数据标准" "200" \
    -X POST "$BASE_URL/api/standards" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"standardCode":"STD-TEST","standardName":"测试标准","standardType":"FORMAT"}'

test_api "合规检查" "200" \
    -X POST "$BASE_URL/api/standards/check" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"assetId":"ASSET-001","standardId":1}'

echo ""
echo "=== 4. AI治理推荐测试 ==="
test_api "获取改善建议" "200" \
    -X GET "$BASE_URL/api/governance/recommend/ASSET-001" \
    -H "Authorization: Bearer $TOKEN"

test_api "分析数据质量趋势" "200" \
    -X POST "$BASE_URL/api/governance/analyze" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"assetIds":["ASSET-001","ASSET-002"],"timeRange":"7d"}'

echo ""
echo "=== 5. 元数据管理测试 ==="
test_api "搜索元数据" "200" \
    -X GET "$BASE_URL/api/metadata/search?keyword=用户" \
    -H "Authorization: Bearer $TOKEN"

test_api "获取资产详情" "200" \
    -X GET "$BASE_URL/api/metadata/assets/ASSET-001" \
    -H "Authorization: Bearer $TOKEN"

echo ""
echo "=============================================="
echo "通过: $PASSED | 失败: $FAILED"
echo "=============================================="

rm -f "$TOKEN_FILE"
[ $FAILED -eq 0 ]
