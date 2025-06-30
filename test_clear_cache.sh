#!/bin/bash

echo "Redis 캐시 클리어 테스트 시작..."

# Redis 캐시 클리어 API 호출 (모든 사용자)
echo "모든 사용자의 추천 캐시 클리어 중..."

# 테스트용 사용자 ID들 (실제 사용자 ID로 변경 필요)
for user_id in 1 2 3 4 5 6 7 8 9 10 123 244 304 308 42 50 51 52 54 66 94 158; do
    echo "사용자 $user_id 캐시 클리어 중..."
    
    # Redis에서 직접 키 삭제 (Redis CLI 사용)
    redis-cli DEL "recipe_recommendations:$user_id"
    redis-cli DEL "recipe_processing:$user_id"
done

echo "Redis 캐시 클리어 완료!"
echo ""
echo "이제 새로운 추천 API를 호출하여 깔끔한 구조로 테스트해보세요:"
echo "curl -H 'Authorization: Bearer YOUR_TOKEN' http://localhost:8080/cook/recipes/recommendations" 