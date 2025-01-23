resource "aws_cloudwatch_log_metric_filter" "cloudwatch_filter" {
  name           = "${var.environment}-Error-Log-Alert"
  log_group_name = aws_cloudwatch_log_group.loan_marketplace_logs.name
  pattern        = "[w1, w2=ERROR, w3=%DuplicateKeyException%]"

  metric_transformation {
    name      = "${var.environment}-Error-Count"
    namespace = "${var.environment}-Monitoring"
    value     = "1"
  }
}

resource "aws_cloudwatch_metric_alarm" "cloudwatch_error_logs_alarm" {
  alarm_name          = "${var.environment}-Error-Log-Alarm"
  alarm_description   = "Alarm triggered by Cloudwatch Error Log..."
  metric_name         = lookup(aws_cloudwatch_log_metric_filter.cloudwatch_filter.metric_transformation[0], "name")
  threshold           = "0"
  statistic           = "Sum"
  comparison_operator = "GreaterThanThreshold"
  datapoints_to_alarm = "1"
  evaluation_periods  = "1"
  period              = "300"
  alarm_actions       = var.environment == "prod" ? [aws_lambda_function.create_jira_ticket_on_alarm_function[0].arn] : []
  namespace           = lookup(aws_cloudwatch_log_metric_filter.cloudwatch_filter.metric_transformation[0], "namespace")
}

data "archive_file" "create_jira_ticket_on_alarm_file" {
  count       = var.environment == "prod" ? 1 : 0
  source_file = "${path.module}/lambda-functions/createJiraTicketOnLogAlarm.mjs"
  output_path = "${path.module}/lambda-functions/createJiraTicketOnLogAlarm.zip"
  type        = "zip"
}

resource "aws_lambda_function" "create_jira_ticket_on_alarm_function" {
  // only create Jira tickets for alarms that are triggered in the production environment
  count            = var.environment == "prod" ? 1 : 0
  function_name    = "create_jira_ticket_on_alarm"
  runtime          = "nodejs20.x"
  handler          = "createJiraTicketOnLogAlarm.handler"
  filename         = data.archive_file.create_jira_ticket_on_alarm_file[0].output_path
  source_code_hash = data.archive_file.create_jira_ticket_on_alarm_file[0].output_base64sha256
  role             = aws_iam_role.function_role[0].arn

  environment {
    variables = {
      JIRA_TOKEN_SECRET_NAME = data.aws_secretsmanager_secret.jira_lambda_token.name
    }
  }
}


resource "aws_iam_role" "function_role" {
  count = var.environment == "prod" ? 1 : 0
  name  = "create_jira_ticket_on_alarm"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      },
    ]
  })

  inline_policy {
    name = "get_secret_permission"
    policy = jsonencode({
      Version = "2012-10-17"
      Statement = [
        {
          Action = [
            "secretsmanager:GetSecretValue",
          ]
          Effect   = "Allow"
          Resource = "${data.aws_secretsmanager_secret.jira_lambda_token.arn}"
        },
        {
          Action = [
            "logs:CreateLogStream",
            "logs:CreateLogGroup",
            "logs:PutLogEvents"
          ]
          Effect   = "Allow",
          Resource = "arn:aws:logs:*:*:*"
        }
      ]
    })
  }
}

resource "aws_lambda_permission" "allow_cloudwatch_to_call_lambda" {
  count         = var.environment == "prod" ? 1 : 0
  statement_id  = "AllowExecutionFromCloudWatch"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.create_jira_ticket_on_alarm_function[0].function_name
  principal     = "lambda.alarms.cloudwatch.amazonaws.com"
  source_arn    = aws_cloudwatch_metric_alarm.cloudwatch_error_logs_alarm.arn
}

resource "aws_cloudwatch_event_rule" "inspector_findings" {
  count       = var.environment == "prod" ? 1 : 0
  name        = "detect-inspector-finding"
  description = "Captures critical findings from Amazon Inspector to call a lambda in prod"

  event_pattern = jsonencode({
    detail-type = [
      "Inspector2 Finding"
    ]
    source    = ["aws.inspector2"]
    resources = [aws_ecr_repository.loan_marketplace.arn]
    detail = {
      "severity" : ["CRITICAL"],
      "status" : ["ACTIVE"]
    }
  })
}

resource "aws_cloudwatch_event_target" "send_to_lambda" {
  count = var.environment == "prod" ? 1 : 0
  arn   = aws_lambda_function.create_jira_ticket_on_inspector_finding[0].arn
  rule  = aws_cloudwatch_event_rule.inspector_findings[0].id
}

resource "aws_lambda_permission" "allow_event_bridge_to_call_lambda" {
  count         = var.environment == "prod" ? 1 : 0
  statement_id  = "AllowExecutionFromEventBridge"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.create_jira_ticket_on_inspector_finding[0].function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.inspector_findings[0].arn
}

data "archive_file" "create_jira_ticket_on_inspector_finding_file" {
  count       = var.environment == "prod" ? 1 : 0
  source_file = "${path.module}/lambda-functions/createJiraTicketOnInspectorFinding.mjs"
  output_path = "${path.module}/lambda-functions/createJiraTicketOnInspectorFinding.zip"
  type        = "zip"
}

resource "aws_lambda_function" "create_jira_ticket_on_inspector_finding" {
  count            = var.environment == "prod" ? 1 : 0
  function_name    = "create_jira_ticket_on_inspector_finding"
  runtime          = "nodejs20.x"
  handler          = "createJiraTicketOnInspectorFinding.handler"
  filename         = data.archive_file.create_jira_ticket_on_inspector_finding_file[0].output_path
  source_code_hash = data.archive_file.create_jira_ticket_on_inspector_finding_file[0].output_base64sha256
  role             = aws_iam_role.function_role[0].arn

  environment {
    variables = {
      JIRA_TOKEN_SECRET_NAME = data.aws_secretsmanager_secret.jira_lambda_token.name
    }
  }
}

resource "aws_cloudwatch_log_group" "client_file_downloads_service_log_group" {
  name              = "/aws/lambda/clientFileDownloadService-${var.environment}"
  retention_in_days = 14  # Adjust the retention period as needed
}

resource "aws_cloudwatch_log_group" "client_file_downloads_service_log_group" {
  name              = "/aws/lambda/preSignUp-${var.environment}"
  retention_in_days = 14  # Adjust the retention period as needed
}
