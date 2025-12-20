# miniAItok(Tiktok + AI agent) - Short Video Learning Platform  🎓

## 👋 Project Introduction

miniAitok video is a short video learning platform based on Spring Cloud microservices architecture, designed to provide efficient and convenient learning experiences through short video format. The platform combines modern technology stack and AI intelligent features to offer students, professionals, and other users a relaxed, interactive, and efficient learning approach.




## Technical Architecture

### 🏗️ Microservices Architecture Design

This project adopts Spring Cloud microservices architecture with the following characteristics:

- **Service Governance**: Service registration and discovery based on Eureka 
- **API Gateway**: Spring Cloud Gateway provides unified entry and routing
- **Service Communication**: Feign communication mechanism
- **Configuration Management**: Unified configuration management with spring cloud Config 
- **Circuit Breaker & Degradation**: Service fault tolerance protection with Resilence4j

### 🛠️ Core Technology Stack

| Technology Category | Technology Selection | Version | Description |
|---------|---------|------|------|
| Base Framework | Spring Boot | 3.4.5 | Microservices base framework |
| Microservices | Spring Cloud | 2024.0.1 | Microservices governance |
| RPC Framework | rgpc | 3.3.0 | High-performance RPC communication |
| Database | MySQL | 8.0.31 | Relational database |
| Connection Pool | HikariCP | 1.2.24 | Database connection pool |
| ORM Framework |  Jpa/Hibernate | 3.5.5 | Data persistence layer |
| Cache | Redis | - | Distributed caching |
| Search Engine | Elasticsearch | 8.x | Full-text search |
| Message Queue | RabbitMQ | - | Asynchronous message processing |
| File Storage | minio | - | Object storage service |
| Video Processing | FFmpeg | 3.0.1 | Video transcoding processing |
| email Service | google email | - | email verification code |
| AI Integration | Spring AI | - | Intelligent chat functionality |
| JSON Processing | Jackson |  | JSON serialization |
| Authentication & Authorization | JWT | 0.12.7 | User authentication |
| Containerization | Docker | - | Application containerization |

## 📁 Project Structure

```
aitok/
├── docker/                    # Docker deployment configuration
│   ├── mysql/                 # MySQL container configuration
│   ├── redis/                 # Redis container configuration
│   ├── aitok/               # Application container configuration
│   └── docker-compose.yml    # Container orchestration file
├── aitok-common/            # Common modules
│   ├── aitok-common-core/   # Core utility classes
│   ├── aitok-common-cache/  # Cache components
│   └── aitok-common-ai/     # AI-related common classes
├── aitok-feign/             # Feign remote calls
├── aitok-gateway/           # API Gateway
├── aitok-model/             # Data model definitions
├── aitok-service/           # Business service modules
│   ├── aitok-service-ai/    # AI chat service
│   ├── aitok-service-member/ # User service
│   ├── aitok-service-video/  # Video service
│   ├── aitok-service-social/ # Social service
│   ├── aitok-service-behave/ # Behavior analysis service
│   ├── aitok-service-search/ # Search service
│   ├── aitok-service-recommend/ # Recommendation service
│   ├── aitok-service-creator/ # Creator center
│   └── aitok-service-notice/ # Notification service
├── aitok-starter/           # Auto-configuration modules
│   ├── aitok-starter-file/  # File storage
│   ├── aitok-starter-email/   # email service
│   └── aitok-starter-video/ # Video processing
├── aitok-tools/             # Tool modules
│   └── aitok-tools-es/      # ES tool encapsulation
└── pom.xml                   # Maven parent project configuration
```

## 🚀 Core Feature Modules

### 1. 🎬 Video Service (aitok-service-video)

**Core Features:**
- Video upload and storage (based on minio)
- Video transcoding processing (based on FFmpeg)
- Video chunk upload
- Video category management
- Video tag system
- Automatic video cover generation
- Video information management
- Related video recommendations

**Technical Features:**
- Supports multiple video format transcoding
- Automatic video cover screenshot generation
- Distributed file storage
- Video metadata extraction

### 2. 👥 User Service (aitok-service-member)

**Core Features:**
- User registration and login
- SMS verification code login
- User information management
- Avatar upload
- Password management
- User authentication and authorization
- Login strategy pattern

**Technical Features:**
- JWT token authentication
- Multiple login methods (username/password, mobile verification code)
- Strategy pattern for login logic
- User information cache optimization

### 3. 🤝 Social Service (aitok-service-social)

**Core Features:**
- User follow/unfollow
- Follower management
- Following feed push
- Social relationship maintenance
- Dynamic timeline

**Technical Features:**
- Push-pull combined dynamic distribution
- Redis implementation for follow relationship caching
- Asynchronous message notification

### 4. 📊 Behavior Analysis Service (aitok-service-behave)

**Core Features:**
- Video like/unlike
- Video collection management
- Comment system (supports replies)
- User behavior data statistics
- Watch history records

**Technical Features:**
- Real-time behavior data updates
- Distributed lock for preventing duplicate operations
- Asynchronous behavior data synchronization

### 5. 🔍 Search Service (aitok-service-search)

**Core Features:**
- Video full-text search
- User search
- Search suggestions and auto-completion
- Search history management
- IK analyzer support

**Technical Features:**
- Elasticsearch full-text retrieval
- Intelligent search suggestions
- Search result highlighting
- Multi-field joint search

### 6. 🎯 Recommendation Service (aitok-service-recommend)

**Core Features:**
- Personalized video recommendations
- Hot video recommendations
- Interest-based content push
- Recommendation algorithm optimization

**Technical Features:**
- Multi-dimensional recommendation algorithms
- User profile analysis
- Real-time recommendation updates

### 7. 🤖 AI Chat Service (aitok-service-ai)

**Core Features:**
- Intelligent dialogue system
- Streaming message responses
- Dialogue context management
- Knowledge base Q&A
- Multi-model support

**Technical Features:**
- Spring AI framework integration
- Streaming response for optimized user experience
- Support for multiple AI models
- Knowledge base RAG retrieval

### 8. 🎨 Creator Center (aitok-service-creator)

**Core Features:**
- Content creation management
- Video collection management
- Creation data statistics
- Material library management

### 9. 🔔 Notification Service (aitok-service-notice)

**Core Features:**
- System message push
- Interactive message notifications
- Message queue processing

### 10. 🌐 API Gateway (aitok-gateway)

**Core Features:**
- Request routing and forwarding
- Unified authentication and authorization
- Rate limiting and circuit breaking
- Cross-origin handling
- Request logging

**Technical Features:**
- JWT token validation
- Custom filter chain
- Dynamic routing configuration
- Unified exception handling

## 🛠️ Technical Components

### File Storage Component (aitok-starter-file)
- Supports Alibaba Cloud OSS and Qiniu Cloud storage
- Automatic file upload service configuration
- Multiple file type support
- Chunk upload functionality

### Email Service Component (aitok-starter-email)
- google email service integration
- Multiple email template support
- Verification code generation and validation

### Video Processing Component (aitok-starter-video)
- FFmpeg video processing
- Video information extraction
- Cover image generation
- Video format conversion

### Elasticsearch Tool (aitok-tools-es)
- ES client encapsulation
- Index management tools
- Query builder

## 🎯 Business Features

### 1. Intelligent Recommendation Algorithm
- Collaborative filtering based on user behavior
- Content feature analysis
- Real-time recommendation strategy adjustment

### 2. Social Learning
- Follow mechanism enhances user engagement
- Interactive comments promote communication
- Learning community atmosphere building

### 3. AI-Assisted Learning
- Intelligent Q&A system
- Personalized learning suggestions
- Knowledge point association recommendations

### 4. Multimedia Support
- High-quality video processing
- Automatic cover generation
- Multi-format compatibility

## 🚀 Quick Start

### Environment Requirements
- JDK 21+
- Maven 3.9+
- MySQL 8.0+
- Redis 6.0+
- Elasticsearch 8.x
- Minio 8.x
- Docker & Docker Compose

### Local Development



1. **Start Base Services**
```bash
# Use Docker Compose to start MySQL, Redis and other base services
docker-compose -f docker/docker-compose.yml up -d
```

2. **Build Project**
```bash
mvn clean package -DskipTests
```

3. **Start Services**
```bash
# Start each service module sequentially

mvn spring-boot:run -pl aitok-grpc, eureka, config, gateway server
mvn spring-boot:run -pl aitok-service/aitok-service-member
mvn spring-boot:run -pl aitok-service/aitok-service-video
# ... other services
```

### Docker Deployment

```bash
# Build and start all services
docker-compose -f docker/docker-compose.yml up -d
```
## 🔧 Configuration Guide

### Database Configuration
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/aitok_short_video_dev
    username: root
    password: your_password
```

### Redis Configuration
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password
```

### File Storage Configuration
```yaml
# Minio
minio:
    endpoint: your_endpoint
    bucketName: your_bucket
    accessKeyId: your_access_key
    accessKeySecret: your_secret


```
### Google Email Configuration
```yaml

```


