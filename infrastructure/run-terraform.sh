#!/bin/bash
# Wrapper to run Terraform via Docker
# Usage: ./run-terraform.sh <command>
# Example: ./run-terraform.sh plan

if [ -z "$AWS_ACCESS_KEY_ID" ] || [ -z "$AWS_SECRET_ACCESS_KEY" ]; then
  echo "Error: AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY must be set."
  echo "Usage:"
  echo "  export AWS_ACCESS_KEY_ID=..."
  echo "  export AWS_SECRET_ACCESS_KEY=..."
  echo "  ./run-terraform.sh init"
  echo "  ./run-terraform.sh apply"
  exit 1
fi

docker run --rm -it \
  -v $(pwd):/workspace \
  -w /workspace \
  -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID \
  -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY \
  -e AWS_DEFAULT_REGION=us-east-1 \
  hashicorp/terraform:latest "$@"
