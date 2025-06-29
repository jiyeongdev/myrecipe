#!/bin/bash

# 매개변수 검증 및 로깅 테스트 스크립트
# 다양한 잘못된 매개변수 케이스를 테스트

echo "🧪 매개변수 검증 및 로깅 테스트 시작"
echo "===================================="

# 서버 주소 설정
SERVER_URL="http://localhost:8080"

# JWT 토큰 (실제 테스트 시에는 로그인 후 받은 토큰 사용)
# JWT_TOKEN="your_jwt_token_here"

if [ -z "$JWT_TOKEN" ]; then
    echo "⚠️  JWT 토큰이 설정되지 않았습니다."
    echo "실제 테스트를 위해 JWT_TOKEN 환경변수를 설정해주세요."
    echo "export JWT_TOKEN=\"your_jwt_token_here\""
    echo ""
fi

# 테스트 함수 정의
test_api() {
    local test_name="$1"
    local data="$2"
    local expected_status="$3"
    
    echo "📋 테스트: $test_name"
    echo "요청 데이터: $data"
    
    if [ -n "$JWT_TOKEN" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "$SERVER_URL/ck/my-ingredient" \
                        -H "Content-Type: application/json" \
                        -H "Authorization: Bearer $JWT_TOKEN" \
                        -d "$data")
        
        status_code=$(echo "$response" | tail -n1)
        response_body=$(echo "$response" | head -n -1)
        
        echo "응답 상태: $status_code (예상: $expected_status)"
        echo "응답 내용: $response_body"
        
        if [ "$status_code" = "$expected_status" ]; then
            echo "✅ 테스트 통과"
        else
            echo "❌ 테스트 실패"
        fi
    else
        echo "⏭️  JWT 토큰 없음으로 스킵"
    fi
    
    echo "----------------------------------------"
}

# 테스트 케이스 1: 정상 데이터
echo "🎯 테스트 케이스 1: 정상 데이터"
VALID_DATA='[
  {
    "foodID": 1001,
    "foodName": "양파"
  },
  {
    "foodID": 1002,
    "foodName": "당근"
  }
]'
test_api "정상적인 재료 등록" "$VALID_DATA" "200"

# 테스트 케이스 2: 빈 배열
echo "🎯 테스트 케이스 2: 빈 배열"
EMPTY_DATA='[]'
test_api "빈 배열 전송" "$EMPTY_DATA" "500"

# 테스트 케이스 3: null 데이터
echo "🎯 테스트 케이스 3: null 데이터"
test_api "null 데이터 전송" "null" "400"

# 테스트 케이스 4: foodID가 null인 경우
echo "🎯 테스트 케이스 4: foodID가 null인 경우"
NULL_FOOD_ID_DATA='[
  {
    "foodID": null,
    "foodName": "양파"
  }
]'
test_api "foodID가 null인 데이터" "$NULL_FOOD_ID_DATA" "500"

# 테스트 케이스 5: foodID가 0 이하인 경우
echo "🎯 테스트 케이스 5: foodID가 0 이하인 경우"
INVALID_FOOD_ID_DATA='[
  {
    "foodID": 0,
    "foodName": "양파"
  }
]'
test_api "foodID가 0인 데이터" "$INVALID_FOOD_ID_DATA" "500"

# 테스트 케이스 6: foodName이 null인 경우
echo "🎯 테스트 케이스 6: foodName이 null인 경우"
NULL_FOOD_NAME_DATA='[
  {
    "foodID": 1001,
    "foodName": null
  }
]'
test_api "foodName이 null인 데이터" "$NULL_FOOD_NAME_DATA" "500"

# 테스트 케이스 7: foodName이 빈 문자열인 경우
echo "🎯 테스트 케이스 7: foodName이 빈 문자열인 경우"
EMPTY_FOOD_NAME_DATA='[
  {
    "foodID": 1001,
    "foodName": ""
  }
]'
test_api "foodName이 빈 문자열인 데이터" "$EMPTY_FOOD_NAME_DATA" "500"

# 테스트 케이스 8: foodName이 공백만 있는 경우
echo "🎯 테스트 케이스 8: foodName이 공백만 있는 경우"
WHITESPACE_FOOD_NAME_DATA='[
  {
    "foodID": 1001,
    "foodName": "   "
  }
]'
test_api "foodName이 공백만 있는 데이터" "$WHITESPACE_FOOD_NAME_DATA" "500"

# 테스트 케이스 9: 중복된 foodID
echo "🎯 테스트 케이스 9: 중복된 foodID"
DUPLICATE_FOOD_ID_DATA='[
  {
    "foodID": 1001,
    "foodName": "양파"
  },
  {
    "foodID": 1001,
    "foodName": "양파2"
  }
]'
test_api "중복된 foodID 데이터" "$DUPLICATE_FOOD_ID_DATA" "500"

# 테스트 케이스 10: 너무 긴 foodName
echo "🎯 테스트 케이스 10: 너무 긴 foodName (100자 초과)"
LONG_NAME="아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주아주긴이름"
LONG_FOOD_NAME_DATA="[
  {
    \"foodID\": 1001,
    \"foodName\": \"$LONG_NAME\"
  }
]"
test_api "너무 긴 foodName 데이터" "$LONG_FOOD_NAME_DATA" "500"

echo ""
echo "📊 테스트 완료!"
echo "기대되는 로그 패턴:"
echo "- 🔍 매개변수 검증 시작"
echo "- ❌ 매개변수 검증 실패 (잘못된 케이스)"
echo "- ✅ 매개변수 검증 완료 (정상 케이스)"
echo "- 📋 재료 등록 작업 시작"
echo "- 🔄 재료 등록 시도"
echo "- ✅ 재료 등록 성공"
echo "- 📊 재료 등록 결과"
echo "- 🚀 비동기 레시피 추천 이벤트 준비"
echo "- 📢 재료 등록 이벤트 발행 성공"
echo ""
echo "🔍 로그 모니터링 명령어:"
echo "tail -f logs/application.log | grep -E '(🔍|❌|✅|📋|🔄|📊|🚀|📢)'" 