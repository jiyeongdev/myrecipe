# 1. OpenJDK 기반 베이스 이미지 선택
FROM amazoncorretto:17-alpine

# 2. Spring 전용 사용자 생성
RUN addgroup -S spring && adduser -S spring -G spring
# 3. 일반 사용자로 실행
USER spring:spring
# 4. JAR 파일 복사
ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} app.jar
# 5. 엔트리포인트 설정
ENTRYPOINT ["java","-jar","/app.jar"]