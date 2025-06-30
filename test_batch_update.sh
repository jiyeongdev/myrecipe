#!/bin/bash

# 배치 업데이트 테스트 스크립트
# 속도 성능 개선된 배치 업데이트 기능 테스트

echo "=== 레시피 추천 배치 업데이트 테스트 ==="
echo ""

# 1. 배치 업데이트 시작
echo "1. 배치 업데이트 시작..."
BATCH_RESPONSE=$(curl -s -X POST "http://localhost:8080/cook/recipes/batch-update" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json")

echo "배치 업데이트 응답: $BATCH_RESPONSE"
echo ""

# 2. 잠시 대기 (배치 처리 시간)
echo "2. 배치 처리 대기 중... (10초)"
sleep 10

# 3. 배치 상태 조회
echo "3. 배치 업데이트 상태 조회..."
STATUS_RESPONSE=$(curl -s -X GET "http://localhost:8080/cook/recipes/batch-status" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json")

echo "배치 상태: $STATUS_RESPONSE"
echo ""

# 4. 개별 추천 조회 테스트 (캐시 확인)
echo "4. 개별 추천 조회 테스트 (캐시 확인)..."
RECOMMENDATION_RESPONSE=$(curl -s -X GET "http://localhost:8080/cook/recipes/recommendations" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json")

echo "추천 조회 응답: $RECOMMENDATION_RESPONSE"
echo ""

# 5. 성능 측정 (TTL 연장 효과 확인)
echo "5. 성능 측정 (TTL 연장 효과 확인)..."
echo "첫 번째 요청:"
time curl -s -X GET "http://localhost:8080/cook/recipes/recommendations" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" > /dev/null

echo "두 번째 요청 (캐시 hit):"
time curl -s -X GET "http://localhost:8080/cook/recipes/recommendations" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" > /dev/null

echo ""
echo "=== 테스트 완료 ==="
echo ""
echo "개선사항:"
echo "- TTL 연장: 1분 → 7일 (캐시 지속성 대폭 향상)"
echo "- 배치 업데이트: 기존 로직 재사용으로 간단한 구현"
echo "- 병렬 처리: CompletableFuture로 성능 최적화"
echo "- 상태 모니터링: 배치 진행 상황 실시간 확인"
echo ""
echo "사용법:"
echo "1. 배치 업데이트 시작: POST /cook/recipes/batch-update"
echo "2. 상태 조회: GET /cook/recipes/batch-status"
echo "3. 추천 조회: GET /cook/recipes/recommendations" 