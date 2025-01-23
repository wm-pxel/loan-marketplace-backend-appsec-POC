data "aws_secretsmanager_secret" "db_credentials" {
  name = "db-credentials-${var.environment}"
  depends_on = [
    aws_secretsmanager_secret.loan_marketplace_db_credentials
  ]
}

data "aws_secretsmanager_secret_version" "db_credentials_version" {
  secret_id = data.aws_secretsmanager_secret.db_credentials.id
  depends_on = [
    aws_secretsmanager_secret_version.loan_marketplace_db_credentials_val
  ]
}

data "aws_secretsmanager_secret" "environment_variables" {
  name = "lm-backend-${var.environment}-env"
  depends_on = [
    aws_secretsmanager_secret.loan_marketplace_env_vars
  ]
}

data "aws_secretsmanager_secret_version" "environment_variables_version" {
  secret_id = data.aws_secretsmanager_secret.environment_variables.id
  depends_on = [
    aws_secretsmanager_secret_version.loan_marketplace_env_vars_val
  ]
}

data "aws_secretsmanager_secret" "image_tag" {
  name = "lm-backend-image-tag-${var.environment}"
}

data "aws_secretsmanager_secret_version" "image_tag_version" {
  secret_id = data.aws_secretsmanager_secret.image_tag.id
}

data "aws_vpc" "selected" {
  id = var.vpc
}

data "aws_internet_gateway" "gateway" {
  internet_gateway_id = var.igw
}

data "aws_secretsmanager_secret" "cognito_variables" {
  name = "lm-backend-${var.environment}-cognito-vars"
  depends_on = [
    aws_secretsmanager_secret.loan_marketplace_cognito_vars
  ]
}

data "aws_secretsmanager_secret_version" "cognito_variables_version" {
  secret_id = data.aws_secretsmanager_secret.cognito_variables.id
  depends_on = [
    aws_secretsmanager_secret_version.loan_marketplace_cognito_vars_val
  ]
}

data "aws_lambda_function" "add_email_to_access_token_lambda" {
  function_name = "lamina-add-email-to-access-token"
}

data "aws_route53_zone" "domain_zone" {
  name = var.domain
}

data "aws_secretsmanager_secret" "jira_lambda_token" {
  name = "jira-lambda-integration-token"
}

data "aws_security_group" "bastion_sg" {
  id = "sg-080d471a822e7301e"
}

data "aws_route53_zone" "hosted_zone" {
  name = var.domain
}

data "aws_ses_email_identity" "noreply_identity" {
  email = "noreply@app.laminafs.com"
}

data "aws_sesv2_configuration_set" "config-set" {
  configuration_set_name = "ses-config-set"
}
