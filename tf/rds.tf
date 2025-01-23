resource "random_password" "password" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}


resource "aws_db_subnet_group" "db_sg" {
  description = "Subnet group for DB"
  name        = "${var.environment}-db-vpc"
  subnet_ids  = var.environment == "prod" ? [aws_subnet.private_subnet_c.id, aws_subnet.private_subnet_d.id] : [var.subnet_us_east_1a, var.subnet_us_east_1b, var.subnet_us_east_1e, var.subnet_us_east_1f]
}


resource "aws_db_instance" "loan_marketplace_db" {
  allocated_storage                     = var.loan_marketplace_rds_parameters[var.environment]["allocated_storage"]
  auto_minor_version_upgrade            = var.loan_marketplace_rds_parameters[var.environment]["auto_minor_version_upgrade"]
  backup_target                         = var.loan_marketplace_rds_parameters[var.environment]["backup_target"]
  ca_cert_identifier                    = "rds-ca-rsa2048-g1"
  copy_tags_to_snapshot                 = var.loan_marketplace_rds_parameters[var.environment]["copy_tags_to_snapshot"]
  customer_owned_ip_enabled             = var.loan_marketplace_rds_parameters[var.environment]["customer_owned_ip_enabled"]
  db_subnet_group_name                  = aws_db_subnet_group.db_sg.name
  deletion_protection                   = var.loan_marketplace_rds_parameters[var.environment]["deletion_protection"]
  engine                                = var.loan_marketplace_rds_parameters[var.environment]["engine"]
  engine_version                        = var.loan_marketplace_rds_parameters[var.environment]["engine_version"]
  iam_database_authentication_enabled   = var.loan_marketplace_rds_parameters[var.environment]["iam_database_authentication_enabled"]
  identifier                            = "lm-backend-${var.environment}"
  instance_class                        = var.loan_marketplace_rds_parameters[var.environment]["instance_class"]
  kms_key_id                            = "arn:aws:kms:us-east-1:710894263335:key/a53cadb3-0c13-478c-98a5-7129f96e73ec"
  license_model                         = var.loan_marketplace_rds_parameters[var.environment]["license_model"]
  maintenance_window                    = var.loan_marketplace_rds_parameters[var.environment]["maintenance_window"]
  max_allocated_storage                 = var.loan_marketplace_rds_parameters[var.environment]["max_allocated_storage"]
  monitoring_interval                   = var.loan_marketplace_rds_parameters[var.environment]["monitoring_interval"]
  monitoring_role_arn                   = var.loan_marketplace_rds_parameters[var.environment]["monitoring_role_arn"]
  multi_az                              = var.loan_marketplace_rds_parameters[var.environment]["multi_az"]
  network_type                          = var.loan_marketplace_rds_parameters[var.environment]["network_type"]
  option_group_name                     = var.loan_marketplace_rds_parameters[var.environment]["option_group_name"]
  parameter_group_name                  = var.loan_marketplace_rds_parameters[var.environment]["parameter_group_name"]
  performance_insights_enabled          = var.loan_marketplace_rds_parameters[var.environment]["performance_insights_enabled"]
  performance_insights_kms_key_id       = "arn:aws:kms:us-east-1:710894263335:key/a53cadb3-0c13-478c-98a5-7129f96e73ec"
  performance_insights_retention_period = var.loan_marketplace_rds_parameters[var.environment]["performance_insights_retention_period"]
  port                                  = var.loan_marketplace_rds_parameters[var.environment]["port"]
  publicly_accessible                   = var.loan_marketplace_rds_parameters[var.environment]["publicly_accessible"]
  storage_encrypted                     = var.loan_marketplace_rds_parameters[var.environment]["storage_encrypted"]
  storage_type                          = var.loan_marketplace_rds_parameters[var.environment]["storage_type"]
  username                              = var.loan_marketplace_rds_parameters[var.environment]["username"]
  password                              = random_password.password.result
  vpc_security_group_ids                = [aws_security_group.loan_marketplace_pg_sg.id]
  skip_final_snapshot                   = true
}

resource "null_resource" "db_setup" {

  provisioner "local-exec" {

    command = "psql -h \"${aws_db_instance.loan_marketplace_db.address}\" -p 5432 -U postgres -f setup_db.sql"

    environment = {
      PGPASSWORD = aws_db_instance.loan_marketplace_db.password
    }
  }
  depends_on = [aws_db_instance.loan_marketplace_db]
}

data "aws_iam_policy_document" "backup_role_policy" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["backup.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "backup_default_service_role" {
  name               = "AWSBackupDefaultServiceRole-${var.environment}"
  assume_role_policy = data.aws_iam_policy_document.backup_role_policy.json
}

resource "aws_iam_role_policy_attachment" "backup_service_role_for_backup_attachment" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSBackupServiceRolePolicyForBackup"
  role       = aws_iam_role.backup_default_service_role.name
}

resource "aws_backup_vault" "rds_backup_vault" {
  name        = "rds-backup-vault-${var.environment}"
  kms_key_arn = "arn:aws:kms:us-east-1:710894263335:key/50d3c87f-4d55-4903-93c8-4317e6179a6b"
}

resource "aws_backup_plan" "rds_instance_plan" {
  name = "rds-instance-daily-backup-${var.environment}"

  rule {
    rule_name                = "rds-instance-daily-backup-rule"
    target_vault_name        = aws_backup_vault.rds_backup_vault.name
    schedule                 = "cron(0 12 * * ? *)"
    enable_continuous_backup = true

    lifecycle {
      delete_after = 10
    }
  }
}

resource "aws_backup_selection" "rds_instance_selection" {
  iam_role_arn = aws_iam_role.backup_default_service_role.arn
  name         = "rds-instance-daily-backup-selection-${var.environment}"
  plan_id      = aws_backup_plan.rds_instance_plan.id

  resources = [
    aws_db_instance.loan_marketplace_db.arn
  ]
}
