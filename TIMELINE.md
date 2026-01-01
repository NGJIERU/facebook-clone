# 📅 14-Week Project Timeline

## 🧭 PHASE 1 — FOUNDATION & DESIGN (Weeks 1–2)
### Week 1 — Architecture & Scope Lock
- **Goals**: Lock final architecture, Define microservice boundaries.
- **Deliverables**: System architecture diagram, Microservice responsibility table, Technology stack justification, Kafka topic design.

### Week 2 — AWS Fundamentals & Local Readiness
- **Goals**: Understand AWS basics, Prepare local development environment.
- **Tasks**:
    - AWS account setup
    - IAM users & roles (basic)
    - Learn VPC, EC2, security groups
    - Dockerize one microservice locally

## ☸️ PHASE 2 — CLOUD INFRASTRUCTURE (Weeks 3–4)
### Week 3 — AWS Networking (CRITICAL WEEK)
- **Goals**: Build production-style network.
- **Tasks**: Create VPC, Public & private subnets, IGW & NAT GW, Security Groups.
- **Deliverables**: VPC diagram, Working EC2 in private subnet.

### Week 4 — Amazon EKS Setup
- **Goals**: Kubernetes cluster running on AWS.
- **Tasks**: Create EKS cluster, Node groups, Configure kubectl, Deploy test Nginx pod.
- **Deliverables**: EKS cluster operational, Pods reachable.

## 🧩 PHASE 3 — CORE MICROSERVICES (Weeks 5–7)
### Week 5 — First Microservice on EKS (Auth)
- **Goals**: Prove end-to-end deployment.
- **Tasks**: Deploy Auth Service to EKS, Connect to RDS, Secrets Manager, Load Balancer.
- **Deliverables**: Auth Service live, OAuth login working.

### Week 6 — User & Feed Services
- **Goals**: Introduce service interaction.
- **Tasks**: Deploy User & Feed Services, GraphQL endpoints, Inter-service communication.
- **Deliverables**: Profile creation, Post creation.

### Week 7 — Kafka Integration
- **Goals**: Introduce event-driven architecture.
- **Tasks**: Deploy Kafka (EC2/MSK), Define topics, Producer/consumer logic.
- **Deliverables**: Kafka running, Async feed updates.

## 🕸️ PHASE 4 — SERVICE MESH & REAL-TIME (Weeks 8–9)
### Week 8 — Istio Service Mesh
- **Goals**: Secure service-to-service traffic.
- **Tasks**: Install Istio, Sidecar injection, mTLS, Traffic routing.
- **Deliverables**: Istio dashboards, mTLS verified.

### Week 9 — Notification & Media Services
- **Goals**: Real-time & media handling.
- **Tasks**: Deploy Notification & Media Services, WebSocket, S3 integration.
- **Deliverables**: Real-time notifications, Image upload.

## 🔐 PHASE 5 — SECURITY, OBSERVABILITY & CI/CD (Weeks 10–11)
### Week 10 — Security Hardening
- **Goals**: Production-grade security.
- **Tasks**: RBAC, Rate limiting, Secrets Manager, Istio policies.

### Week 11 — Monitoring & CI/CD
- **Goals**: Operability & automation.
- **Tasks**: Prometheus, Grafana, Jaeger, GitHub Actions, ECR.

## 🧪 PHASE 6 — TESTING & VALIDATION (Weeks 12–13)
### Week 12 — Testing Implementation
- **Goals**: QA.
- **Tasks**: Unit/Integration tests, Security tests.

### Week 13 — System Validation & Optimization
- **Goals**: Stability.
- **Tasks**: End-to-end testing, Failure simulation, Performance observation.

## 📝 PHASE 7 — FINALIZATION (Week 14)
### Week 14 — Report, Demo & Viva Prep
- **Goals**: Convert work into marks.
- **Tasks**: Final report, Diagrams, Demo rehearsal.
