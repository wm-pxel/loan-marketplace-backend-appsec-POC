variable "profile" {
  type = string
}

variable "region" {
  type        = string
  description = "Region for the stack"
}

variable "environment" {
  type        = string
  description = "Environment of the infrastructure"
}

variable "domain" {
  type = string
}

variable "sub_domain" {
  type = string
  default = null
}

variable "use_mfa" {
  type = bool
  default = true
}

variable allow_local_login {
  type = bool
  default = false
}


variable "subnet_us_east_1a" {
  default = "subnet-0e2e072de2503c95a"
  type    = string
}

variable "subnet_us_east_1b" {
  default = "subnet-0153336040a1409a6"
  type    = string
}

variable "subnet_us_east_1c" {
  default = "subnet-0f7251277a1fc4150"
  type    = string
}

variable "subnet_us_east_1d" {
  default = "subnet-001d7ea1f36c42746"
  type    = string
}

variable "subnet_us_east_1e" {
  default = "subnet-04be6ce1d4896694e"
  type    = string
}

variable "subnet_us_east_1f" {
  default = "subnet-0ad1de710673d6efd"
  type    = string
}

variable "vpc" {
  default = "vpc-0c52fecd9784ca31b"
  type    = string
}

variable "igw" {
  default = "igw-06a755400b54d05e1"
  type = string
}

variable "cidr_blocks" {
  type = map(any)
  default = {
    "dev"  = ["172.31.110.0/24", "172.31.111.0/24", "172.31.112.0/24", "172.31.113.0/24", "172.31.114.0/24"]
    "test" = ["172.31.120.0/24", "172.31.121.0/24", "172.31.122.0/24", "172.31.123.0/24", "172.31.124.0/24"]
    "qa"   = ["172.31.130.0/24", "172.31.131.0/24", "172.31.132.0/24", "172.31.133.0/24", "172.31.134.0/24"]
    "sandbox" = ["172.31.140.0/24", "172.31.141.0/24", "172.31.142.0/24", "172.31.143.0/24", "172.31.144.0/24"]
    "prod" = ["172.31.180.0/24", "172.31.181.0/24", "172.31.182.0/24", "172.31.183.0/24", "172.31.184.0/24"]
  }
}

variable "use_custom_auth_domain" {
  type = bool
  default = false
}

variable "loan_marketplace_rds_parameters" {
  type = map(any)
  default = {
    test = {
      allocated_storage                     = "200"
      auto_minor_version_upgrade            = "true"
      availability_zone                     = "us-east-1d"
      backup_retention_period               = "7"
      backup_target                         = "region"
      backup_window                         = "06:33-07:03"
      copy_tags_to_snapshot                 = "true"
      customer_owned_ip_enabled             = "false"
      db_name                               = "marketplace"
      deletion_protection                   = "false"
      engine                                = "postgres"
      engine_version                        = "15"
      iam_database_authentication_enabled   = "false"
      instance_class                        = "db.m5d.large"
      license_model                         = "postgresql-license"
      maintenance_window                    = "wed:05:45-wed:06:15"
      max_allocated_storage                 = "1000"
      monitoring_interval                   = "60"
      monitoring_role_arn                   = "arn:aws:iam::710894263335:role/rds-monitoring-role"
      multi_az                              = "true"
      network_type                          = "IPV4"
      option_group_name                     = "default:postgres-15"
      parameter_group_name                  = "default.postgres15"
      performance_insights_enabled          = "true"
      performance_insights_retention_period = "7"
      port                                  = "5432"
      publicly_accessible                   = "true"
      storage_encrypted                     = "true"
      storage_throughput                    = "0"
      storage_type                          = "gp3"
      username                              = "postgres"
    },
    dev = {
      allocated_storage                     = "200"
      auto_minor_version_upgrade            = "true"
      availability_zone                     = "us-east-1d"
      backup_retention_period               = "7"
      backup_target                         = "region"
      backup_window                         = "06:33-07:03"
      copy_tags_to_snapshot                 = "true"
      customer_owned_ip_enabled             = "false"
      db_name                               = "marketplace"
      deletion_protection                   = "false"
      engine                                = "postgres"
      engine_version                        = "15"
      iam_database_authentication_enabled   = "false"
      instance_class                        = "db.m5d.large"
      license_model                         = "postgresql-license"
      maintenance_window                    = "wed:05:45-wed:06:15"
      max_allocated_storage                 = "1000"
      monitoring_interval                   = "60"
      monitoring_role_arn                   = "arn:aws:iam::710894263335:role/rds-monitoring-role"
      multi_az                              = "true"
      network_type                          = "IPV4"
      option_group_name                     = "default:postgres-15"
      parameter_group_name                  = "default.postgres15"
      performance_insights_enabled          = "true"
      performance_insights_retention_period = "7"
      port                                  = "5432"
      publicly_accessible                   = "true"
      storage_encrypted                     = "true"
      storage_throughput                    = "0"
      storage_type                          = "gp3"
      username                              = "postgres"
    },
    qa = {
      allocated_storage                     = "200"
      auto_minor_version_upgrade            = "true"
      availability_zone                     = "us-east-1d"
      backup_retention_period               = "7"
      backup_target                         = "region"
      backup_window                         = "06:33-07:03"
      copy_tags_to_snapshot                 = "true"
      customer_owned_ip_enabled             = "false"
      db_name                               = "marketplace"
      deletion_protection                   = "false"
      engine                                = "postgres"
      engine_version                        = "15"
      iam_database_authentication_enabled   = "false"
      instance_class                        = "db.m5d.large"
      license_model                         = "postgresql-license"
      maintenance_window                    = "wed:05:45-wed:06:15"
      max_allocated_storage                 = "1000"
      monitoring_interval                   = "60"
      monitoring_role_arn                   = "arn:aws:iam::710894263335:role/rds-monitoring-role"
      multi_az                              = "true"
      network_type                          = "IPV4"
      option_group_name                     = "default:postgres-15"
      parameter_group_name                  = "default.postgres15"
      performance_insights_enabled          = "true"
      performance_insights_retention_period = "7"
      port                                  = "5432"
      publicly_accessible                   = "true"
      storage_encrypted                     = "true"
      storage_throughput                    = "0"
      storage_type                          = "gp3"
      username                              = "postgres"
    },
    prod = {
      allocated_storage                     = "200"
      auto_minor_version_upgrade            = "true"
      availability_zone                     = "us-east-1d"
      backup_retention_period               = "7"
      backup_target                         = "region"
      backup_window                         = "06:33-07:03"
      copy_tags_to_snapshot                 = "true"
      customer_owned_ip_enabled             = "false"
      db_name                               = "marketplace"
      deletion_protection                   = "false"
      engine                                = "postgres"
      engine_version                        = "15"
      iam_database_authentication_enabled   = "false"
      instance_class                        = "db.m5d.large"
      license_model                         = "postgresql-license"
      maintenance_window                    = "wed:05:45-wed:06:15"
      max_allocated_storage                 = "1000"
      monitoring_interval                   = "60"
      monitoring_role_arn                   = "arn:aws:iam::710894263335:role/rds-monitoring-role"
      multi_az                              = "true"
      network_type                          = "IPV4"
      option_group_name                     = "default:postgres-15"
      parameter_group_name                  = "default.postgres15"
      performance_insights_enabled          = "true"
      performance_insights_retention_period = "7"
      port                                  = "5432"
      publicly_accessible                   = "true"
      storage_encrypted                     = "true"
      storage_throughput                    = "0"
      storage_type                          = "gp3"
      username                              = "postgres"
    },
    sandbox = {
      allocated_storage                     = "200"
      auto_minor_version_upgrade            = "true"
      availability_zone                     = "us-east-1d"
      backup_retention_period               = "7"
      backup_target                         = "region"
      backup_window                         = "06:33-07:03"
      copy_tags_to_snapshot                 = "true"
      customer_owned_ip_enabled             = "false"
      db_name                               = "marketplace"
      deletion_protection                   = "false"
      engine                                = "postgres"
      engine_version                        = "15"
      iam_database_authentication_enabled   = "false"
      instance_class                        = "db.m5d.large"
      license_model                         = "postgresql-license"
      maintenance_window                    = "wed:05:45-wed:06:15"
      max_allocated_storage                 = "1000"
      monitoring_interval                   = "60"
      monitoring_role_arn                   = "arn:aws:iam::710894263335:role/rds-monitoring-role"
      multi_az                              = "true"
      network_type                          = "IPV4"
      option_group_name                     = "default:postgres-15"
      parameter_group_name                  = "default.postgres15"
      performance_insights_enabled          = "true"
      performance_insights_retention_period = "7"
      port                                  = "5432"
      publicly_accessible                   = "true"
      storage_encrypted                     = "true"
      storage_throughput                    = "0"
      storage_type                          = "gp3"
      username                              = "postgres"
    }
  }
}

variable "user_pool_password_policy" {
  default = {
    minimum_length = 10
    require_lowercase = true
    require_numbers = true
    require_symbols = true
    require_uppercase = true
    temporary_password_validity_days = 3
  }
}

variable "ecr_image_prefix" {
  default = "lm-backend"
  type    = string
}

// TODO fix - do not hardcode, read from the cognito_user_pool resource
variable "issuer_uri" {
  default = "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_2GCDHqzoJ"
  type    = string
}

// TODO fix - do not hardcode, read from the cognito_user_pool resource
variable "jwk_set_uri" {
  default = "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_2GCDHqzoJ/.well-known/jwks.json"
  type    = string
}