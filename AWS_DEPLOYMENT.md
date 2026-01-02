# ☁️ AWS Deployment Guide - Complete Walkthrough

This comprehensive guide walks you through deploying the Facebook Clone platform to Amazon Web Services (AWS) using Kubernetes (EKS), Istio service mesh, and managed AWS services.

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Architecture](#architecture)
4. [Cost Estimates](#cost-estimates)
5. [Step-by-Step Deployment](#step-by-step-deployment)
6. [Post-Deployment Configuration](#post-deployment-configuration)
7. [Monitoring & Observability](#monitoring--observability)
8. [Security Hardening](#security-hardening)
9. [Backup & Disaster Recovery](#backup--disaster-recovery)
10. [Troubleshooting](#troubleshooting)
11. [Scaling & Performance](#scaling--performance)

---

## Overview

### What We're Building

A production-ready, highly available social media platform running on:
- **Amazon EKS**: Managed Kubernetes for container orchestration
- **Istio Service Mesh**: For secure service-to-service communication
- **Amazon RDS**: Managed PostgreSQL databases
- **Amazon ElastiCache**: Managed Redis for caching
- **Amazon MSK or EC2-hosted Kafka**: Event streaming
- **Amazon S3**: Object storage for media files
- **Application Load Balancer**: Traffic distribution
- **CloudWatch**: Logging and monitoring

### Deployment Timeline

- **Infrastructure Setup**: 30-45 minutes
- **Service Deployment**: 15-20 minutes
- **Configuration & Testing**: 20-30 minutes
- **Total**: ~2 hours for first deployment

---

## Prerequisites

### 1. AWS Account Setup

```bash
# Create AWS account at https://aws.amazon.com
# Enable billing alerts
# Set up MFA for root account

# Create IAM user with administrative access
# Download access keys
```

### 2. Required Tools

```bash
# Install AWS CLI
brew install awscli  # macOS
# or: curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"

# Verify installation
aws --version  # Should be 2.x or higher

# Configure AWS CLI
aws configure
# Enter when prompted:
# - AWS Access Key ID: <your-access-key>
# - AWS Secret Access Key: <your-secret-key>
# - Default region: ap-southeast-1
# - Default output format: json

# Test configuration
aws sts get-caller-identity
```

```bash
# Install Terraform
brew install terraform  # macOS
# or: https://www.terraform.io/downloads

terraform --version  # Should be 1.5 or higher
```

```bash
# Install kubectl
brew install kubectl  # macOS
# or: https://kubernetes.io/docs/tasks/tools/

kubectl version --client
```

```bash
# Install Helm (Kubernetes package manager)
brew install helm  # macOS
# or: https://helm.sh/docs/intro/install/

helm version
```

```bash
# Install Istioctl
curl -L https://istio.io/downloadIstio | sh -
cd istio-1.28.2
export PATH=$PWD/bin:$PATH
istioctl version
```

### 3. Docker Setup

```bash
# Ensure Docker is running
docker --version
docker ps

# Test Docker
docker run hello-world
```

---

## Architecture

### Network Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         INTERNET                             │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
            ┌──────────────────────┐
            │     AWS WAF          │ (Optional - DDoS protection)
            └──────────┬───────────┘
                       │
                       ▼
            ┌──────────────────────┐
            │ Application Load     │
            │     Balancer (ALB)   │
            └──────────┬───────────┘
                       │
   ┌───────────────────┴────────────────────┐
   │                VPC                      │
   │         (10.0.0.0/16)                   │
   │                                         │
   │  ┌─────────────────────────────────┐   │
   │  │    Public Subnets               │   │
   │  │    (10.0.1.0/24, 10.0.2.0/24)   │   │
   │  │    - NAT Gateways               │   │
   │  │    - Bastion Host (Optional)    │   │
   │  └─────────────────────────────────┘   │
   │                                         │
   │  ┌─────────────────────────────────┐   │
   │  │    Private Subnets              │   │
   │  │    (10.0.10.0/24, 10.0.11.0/24) │   │
   │  │                                  │   │
   │  │  ┌────────────────────────┐     │   │
   │  │  │   EKS Cluster          │     │   │
   │  │  │                        │     │   │
   │  │  │  ┌──────────────────┐ │     │   │
   │  │  │  │ Istio Gateway    │ │     │   │
   │  │  │  └────────┬─────────┘ │     │   │
   │  │  │           │            │     │   │
   │  │  │  ┌────────▼─────────┐ │     │   │
   │  │  │  │  Microservices   │ │     │   │
   │  │  │  │  - Auth Service  │ │     │   │
   │  │  │  │  - User Service  │ │     │   │
   │  │  │  │  - Feed Service  │ │     │   │
   │  │  │  │  - Media Service │ │     │   │
   │  │  │  │  - Notif Service │ │     │   │
   │  │  │  └──────────────────┘ │     │   │
   │  │  │                        │     │   │
   │  │  │  ┌──────────────────┐ │     │   │
   │  │  │  │  Kafka Brokers   │ │     │   │
   │  │  │  └──────────────────┘ │     │   │
   │  │  └────────────────────────┘     │   │
   │  └─────────────────────────────────┘   │
   │                                         │
   │  ┌─────────────────────────────────┐   │
   │  │    Isolated Subnets             │   │
   │  │    (10.0.20.0/24, 10.0.21.0/24) │   │
   │  │    - RDS (PostgreSQL)           │   │
   │  │    - ElastiCache (Redis)        │   │
   │  └─────────────────────────────────┘   │
   │                                         │
   └─────────────────────────────────────────┘
                       │
                       ▼
            ┌──────────────────────┐
            │     Amazon S3        │
            │   (Media Storage)    │
            └──────────────────────┘
```

### Service Distribution

| Component | AWS Service | HA Configuration |
|-----------|-------------|------------------|
| **Load Balancing** | Application Load Balancer | Multi-AZ |
| **Compute** | EKS (EC2 nodes) | 3+ nodes across AZs |
| **Database** | RDS PostgreSQL | Multi-AZ with read replicas |
| **Cache** | ElastiCache Redis | Cluster mode with replicas |
| **Message Queue** | MSK (Kafka) | 3 brokers across AZs |
| **Object Storage** | S3 | 99.999999999% durability |
| **Service Mesh** | Istio on EKS | Distributed across nodes |

---

## Cost Estimates

### Monthly Cost Breakdown (Production Environment)

#### Compute Layer
| Resource | Type | Quantity | Monthly Cost |
|----------|------|----------|--------------|
| EKS Control Plane | - | 1 | $72 |
| EC2 Worker Nodes | t3.medium | 3 | $75 |
| EC2 Kafka Nodes | m5.large | 3 | $244 |
| NAT Gateway | - | 2 | $64 |
| **Subtotal** | | | **$455** |

#### Data Layer
| Resource | Type | Quantity | Monthly Cost |
|----------|------|----------|--------------|
| RDS PostgreSQL | db.t3.medium Multi-AZ | 5 instances | $250 |
| RDS Read Replicas | db.t3.medium | 2 | $100 |
| ElastiCache Redis | cache.t3.micro | 2 nodes | $50 |
| S3 Storage | Standard | 500 GB | $11 |
| S3 Requests | - | 1M requests | $4 |
| **Subtotal** | | | **$415** |

#### Networking
| Resource | Type | Monthly Cost |
|----------|------|--------------|
| Application Load Balancer | - | $22 |
| Data Transfer Out | 100 GB | $9 |
| **Subtotal** | | **$31** |

#### Monitoring & Security
| Resource | Type | Monthly Cost |
|----------|------|--------------|
| CloudWatch Logs | 50 GB | $25 |
| CloudWatch Metrics | Custom | $10 |
| AWS Secrets Manager | 10 secrets | $4 |
| **Subtotal** | | **$39** |

### **Total Monthly Cost: ~$940**

### Development Environment (Cost Optimized)
- Single AZ deployment
- Smaller instance types (t3.small)
- No read replicas
- **Estimated Cost: $200-300/month**

### Cost Optimization Strategies
1. **Spot Instances**: Save 50-70% on EC2 costs for stateless workloads
2. **Reserved Instances**: Save 30-50% for steady-state workloads (1-yr commitment)
3. **S3 Intelligent-Tiering**: Automatic cost optimization for storage
4. **Auto Scaling**: Scale down during off-peak hours
5. **Aurora Serverless**: Pay per use for databases with variable load

---

## Step-by-Step Deployment

### Phase 1: Infrastructure Setup (Terraform)

#### Step 1.1: Prepare Terraform Configuration

```bash
cd infrastructure

# Review variables
cat main.tf

# Expected output:
# - Region: ap-southeast-1
# - Cluster name: facebook-cluster
# - VPC CIDR: 10.0.0.0/16
```

#### Step 1.2: Initialize Terraform

```bash
# Download providers and modules
terraform init

# Expected output:
# - AWS provider downloaded
# - VPC module initialized
# - EKS module initialized
```

#### Step 1.3: Plan Infrastructure

```bash
# Generate execution plan
terraform plan -out=tfplan

# Review the plan - should create:
# - 1 VPC with subnets
# - 1 EKS cluster
# - 5 ECR repositories
# - 3 Security groups
# - 2 NAT gateways
# - IAM roles and policies
```

#### Step 1.4: Apply Infrastructure

```bash
# Create resources (takes ~20-30 minutes)
terraform apply tfplan

# Monitor progress - EKS cluster creation is the slowest part
# Watch for any errors

# Save outputs
terraform output > terraform-outputs.txt
```

**Important Outputs:**
```bash
# Cluster details
export CLUSTER_NAME=$(terraform output -raw cluster_name)
export REGION=ap-southeast-1

# ECR URLs (for Docker images)
export AUTH_ECR=$(terraform output -json ecr_repository_urls | jq -r '.auth')
export USER_ECR=$(terraform output -json ecr_repository_urls | jq -r '.user')
export FEED_ECR=$(terraform output -json ecr_repository_urls | jq -r '.feed')
```

#### Step 1.5: Configure kubectl

```bash
# Update kubeconfig to connect to EKS
aws eks update-kubeconfig \
  --region $REGION \
  --name $CLUSTER_NAME

# Verify connection
kubectl get nodes

# Expected output:
# NAME                           STATUS   ROLES    AGE   VERSION
# ip-10-0-10-123.ec2.internal   Ready    <none>   5m    v1.29.x
# ip-10-0-10-124.ec2.internal   Ready    <none>   5m    v1.29.x
# ip-10-0-10-125.ec2.internal   Ready    <none>   5m    v1.29.x
```

### Phase 2: Container Registry Setup

#### Step 2.1: Authenticate Docker to ECR

```bash
# Get ECR login token
aws ecr get-login-password --region $REGION | \
  docker login --username AWS --password-stdin \
  $(echo $AUTH_ECR | cut -d/ -f1)

# Verify login
echo "Login Succeeded"
```

#### Step 2.2: Build and Push Images

```bash
cd ..  # Return to project root

# Build Auth Service
docker build -t facebook-auth:latest backend/auth-service
docker tag facebook-auth:latest $AUTH_ECR:latest
docker push $AUTH_ECR:latest

# Build User Service
docker build -t facebook-user:latest backend/user-service
docker tag facebook-user:latest $USER_ECR:latest
docker push $USER_ECR:latest

# Build Feed Service
docker build -t facebook-feed:latest backend/feed-service
docker tag facebook-feed:latest $FEED_ECR:latest
docker push $FEED_ECR:latest

# Build Media Service
export MEDIA_ECR=$(terraform output -json ecr_repository_urls | jq -r '.media')
docker build -t facebook-media:latest backend/media-service
docker tag facebook-media:latest $MEDIA_ECR:latest
docker push $MEDIA_ECR:latest

# Build Notification Service
export NOTIF_ECR=$(terraform output -json ecr_repository_urls | jq -r '.notification')
docker build -t facebook-notification:latest backend/notification-service
docker tag facebook-notification:latest $NOTIF_ECR:latest
docker push $NOTIF_ECR:latest
```

**Automated Script:**
```bash
# Use the provided script
chmod +x infrastructure/deploy-images.sh
./infrastructure/deploy-images.sh

# This builds and pushes all images
```

### Phase 3: Install Istio Service Mesh

#### Step 3.1: Download Istio

```bash
# Download Istio 1.28.2
curl -L https://istio.io/downloadIstio | ISTIO_VERSION=1.28.2 sh -
cd istio-1.28.2

# Add istioctl to PATH
export PATH=$PWD/bin:$PATH

# Verify
istioctl version
```

#### Step 3.2: Install Istio

```bash
# Install with production profile
istioctl install --set profile=production -y

# This installs:
# - Istiod (control plane)
# - Istio Ingress Gateway
# - Istio Egress Gateway

# Verify installation
kubectl get pods -n istio-system

# Expected pods:
# - istiod-xxx
# - istio-ingressgateway-xxx
# - istio-egressgateway-xxx
```

#### Step 3.3: Enable Sidecar Injection

```bash
# Label default namespace for automatic sidecar injection
kubectl label namespace default istio-injection=enabled

# Verify label
kubectl get namespace -L istio-injection
```

### Phase 4: Deploy Data Layer

#### Option A: Amazon RDS (Recommended for Production)

```bash
# Create RDS instances (one per service)
# This can be added to Terraform configuration

# Auth DB
aws rds create-db-instance \
  --db-instance-identifier facebook-auth-db \
  --db-instance-class db.t3.medium \
  --engine postgres \
  --master-username admin \
  --master-user-password <SECURE_PASSWORD> \
  --allocated-storage 20 \
  --vpc-security-group-ids <SECURITY_GROUP_ID> \
  --db-subnet-group-name <SUBNET_GROUP> \
  --multi-az \
  --backup-retention-period 7 \
  --tags Key=Service,Value=auth

# Repeat for user-db, feed-db, media-db, notification-db

# Get RDS endpoints
aws rds describe-db-instances \
  --query 'DBInstances[*].[DBInstanceIdentifier,Endpoint.Address]' \
  --output table
```

#### Option B: PostgreSQL on Kubernetes (Development)

```bash
cd infrastructure/k8s

# Deploy PostgreSQL
kubectl apply -f postgres-deployment.yaml

# Verify
kubectl get pods -l app=postgres
kubectl get svc postgres

# Expected output:
# NAME       TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)
# postgres   ClusterIP   10.100.xxx.xxx  <none>        5432/TCP
```

#### Deploy Redis

```bash
# For production, use ElastiCache
# For development, deploy on K8s

kubectl apply -f redis-deployment.yaml

# Verify
kubectl get pods -l app=redis
```

#### Deploy Kafka

```bash
# Deploy Zookeeper first
kubectl apply -f kafka-deployment.yaml

# Wait for Zookeeper to be ready
kubectl wait --for=condition=ready pod -l app=zookeeper --timeout=300s

# Kafka pods will start automatically

# Verify
kubectl get pods -l app=kafka
kubectl get svc kafka
```

### Phase 5: Deploy Microservices

#### Step 5.1: Create Secrets

```bash
# Create Kubernetes secrets
kubectl create secret generic jwt-secret \
  --from-literal=secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

kubectl create secret generic database-credentials \
  --from-literal=username=admin \
  --from-literal=password=<YOUR_SECURE_PASSWORD>

# Verify secrets
kubectl get secrets
```

#### Step 5.2: Update Deployment Files

```bash
# Edit deployment files to use ECR image URLs
# Example for auth-service:

sed -i '' "s|image: facebook-auth:latest|image: $AUTH_ECR:latest|g" \
  infrastructure/k8s/auth-deployment.yaml

# Repeat for all services
```

#### Step 5.3: Deploy Services

```bash
cd infrastructure/k8s

# Deploy all services
kubectl apply -f auth-deployment.yaml
kubectl apply -f user-deployment.yaml
kubectl apply -f feed-deployment.yaml
kubectl apply -f media-deployment.yaml
kubectl apply -f notification-deployment.yaml

# Verify deployments
kubectl get deployments

# Check pod status
kubectl get pods

# Wait for all pods to be Running
kubectl wait --for=condition=ready pod --all --timeout=300s
```

#### Step 5.4: Verify Service Health

```bash
# Check service endpoints
kubectl get svc

# Port-forward to test (optional)
kubectl port-forward svc/auth-service 8080:8080 &
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### Phase 6: Configure Istio Gateway

#### Step 6.1: Deploy Gateway and VirtualServices

```bash
# Deploy Istio ingress configuration
kubectl apply -f infrastructure/k8s/istio-gateway.yaml

# This creates:
# - Gateway: Entry point for external traffic
# - VirtualServices: Route rules for each service
```

#### Step 6.2: Get Load Balancer URL

```bash
# Get external IP/DNS of Istio Gateway
kubectl get svc istio-ingressgateway -n istio-system

# Note the EXTERNAL-IP or EXTERNAL-HOSTNAME
export LB_URL=$(kubectl get svc istio-ingressgateway -n istio-system \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

echo "Load Balancer URL: $LB_URL"
```

#### Step 6.3: Test Gateway

```bash
# Test authentication endpoint
curl -X POST http://$LB_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test1234!"
  }'

# Expected: User created successfully
```

### Phase 7: Deploy Frontend

#### Option A: Amazon S3 + CloudFront (Recommended)

```bash
# Build production frontend
cd frontend
npm run build

# Create S3 bucket
aws s3 mb s3://facebook-frontend-prod --region $REGION

# Enable static website hosting
aws s3 website s3://facebook-frontend-prod \
  --index-document index.html \
  --error-document index.html

# Upload files
aws s3 sync dist/ s3://facebook-frontend-prod/ --delete

# Make bucket public (or use CloudFront)
aws s3api put-bucket-policy --bucket facebook-frontend-prod \
  --policy '{
    "Version": "2012-10-17",
    "Statement": [{
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::facebook-frontend-prod/*"
    }]
  }'

# Create CloudFront distribution (optional, for CDN)
aws cloudfront create-distribution \
  --origin-domain-name facebook-frontend-prod.s3-website-$REGION.amazonaws.com \
  --default-root-object index.html
```

#### Option B: Deploy to Kubernetes

```bash
# Build Docker image
docker build -t facebook-frontend:latest .

# Push to ECR
export FRONTEND_ECR=<YOUR_FRONTEND_ECR_URL>
docker tag facebook-frontend:latest $FRONTEND_ECR:latest
docker push $FRONTEND_ECR:latest

# Deploy to K8s
kubectl apply -f infrastructure/k8s/frontend-deployment.yaml
```

---

## Post-Deployment Configuration

### 1. DNS Setup

```bash
# Option A: Route 53
# Create hosted zone
aws route53 create-hosted-zone --name yourdomain.com --caller-reference $(date +%s)

# Create A record for API
aws route53 change-resource-record-sets \
  --hosted-zone-id <ZONE_ID> \
  --change-batch '{
    "Changes": [{
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "api.yourdomain.com",
        "Type": "A",
        "AliasTarget": {
          "HostedZoneId": "<ALB_ZONE_ID>",
          "DNSName": "'$LB_URL'",
          "EvaluateTargetHealth": false
        }
      }
    }]
  }'

# Option B: External DNS Provider
# Create CNAME record: api.yourdomain.com -> $LB_URL
```

### 2. SSL/TLS Certificate

```bash
# Request certificate from ACM
aws acm request-certificate \
  --domain-name api.yourdomain.com \
  --validation-method DNS \
  --region $REGION

# Get certificate ARN
export CERT_ARN=$(aws acm list-certificates \
  --query 'CertificateSummaryList[0].CertificateArn' \
  --output text)

# Add certificate to ALB
# Update Istio Gateway configuration to use ACM certificate
```

### 3. Environment Variables

```bash
# Update frontend API endpoint
kubectl set env deployment/frontend \
  VITE_API_URL=https://api.yourdomain.com

# Restart frontend
kubectl rollout restart deployment/frontend
```

---

## Monitoring & Observability

### Install Monitoring Stack

```bash
# Add Prometheus Helm repo
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install Prometheus + Grafana
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace

# Access Grafana
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80

# Login: admin / prom-operator
# Open: http://localhost:3000
```

### Configure Istio Telemetry

```bash
# Enable Istio metrics
kubectl apply -f - <<EOF
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  meshConfig:
    defaultConfig:
      tracing:
        zipkin:
          address: jaeger-collector.istio-system:9411
EOF
```

---

## Security Hardening

### 1. Network Policies

```bash
# Apply network policies
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-all
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
EOF
```

### 2. Pod Security Standards

```bash
# Enable pod security
kubectl label namespace default pod-security.kubernetes.io/enforce=baseline
```

---

This guide provides a comprehensive deployment walkthrough. For troubleshooting and advanced configurations, refer to the main README.md and ARCHITECTURE.md files.

**Deployment Complete! 🎉**
