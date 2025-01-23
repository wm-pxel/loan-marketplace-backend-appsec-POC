# secret to store the db credentials
resource "aws_secretsmanager_secret" "loan_marketplace_db_credentials" {
  name = "db-credentials-${var.environment}"
}

# initial value
resource "aws_secretsmanager_secret_version" "loan_marketplace_db_credentials_val" {
  secret_id = aws_secretsmanager_secret.loan_marketplace_db_credentials.id
  secret_string = jsonencode(
    {
      username       = aws_db_instance.loan_marketplace_db.username,
      password       = aws_db_instance.loan_marketplace_db.password,
      datasource_url = "jdbc:postgresql://${aws_db_instance.loan_marketplace_db.address}:5432/marketplace"
    }
  )
  lifecycle {
    ignore_changes = [secret_string]
  }
}

# secret to store env variables
resource "aws_secretsmanager_secret" "loan_marketplace_env_vars" {
  name = "lm-backend-${var.environment}-env"
}

# initial value
resource "aws_secretsmanager_secret_version" "loan_marketplace_env_vars_val" {
  secret_id = aws_secretsmanager_secret.loan_marketplace_env_vars.id
  secret_string = jsonencode(
    {
      SPRING_PROFILES_ACTIVE           = "default",
      SECRET                           = "default",
      S3_FILE_UPLOAD_BUCKET            = "${aws_s3_bucket.loan_marketplace_file_uploads.id}",
      S3_FILE_UPLOAD_REGION            = "${var.region}",
      S3_FILE_UPLOAD_ACCESS_KEY_ID     = "default",
      S3_FILE_UPLOAD_SECRET_ACCESS_KEY = "default"
      AWS_ACCESS_KEY_ID                = "default",
      AWS_SECRET_ACCESS_KEY            = "default"
    }
  )
  depends_on = [
    aws_s3_bucket.loan_marketplace_file_uploads,
    aws_lb.loan_marketplace_lb
  ]
  lifecycle {
    ignore_changes = [secret_string]
  }
}

# secret to store load balancer dns name
resource "aws_secretsmanager_secret" "loan_marketplace_lb" {
  name = "lm-backend-lb-${var.environment}"
}

# initial value
resource "aws_secretsmanager_secret_version" "loan_marketplace_lb_val" {
  secret_id = aws_secretsmanager_secret.loan_marketplace_lb.id
  secret_string = jsonencode(
    {
      DNS_NAME = "${aws_lb.loan_marketplace_lb.dns_name}",
    }
  )
}

# secret to store image tag
resource "aws_secretsmanager_secret" "loan_marketplace_image_tag" {
  name = "lm-backend-image-tag-${var.environment}"
}

# secret to store cognito variables
resource "aws_secretsmanager_secret" "loan_marketplace_cognito_vars" {
  name = "lm-backend-${var.environment}-cognito-vars"
}

# initial cognito values
resource "aws_secretsmanager_secret_version" "loan_marketplace_cognito_vars_val" {
  secret_id = aws_secretsmanager_secret.loan_marketplace_cognito_vars.id
  secret_string = jsonencode(
    {
      ISSUER_URI  = "${var.issuer_uri}",
      JWK_SET_URI = "${var.jwk_set_uri}"
    }
  )
  lifecycle {
    ignore_changes = [secret_string]
  }
}

# Define the Cognito credentials secret
resource "aws_secretsmanager_secret" "lamina_service_cognito_credentials" {
  name = "lamina-service-cognito-credentials-${var.environment}"
}

# Define the secret version for Cognito credentials
resource "aws_secretsmanager_secret_version" "lamina_service_cognito_credentials_version" {
  secret_id     = aws_secretsmanager_secret.lamina_service_cognito_credentials.id
  secret_string = jsonencode({
    client_id = "placeholder",  # Placeholder value, actual value managed in AWS
    client_secret = "placeholder",  # Placeholder value, actual value managed in AWS
    issuer = "placeholder"  # Placeholder value, actual value managed in AWS
  })
  lifecycle {
    ignore_changes = [secret_string]
  }
}

# Define the Salesforce secret
resource "aws_secretsmanager_secret" "lamina_service_sf_secret" {
  name = "lamina-service-sf-secret-${var.environment}"
}

# Define the secret version for Salesforce secret
resource "aws_secretsmanager_secret_version" "lamina_service_sf_secret_version" {
  secret_id     = aws_secretsmanager_secret.lamina_service_sf_secret.id
  lifecycle {
    ignore_changes = [secret_string]
  }
}