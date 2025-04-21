#!/bin/bash
set -e

# 환경 변수 파일 로드 함수
load_env() {
    if [ -f .env ]; then
        export $(cat .env | grep -v '#' | awk '/=/ {print $1}')
    else
        echo -e "${RED}.env 파일이 없습니다. .env.template 파일을 복사하여 .env 파일을 생성해주세요.${NC}"
        exit 1
    fi
}

# 환경 변수 검증 함수
validate_env() {
    if [ -z "$AWS_ACCOUNT_ID" ]; then
        echo -e "${RED}AWS_ACCOUNT_ID가 설정되지 않았습니다. .env 파일을 확인해주세요.${NC}"
        exit 1
    fi
    if [ -z "$AWS_REGION" ]; then
        echo -e "${RED}AWS_REGION이 설정되지 않았습니다. .env 파일을 확인해주세요.${NC}"
        exit 1
    fi
    if [ -z "$ECR_REPOSITORY_NAME" ]; then
        echo -e "${RED}ECR_REPOSITORY_NAME이 설정되지 않았습니다. .env 파일을 확인해주세요.${NC}"
        exit 1
    fi
}

# ENVIRONMENT 값 검증 및 변환 함수
validate_environment() {
    # ENVIRONMENT가 설정되지 않은 경우 기본값 설정
    if [ -z "$ENVIRONMENT" ]; then
        echo -e "${GREEN}ENVIRONMENT가 설정되지 않아 기본값 'development'로 설정됩니다.${NC}"
        ENVIRONMENT="development"
    fi

    # ENVIRONMENT 값에 따라 변환
    case "$ENVIRONMENT" in
        "dev")
            ENVIRONMENT="development"
            echo -e "${GREEN}ENVIRONMENT 'dev'가 'development'로 변환되었습니다.${NC}"
            ;;
        "test"|"development")
            # 이미 올바른 값이므로 그대로 유지
            ;;
        *)
            echo -e "${RED}지원하지 않는 ENVIRONMENT 값입니다: $ENVIRONMENT${NC}"
            echo -e "${GREEN}기본값 'development'로 설정됩니다.${NC}"
            ENVIRONMENT="development"
            ;;
    esac
}

# 색상 코드 설정
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 환경 변수 로드
echo -e "${GREEN}=== 환경 변수 로드 중 ===${NC}"
load_env
validate_env
validate_environment

echo -e "${GREEN}=== 배포 정보 ===${NC}"
echo "AWS Account ID: $AWS_ACCOUNT_ID"
echo "AWS Region: $AWS_REGION"
echo "ECR Repository: $ECR_REPOSITORY_NAME"
echo "Environment: $ENVIRONMENT"
echo "----------------------------------------"

# ECR 로그인
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

# Docker 이미지 빌드
IMAGE_TAG="${ECR_REPOSITORY_NAME}:${ENVIRONMENT}"
ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}"

echo -e "${GREEN}=== Docker 이미지 빌드 중 ===${NC}"
./gradlew bootBuildImage --imageName=${IMAGE_TAG}


if [ $? -ne 0 ]; then
    echo -e "${RED}Docker 이미지 빌드 실패${NC}"
    exit 1
fi

# ECR에 이미지 태그 지정
echo -e "${GREEN}=== 이미지 태그 지정 중 ===${NC}"
docker tag ${IMAGE_TAG} ${ECR_URI}:${ENVIRONMENT}

# ECR에 이미지 푸시
echo -e "${GREEN}=== 이미지 푸시 중 ===${NC}"
if docker push ${ECR_URI}:${ENVIRONMENT}; then
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}✅ 배포가 성공적으로 완료되었습니다!${NC}"
    echo -e "${GREEN}이미지 URI: ${ECR_URI}:${ENVIRONMENT}${NC}"
    echo -e "${GREEN}========================================${NC}"
else
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}❌ 이미지 푸시에 실패했습니다.${NC}"
    echo -e "${RED}다음 사항을 확인해주세요:${NC}"
    echo -e "${RED}1. AWS 자격 증명이 올바른지 확인${NC}"
    echo -e "${RED}2. ECR 리포지토리가 존재하는지 확인${NC}"
    echo -e "${RED}3. 네트워크 연결 상태 확인${NC}"
    echo -e "${RED}========================================${NC}"
    exit 1
fi
