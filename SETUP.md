# 🚀 Complete Setup Guide - Facebook Clone

This guide will walk you through setting up the Facebook Clone project from GitHub, whether you've never worked with GitHub before or are an experienced developer.

---

## 📋 Table of Contents
- [Prerequisites](#prerequisites)
- [Part 1: GitHub Basics](#part-1-github-basics)
- [Part 2: Local Development Setup](#part-2-local-development-setup)
- [Part 3: Running with Docker](#part-3-running-with-docker)
- [Part 4: Development Workflow](#part-4-development-workflow)
- [Part 5: Troubleshooting](#part-5-troubleshooting)

---

## Prerequisites

Before starting, ensure you have the following installed on your computer:

### Required Software
- **Git**: Version control system
  - Download from: https://git-scm.com/downloads
  - Verify installation: `git --version`
  
- **Docker Desktop**: Container runtime
  - Download from: https://www.docker.com/products/docker-desktop
  - Verify installation: `docker --version` and `docker-compose --version`
  
- **Node.js**: JavaScript runtime (v18 or higher)
  - Download from: https://nodejs.org/
  - Verify installation: `node --version` and `npm --version`
  
- **Java Development Kit (JDK)**: Version 17 or higher
  - Download from: https://adoptium.net/
  - Verify installation: `java --version`
  
- **Maven**: Java build tool
  - Download from: https://maven.apache.org/download.cgi
  - Verify installation: `mvn --version`

### Optional Tools
- **VS Code** or **IntelliJ IDEA**: Code editor
- **Postman**: API testing tool
- **pgAdmin** or **DBeaver**: Database management tool

---

## Part 1: GitHub Basics

### What is GitHub?
GitHub is a platform that stores code in **repositories** (repos). Think of it as a cloud storage for your code that also tracks changes.

### Step 1: Create a GitHub Account
1. Go to https://github.com
2. Click "Sign up"
3. Follow the registration process

### Step 2: Understanding Git Commands
Here are the basic commands you'll use:

```bash
# Clone: Download a repository from GitHub to your computer
git clone <repository-url>

# Pull: Update your local code with the latest changes from GitHub
git pull

# Status: Check what files you've changed
git status

# Add: Stage files for commit
git add .

# Commit: Save your changes with a message
git commit -m "Your message here"

# Push: Upload your changes to GitHub
git push
```

### Step 3: Clone This Repository

**Option A: Using HTTPS (Recommended for beginners)**
```bash
# Navigate to where you want to store the project
cd ~/Documents

# Clone the repository
git clone https://github.com/YOUR_USERNAME/facebook-clone.git

# Navigate into the project
cd facebook-clone
```

**Option B: Using SSH (For experienced users)**
```bash
# First, set up SSH keys (one-time setup)
ssh-keygen -t ed25519 -C "your_email@example.com"
cat ~/.ssh/id_ed25519.pub
# Copy the output and add it to GitHub: Settings > SSH and GPG keys > New SSH key

# Clone the repository
git clone git@github.com:YOUR_USERNAME/facebook-clone.git
cd facebook-clone
```

---

## Part 2: Local Development Setup

### Step 1: Understand the Project Structure

```
facebook-clone/
├── backend/                    # Backend microservices
│   ├── auth-service/          # Authentication service
│   ├── user-service/          # User profile management
│   ├── feed-service/          # News feed service
│   ├── media-service/         # Image/video uploads
│   └── notification-service/  # Real-time notifications
├── frontend/                   # Vue.js web application
│   ├── src/                   # Source code
│   ├── public/                # Static assets
│   └── package.json           # Dependencies
├── infrastructure/             # Cloud deployment configs
│   ├── k8s/                   # Kubernetes manifests
│   └── *.tf                   # Terraform files (AWS)
├── docker-compose.yml          # Local development orchestration
└── README.md                   # Project documentation
```

### Step 2: Set Up Frontend

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies (this downloads all required packages)
npm install

# This will create a node_modules folder - don't worry if it takes a few minutes!
```

### Step 3: Set Up Backend Services

Each backend service needs to be built. You can do this individually or let Docker Compose handle it (recommended).

**Manual Build (Optional - for learning)**
```bash
# Build auth-service
cd backend/auth-service
mvn clean install -DskipTests
cd ../..

# Repeat for other services (user, feed, media, notification)
```

**Docker Compose Build (Recommended)**
```bash
# Return to project root
cd /path/to/facebook-clone

# Docker will automatically build all services
docker-compose build
```

---

## Part 3: Running with Docker

### Understanding Docker Compose
Docker Compose orchestrates multiple containers (microservices) at once. Our `docker-compose.yml` file defines:
- **5 Backend Services**: auth, user, feed, media, notification
- **Infrastructure**: PostgreSQL, Redis, Kafka, Zookeeper, MinIO

### Step 1: Start All Services

```bash
# Start all services in detached mode (runs in background)
docker-compose up -d

# This will:
# 1. Download all required Docker images
# 2. Build your microservices
# 3. Create databases
# 4. Set up networking
# 
# ⏱️ First run may take 10-15 minutes
```

### Step 2: Verify Services Are Running

```bash
# Check running containers
docker-compose ps

# You should see all services with status "Up"
```

Expected output:
```
NAME                              STATUS
facebook-auth-service             Up
facebook-feed-service             Up
facebook-user-service             Up
facebook-media-service            Up
facebook-notification-service     Up
facebook-postgres                 Up
facebook-redis                    Up
facebook-kafka                    Up
facebook-zookeeper                Up
facebook-minio                    Up
```

### Step 3: Check Service Health

```bash
# View logs for all services
docker-compose logs -f

# View logs for a specific service
docker-compose logs -f auth-service

# Test service endpoints
curl http://localhost:8080/actuator/health  # Auth Service
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Feed Service
curl http://localhost:8084/actuator/health  # Notification Service
curl http://localhost:8085/actuator/health  # Media Service
```

### Step 4: Start Frontend Development Server

```bash
# In a NEW terminal window
cd frontend
npm run dev

# Frontend will be available at: http://localhost:5173
```

### Step 5: Access the Application

Open your browser and navigate to:
- **Frontend**: http://localhost:5173
- **MinIO Console** (Object Storage): http://localhost:9001
  - Username: `admin`
  - Password: `password`

---

## Part 4: Development Workflow

### Daily Development Flow

```bash
# 1. Start your day - pull latest changes
git pull origin main

# 2. Start backend services
docker-compose up -d

# 3. Start frontend (in a separate terminal)
cd frontend
npm run dev

# 4. Make your changes...

# 5. End of day - commit your changes
git add .
git commit -m "Describe what you changed"
git push origin main
```

### Making Code Changes

#### Frontend Changes (Vue.js)
```bash
# Frontend has hot-reload - changes appear immediately
cd frontend
# Edit files in src/
# Browser will auto-refresh
```

#### Backend Changes (Spring Boot)
```bash
# After changing Java code, rebuild the service

# Option 1: Rebuild specific service
docker-compose up -d --build auth-service

# Option 2: Rebuild all services
docker-compose up -d --build
```

### Database Management

```bash
# Connect to PostgreSQL
docker exec -it facebook-postgres psql -U admin

# List databases
\l

# Connect to a specific database
\c auth_db

# List tables
\dt

# Exit
\q
```

### Viewing Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f auth-service

# Last 100 lines
docker-compose logs --tail=100 feed-service
```

### Stopping Services

```bash
# Stop all services (keeps data)
docker-compose stop

# Stop and remove containers (keeps data volumes)
docker-compose down

# Stop and remove EVERYTHING including data
docker-compose down -v
```

---

## Part 5: Troubleshooting

### Common Issues and Solutions

#### Issue 1: Port Already in Use
```
Error: Bind for 0.0.0.0:8080 failed: port is already allocated
```

**Solution:**
```bash
# Find what's using the port
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change the port in docker-compose.yml
```

#### Issue 2: Container Keeps Restarting
```bash
# Check logs to see why
docker-compose logs auth-service

# Common causes:
# - Database not ready: Wait 30 seconds and check again
# - Wrong configuration: Check environment variables in docker-compose.yml
# - Build errors: Try rebuilding
docker-compose up -d --build --force-recreate auth-service
```

#### Issue 3: Frontend Can't Connect to Backend
```
Network Error: Failed to fetch
```

**Solution:**
```bash
# 1. Verify backend is running
docker-compose ps

# 2. Check backend health
curl http://localhost:8080/actuator/health

# 3. Check frontend proxy configuration
cat frontend/vite.config.js
```

#### Issue 4: Database Connection Failed
```
Connection refused: postgres:5432
```

**Solution:**
```bash
# 1. Ensure PostgreSQL is running
docker-compose ps postgres

# 2. Check database logs
docker-compose logs postgres

# 3. Verify database initialization
docker exec -it facebook-postgres psql -U admin -l
```

#### Issue 5: Maven Build Fails
```
Could not resolve dependencies
```

**Solution:**
```bash
# 1. Clear Maven cache
rm -rf ~/.m2/repository

# 2. Retry build
docker-compose build --no-cache auth-service
```

#### Issue 6: npm install Fails
```
ERESOLVE unable to resolve dependency tree
```

**Solution:**
```bash
# 1. Delete node_modules and package-lock.json
cd frontend
rm -rf node_modules package-lock.json

# 2. Reinstall
npm install --legacy-peer-deps
```

### Getting Help

1. **Check existing documentation**
   - README.md
   - ARCHITECTURE.md
   - PROJECT_SPEC.md

2. **View detailed logs**
   ```bash
   docker-compose logs -f <service-name>
   ```

3. **Restart from scratch**
   ```bash
   docker-compose down -v
   docker-compose build --no-cache
   docker-compose up -d
   ```

---

## 🎉 Next Steps

Once everything is running:
1. ✅ Read [README.md](README.md) for feature overview
2. ✅ Review [ARCHITECTURE.md](ARCHITECTURE.md) for system design
3. ✅ Check [PROJECT_SPEC.md](PROJECT_SPEC.md) for technical specifications
4. ✅ Explore the code and make your first change!

---

## 📚 Learning Resources

- **Git & GitHub**: https://docs.github.com/en/get-started
- **Docker**: https://docs.docker.com/get-started/
- **Spring Boot**: https://spring.io/guides
- **Vue.js**: https://vuejs.org/guide/
- **Kubernetes**: https://kubernetes.io/docs/tutorials/

---

**Happy Coding! 🚀**
