# 使用OpenJDK 21作为基础镜像
FROM openjdk:21-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制Maven配置文件
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# 复制源代码
COPY src ./src

# 构建应用（使用Maven wrapper）
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# 运行时参数
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC"

# 启动应用
CMD ["java", "-jar", "target/demo-ai.jar"]