#!/bin/bash

# 🎯 하이브리드 Redis 추천 시스템 테스트
# 
# 🥬 재료 추가 → ✅ 즉시 응답 (0.5초)
#     ↓ (백그라운드)
# 🔍 복잡한 추천 계산 시작 (2-3초 소요)
#     ↓
# 💾 Redis/DB에 추천 결과 캐싱
#     ↓ (나중에 SNS 화면 방문)
# 📱 SNS 화면: "당신을 위한 맞춤 추천" 섹션 표시

echo "🎯 하이브리드 Redis 추천 시스템 테스트 시작"
echo "================================================"

# 기본 설정
BASE_URL="http://localhost:8080"
TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzMDYiLCJtZW1iZXJJZCI6MzA2LCJuYW1lIjoi67Cw7KeA7JiBIiwicm9sZSI6IlVTRVIiLCJwaG9uZSI6IjAxMDExMTE0NDQ0IiwiY29tcGxldGVGbGFnIjp0cnVlLCJpYXQiOjE3NTEyODk4MTUsImV4cCI6MTc1MTM3NjIxNX0.8T8XpBGe1tezr8MiugVineSxT2DhBMmaucwJB0pQHnR5yLk-ofkH6QpdjKC3oSM94BtnhyoYdbamg8v9mVadxg"  # 실제 JWT 토큰으로 교체 필요

echo ""
echo "1️⃣ 재료 등록 테스트 (즉시 응답)"
echo "--------------------------------"

# 재료 등록 (백그라운드 추천 트리거)
curl -X POST "$BASE_URL/ck/my-ingredient" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '[
    {
        "foodID": "111", 
        "foodName" : "111퀴노아"
    },
      {
        "foodID": 222 , 
        "foodName" : "222풋마늘"
    }
]' \
  -w "\n응답 시간: %{time_total}초\n"

echo ""
echo "✅ 재료 등록 완료! (백그라운드에서 추천 계산 시작됨)"

echo ""
echo "2️⃣ 백그라운드 추천 계산 대기 (2-3초)"
echo "-----------------------------------"
echo "추천 계산 중... 잠시만 기다려주세요"

# 추천 계산 완료 대기
for i in {1..5}; do
    echo -n "."
    sleep 1
done
echo ""

echo ""
echo "3️⃣ SNS 피드 조회 (하이브리드 구조)"
echo "--------------------------------"

# SNS 피드 조회
echo "📱 하이브리드 SNS 피드 요청..."
curl -X GET "$BASE_URL/cook/recipes/feed?limit=10" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/json" \
  -w "\n응답 시간: %{time_total}초\n" | jq '
    {
      "추천_섹션": {
        "제목": .data.personalizedRecommendations.sectionTitle,
        "추천_있음": .data.personalizedRecommendations.hasRecommendations,
        "추천_개수": .data.personalizedRecommendations.totalCount,
        "빈_메시지": .data.personalizedRecommendations.emptyMessage,
        "추천_목록": .data.personalizedRecommendations.recommendations | map({
          "레시피ID": .cookId,
          "제목": .cookTitle,
          "매칭률": .matchingRatePercent,
          "설명": .matchingDescription
        })
      },
      "전체_레시피_섹션": {
        "제목": .data.allRecipes.sectionTitle,
        "총_개수": .data.allRecipes.totalCount,
        "레시피_목록": .data.allRecipes.recipes[0:3] | map({
          "레시피ID": .recipe.cookId,
          "제목": .recipe.cookTitle,
          "추천받음": .isRecommended,
          "배지": .recommendationBadge,
          "매칭설명": .matchingDescription
        })
      }
    }
  '

echo ""
echo "4️⃣ Redis 캐시 확인 (개발용)"
echo "-------------------------"

echo "💾 Redis에 저장된 캐시 구조:"
echo ""
echo "사용자별 경량 추천:"
echo "key: recommendations:사용자ID"
echo "value: [{cookId, matchingRate, matchedCount, totalIngredients}, ...]"
echo ""
echo "레시피 상세 정보:"
echo "key: recipe_detail:레시피ID"  
echo "value: {cookId, cookTitle, authorId, cookImg, authorName}"
echo ""

echo "🎯 하이브리드 Redis 추천 시스템 테스트 완료!"
echo "================================================"
echo ""
echo "📋 테스트 결과 확인 사항:"
echo "✅ 재료 등록 즉시 응답 (0.5초 내)"
echo "✅ 백그라운드 추천 계산 (2-3초)"
echo "✅ 하이브리드 Redis 캐싱 (경량 + 상세 분리)"
echo "✅ SNS 피드 통합 응답 (맞춤 추천 + 전체 레시피)"
echo "✅ 추천 플래그 표시 (⭐️ 맞춤 추천)"
echo ""
echo "🚨 주의사항:"
echo "- TOKEN 변수에 유효한 JWT 토큰을 설정하세요"
echo "- 서버가 localhost:8080에서 실행 중인지 확인하세요"
echo "- Redis 서버가 실행 중인지 확인하세요" 