#!/bin/bash

# 냉장고 재료 등록 API 테스트 스크립트
# 비동기 레시피 추천 기능 테스트

echo "🧪 비동기 레시피 추천 시스템 테스트 시작"
echo "================================="

# 서버 주소 설정
SERVER_URL="http://localhost:8080"

# JWT 토큰 (실제 테스트 시에는 로그인 후 받은 토큰 사용)
# JWT_TOKEN="your_jwt_token_here"

echo "📝 테스트 데이터 준비..."

# 테스트용 재료 등록 데이터
INGREDIENT_DATA='[
  {
    "foodId": 1001,
    "foodName": "양파"
  },
  {
    "foodId": 1002,
    "foodName": "당근"
  },
  {
    "foodId": 1003,
    "foodName": "감자"
  },
  {
    "foodId": 1004,
    "foodName": "계란"
  }
]'

echo "🚀 재료 등록 API 호출 시작..."
echo "요청 데이터: $INGREDIENT_DATA"

# API 호출 (JWT 토큰이 있는 경우)
if [ -n "$JWT_TOKEN" ]; then
    curl -X POST "$SERVER_URL/ck/my-ingredient" \
         -H "Content-Type: application/json" \
         -H "Authorization: Bearer $JWT_TOKEN" \
         -d "$INGREDIENT_DATA" \
         -w "\n상태 코드: %{http_code}\n응답 시간: %{time_total}초\n" \
         -s
else
    echo "⚠️  JWT 토큰이 설정되지 않았습니다."
    echo "실제 테스트를 위해서는 다음 단계를 따라주세요:"
    echo "1. 애플리케이션 실행"
    echo "2. 로그인하여 JWT 토큰 획득"
    echo "3. 스크립트의 JWT_TOKEN 변수에 토큰 설정"
    echo ""
    echo "테스트용 curl 명령어:"
    echo "curl -X POST '$SERVER_URL/ck/my-ingredient' \\"
    echo "     -H 'Content-Type: application/json' \\"
    echo "     -H 'Authorization: Bearer YOUR_JWT_TOKEN' \\"
    echo "     -d '$INGREDIENT_DATA'"
fi

echo ""
echo "📊 기대되는 동작:"
echo "1. 재료 등록 API가 즉시 응답 반환"
echo "2. 백그라운드에서 비동기 레시피 추천 작업 시작"
echo "3. 다음 로그들을 확인:"
echo "   - 🌐 [API] 재료 등록 API 호출"
echo "   - 🔄 [main] FoodIngredientService.createFoodIngredients() 시작"
echo "   - 📢 재료 등록 이벤트 발행"
echo "   - 🚀 [RecipeRecommend-1] 비동기 레시피 추천 작업 시작"
echo "   - 🔍 [RecipeRecommend-1] 레시피 검색 완료"
echo "   - 💾 추천 결과 저장"
echo "   - 📱 [Notification-1] 추천 완료 알림 전송"
echo "   - ✨ 레시피 추천 완료"

echo ""
echo "🔍 애플리케이션 로그 모니터링:"
echo "tail -f logs/application.log | grep -E '(🌐|🔄|��|🚀|🔍|💾|📱|✨)'" 