# FINAL ENTERPRISE-GRADE TECHNOLOGY STACK
## Facebook-Like Social Media Platform (Distributed Architecture)

### 🧩 ARCHITECTURE STYLE
- **Microservices Architecture**
- **Event-Driven Architecture (EDA)**
- **Service Mesh–enabled Kubernetes Deployment**
- Each service is:
    - Independently deployable
    - Owns its own database
    - Communicates via Kafka (async) and REST/GraphQL (sync)

### 🖥️ Frontend (Client Layer)
- **Vue.js 3**
- **Vite**
- **Pinia**
- **Vue Router**
- **Tailwind CSS / Vuetify**
- **Apollo Client (GraphQL)**
- **WebSocket Client (STOMP)**

### ⚙️ Backend — Microservices Layer
Each service is a separate Spring Boot application.

#### 🔐 1️⃣ Auth Service
- **Responsibilities**: OAuth login (Google / Facebook), JWT issuance (access + refresh), Token validation, User identity
- **Tech**: Java 17, Spring Boot, Spring Security, OAuth 2.0 Client, JWT, PostgreSQL, Redis (token cache), Flyway

#### 👤 2️⃣ User Service
- **Responsibilities**: User profiles, Friends / followers, Privacy settings
- **Tech**: Spring Boot, GraphQL, PostgreSQL, Flyway

#### 📰 3️⃣ Feed Service
- **Responsibilities**: Post creation, Feed aggregation, Feed ranking logic
- **Tech**: Spring Boot, GraphQL, PostgreSQL, Redis (feed cache), Elasticsearch, Kafka (consume user activity events), Flyway

#### 💬 4️⃣ Notification Service
- **Responsibilities**: Real-time notifications, Email / push notifications, Event consumption
- **Tech**: Spring Boot, Kafka (consumer), WebSocket (STOMP), PostgreSQL, Redis

#### 🖼️ 5️⃣ Media Service
- **Responsibilities**: Image / video upload, Media metadata, Access control
- **Tech**: Spring Boot, REST API, MinIO (object storage), PostgreSQL (metadata)

### 🔁 EVENT STREAMING & MESSAGING
- **Apache Kafka**
    - **Topics**: user-events, post-events, reaction-events, notification-events, audit-events
    - **Producers**: Auth Service, User Service, Feed Service
    - **Consumers**: Feed Service, Notification Service, Analytics (future)

### 🌐 API COMMUNICATION
- **Synchronous**: REST (Auth, Media), GraphQL (User, Feed)
- **Asynchronous**: Kafka event streams

### ☸️ SERVICE MESH
- **Istio** (Envoy sidecar proxy, Istiod control plane)
- **Features**: mTLS, Traffic routing, Retries & timeouts, Circuit breaking, Telemetry
- **No application code changes required**

### ☸️ KUBERNETES & INFRASTRUCTURE
- **Cluster**: Kubernetes, Minikube / Kind (local)
- **Networking**: Istio Ingress Gateway
- **Scaling**: Horizontal Pod Autoscaler (HPA)

### 🗄️ DATA LAYER (Per-Service Isolation)
- **Auth**: PostgreSQL + Redis
- **User**: PostgreSQL
- **Feed**: PostgreSQL + Redis + Elasticsearch
- **Notification**: PostgreSQL + Redis
- **Media**: MinIO + PostgreSQL
- **Rule**: ✔ No shared databases

### 🔐 SECURITY (ZERO-TRUST MODEL)
- OAuth 2.0, JWT, RBAC, mTLS (Istio), Rate limiting, CORS, Network policies

---

## ☁️ FULL AWS DEPLOYMENT ARCHITECTURE
**Enterprise-Grade Cloud-Native System**

### 1️⃣ HIGH-LEVEL CLOUD ARCHITECTURE
Users -> AWS ALB -> Istio Ingress Gateway (EKS) -> Microservices -> Kafka/DBs/S3

### 2️⃣ AWS ACCOUNT & REGION SETUP
- Region: **ap-southeast-1 (Singapore)**

### 3️⃣ NETWORKING
- **VPC** (10.0.0.0/16)
- **Subnets**: Public (ALB), Private (EKS, Kafka), Isolated (RDS)

### 4️⃣ CONTAINER ORCHESTRATION
- **Amazon EKS** (Kubernetes v1.28+)
- **Node Groups**: general-ng (microservices), kafka-ng (brokers), infra-ng (Istio, monitoring)

### 5️⃣ SERVICE MESH
- **Istio on EKS** (mTLS STRICT, Traffic routing, Telemetry)

### 6️⃣ MICROSERVICES DEPLOYMENT
- Dockerized, Kubernetes Deployment, HPA, Sidecar-injected

### 7️⃣ EVENT STREAMING
- **Apache Kafka (AWS)**: Self-managed on EC2 (recommended) OR Amazon MSK.
- **Topics**: auth-events, user-events, post-events, reaction-events, notification-events

### 8️⃣ DATA LAYER
- **Amazon RDS (PostgreSQL)**: Separate DB per service.
- **Redis**: Self-managed or ElastiCache.
- **Amazon S3**: For media storage.

### 9️⃣ SECURITY
- OAuth 2.0, JWT, IAM Roles, Security Groups, Istio mTLS, AWS Secrets Manager.

### 1️⃣0️⃣ OBSERVABILITY
- Prometheus, Grafana, Jaeger (distributed tracing), SLF4J + Logback.

### 1️⃣1️⃣ CI/CD PIPELINE
- GitHub Actions -> ECR -> EKS.

---

## 📅 Week 1 — Architecture & Scope Lock
**Goals**:
1. Lock final system architecture.
2. Define clear microservice boundaries.
3. Prevent scope creep.

### 🧱 MICROservice RESPONSIBILITY TABLE (LOCKED)
| Service | Responsibilities | Does NOT Do |
| :--- | :--- | :--- |
| **Auth** | OAuth, JWT, Validation | Profiles, Posts |
| **User** | Profiles, Friends, Privacy | Auth |
| **Feed** | Posts, Aggregation, Ranking | Auth Logic |
| **Notification** | Real-time, Alerts | Feed Generation |
| **Media** | Uploads, Metadata | Feed or Auth |

### 🌊 KAFKA TOPIC DESIGN
- `auth-events`: Login/Logout
- `user-events`: Profile updates
- `post-events`: Post creation
- `reaction-events`: Likes/Comments
- `notification-events`: Delivery status

### 🧷 SCOPE LOCK RULES
- No additional microservices, messaging systems, or cloud services after Week 1.
