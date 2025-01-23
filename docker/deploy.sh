#! /usr/bin/env bash

aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 710894263335.dkr.ecr.us-east-1.amazonaws.com

IMAGE_TAG=$(suid -l 14 -s)
echo $IMAGE_TAG
docker build --platform linux/amd64 -f docker/deployment/Dockerfile -t 710894263335.dkr.ecr.us-east-1.amazonaws.com/loan-marketplace-$BRANCH_NAME:$IMAGE_TAG .
docker build --platform linux/amd64 -f docker/deployment/Dockerfile -t 710894263335.dkr.ecr.us-east-1.amazonaws.com/loan-marketplace-$BRANCH_NAME:latest .
docker push 710894263335.dkr.ecr.us-east-1.amazonaws.com/loan-marketplace-$BRANCH_NAME:$IMAGE_TAG
docker push 710894263335.dkr.ecr.us-east-1.amazonaws.com/loan-marketplace-$BRANCH_NAME:latest

aws secretsmanager update-secret --secret-id "loan-marketplace-$BRANCH_NAME-image-tag" --secret-string "{\"tag\": \"$IMAGE_TAG\"}" --output json

cd devops/$BRANCH_NAME/aws/ecs/us-east-1

terraform init
terraform apply -auto-approve
