# IAM role for ECS tasks
resource "aws_iam_role" "loan_marketplace_ecs_execution_role" {
  name = "lm_backend_ecs_execution_role_${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_policy" "ecs_secrets_policy" {
  name        = "ecs_secrets_policy_${var.environment}"
  description = "Allow ECS to read secrets from AWS Secrets Manager"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action   = "secretsmanager:GetSecretValue",
        Resource = "arn:aws:secretsmanager:us-east-1:710894263335:secret:*",
        Effect   = "Allow",
      },
    ],
  })
}

resource "aws_iam_role" "client_file_downloads_role" {
  name = "client_file_download_service-${var.environment}"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Sid    = "",
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role" "pre_sign_up" {
  name = "pre_sign_up-${var.environment}"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Sid    = "",
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

# Attach the AmazonS3FullAccess policy
resource "aws_iam_role_policy_attachment" "client_file_downloads_service_s3_policy" {
  role       = aws_iam_role.client_file_downloads_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

# Attach the AWSLambdaBasicExecutionRole policy
resource "aws_iam_role_policy_attachment" "client_file_downloads_service_lambda_policy" {
  role       = aws_iam_role.client_file_downloads_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Attach the SecretsManagerReadWrite policy
resource "aws_iam_role_policy_attachment" "client_file_downloads_service_secrets_policy" {
  role       = aws_iam_role.client_file_downloads_role.name
  policy_arn = "arn:aws:iam::aws:policy/SecretsManagerReadWrite"
}

resource "aws_iam_role_policy_attachment" "loan_marketplace_ecs_execution_role_attachment" {
  role       = aws_iam_role.loan_marketplace_ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy_attachment" "loan_marketplace_ecs_secrets_policy_attachment" {
  role       = aws_iam_role.loan_marketplace_ecs_execution_role.name
  policy_arn = aws_iam_policy.ecs_secrets_policy.arn
}

resource "aws_iam_role_policy_attachment" "pre_sign_up" {
  role       = aws_iam_role.pre_sign_up.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

