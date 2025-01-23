
resource "aws_cognito_user_pool" "user_pool" {
  name = "lm-user-pool-${var.environment}"

  username_attributes = [
    "email",
  ]

  email_configuration {
    email_sending_account = "DEVELOPER"
    from_email_address = "Lamina Verification <noreply@app.laminafs.com>"
    source_arn = data.aws_ses_email_identity.noreply_identity.arn
    configuration_set = data.aws_sesv2_configuration_set.config-set.configuration_set_name
  }

  account_recovery_setting {
    recovery_mechanism {
      name     = "verified_email"
      priority = 1
    }

    recovery_mechanism {
      name     = "verified_phone_number"
      priority = 2
    }
  }

  mfa_configuration = var.use_mfa ? "ON" : "OFF"

  dynamic "email_mfa_configuration" {
    for_each = var.use_mfa ? toset([1]) : toset([])
    content {
      subject = "Lamina Email Verification"
      message = "Your Lamina verification code is {####}"
    }
  }

  password_policy {
    minimum_length = var.user_pool_password_policy.minimum_length
    require_lowercase = var.user_pool_password_policy.require_lowercase
    require_numbers = var.user_pool_password_policy.require_lowercase
    require_symbols = var.user_pool_password_policy.require_symbols
    require_uppercase = var.user_pool_password_policy.require_uppercase
    temporary_password_validity_days = var.user_pool_password_policy.temporary_password_validity_days
  }

  schema {
    attribute_data_type       = "String"
    developer_only_attribute  = false
    mutable                   = true
    required                  = false
    name                      = "marketplaceId"
    string_attribute_constraints {
      max_length = "36"
      min_length = "36"
    }
  }

  schema {
    attribute_data_type       = "Number"
    developer_only_attribute  = false
    mutable                   = true
    required                  = false
    name                      = "test-account"

    number_attribute_constraints {
      max_value = 1
      min_value = 0
    }
  }

  user_pool_add_ons {
    advanced_security_mode = "AUDIT"
  }

  lambda_config {
    pre_token_generation_config {
      lambda_arn = data.aws_lambda_function.add_email_to_access_token_lambda.arn
      lambda_version = "V2_0"
    }
  }

}

resource "aws_cognito_user_pool_domain" "user_pool_domain" {
    domain          = var.use_custom_auth_domain ? "auth.${local.fqdn}" : "lm-${var.sub_domain}"
    user_pool_id    = aws_cognito_user_pool.user_pool.id
    certificate_arn = var.use_custom_auth_domain ? aws_acm_certificate.auth_cert[0].arn : null
    depends_on      = [aws_acm_certificate_validation.auth_cert_validation[0]]

}


resource "aws_cognito_user_pool_client" "web_client" {
  name         = "lm-web-client"
  user_pool_id = aws_cognito_user_pool.user_pool.id
  generate_secret = false
  allowed_oauth_flows = ["code"]
  allowed_oauth_scopes = ["openid"]
  allowed_oauth_flows_user_pool_client = true
  callback_urls = local.web_client_redirect
  logout_urls = local.web_client_redirect

  access_token_validity = 24
  id_token_validity = 24
  refresh_token_validity = 30

  token_validity_units {
    access_token = "hours"
    id_token = "hours"
    refresh_token = "days"
  }
}

resource "aws_cognito_user_pool_client" "service_client" {
  name         = "lm-service-client"
  user_pool_id = aws_cognito_user_pool.user_pool.id

  generate_secret = true
  allowed_oauth_flows = ["client_credentials"]
  allowed_oauth_scopes = [
    for scope in aws_cognito_resource_server.resource.scope_identifiers : scope
  ]
  allowed_oauth_flows_user_pool_client = true

  access_token_validity = 24
  id_token_validity = 24
  refresh_token_validity = 30

  token_validity_units {
    access_token = "hours"
    id_token = "hours"
    refresh_token = "days"
  }
}

resource "aws_cognito_resource_server" "resource" {
  name = "lamina"
  identifier   = "lamina"
  user_pool_id = aws_cognito_user_pool.user_pool.id

  scope {
    scope_description = "Access files lists"
    scope_name        = "lambda-service-account"
  }
}

resource "aws_cognito_resource_server" "user_mgmt_api" {
  name = "User Management API"
  identifier = "UserManagementAPI"
  user_pool_id = aws_cognito_user_pool.user_pool.id

  lifecycle {
    ignore_changes = [scope]
  }
}