#!/bin/bash
set -e

export PATH=$PATH:/opt/homebrew/bin
REGION="us-east-1"
ACCOUNT_ID="991370224656"
ECR_URL="$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"

echo "Logging into ECR..."
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_URL

build_and_push() {
    SERVICE=$1
    REPO_NAME="facebook-$SERVICE"
    IMAGE_URI="$ECR_URL/$REPO_NAME:latest"

    echo "========================================"
    echo "Processing $SERVICE..."
    echo "========================================"

    # Assuming running from project root
    cd backend/$SERVICE-service
    
    echo "Building Docker Image (with Maven)..."
    docker build --platform linux/amd64 -t $REPO_NAME .
    docker tag $REPO_NAME:latest $IMAGE_URI

    echo "Pushing to ECR..."
    docker push $IMAGE_URI
    
    cd ../..
}

build_frontend() {
    echo "========================================"
    echo "Processing frontend..."
    echo "========================================"
    
    REPO_NAME="facebook-frontend"
    IMAGE_URI="$ECR_URL/$REPO_NAME:latest"

    cd frontend
    docker build --no-cache --platform linux/amd64 -t $REPO_NAME .
    docker tag $REPO_NAME:latest $IMAGE_URI
    
    echo "Pushing configuration..."
    docker push $IMAGE_URI
    cd ..
}

build_and_push "auth"
build_and_push "user"
build_and_push "feed"
build_frontend

echo "✅ All images deployed successfully!"
