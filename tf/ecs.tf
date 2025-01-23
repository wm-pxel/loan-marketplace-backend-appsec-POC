resource "aws_ecs_cluster" "loan_marketplace" {
  name = "lm-backend-${var.environment}"
}

resource "aws_cloudwatch_log_group" "loan_marketplace_logs" {
  name = "/ecs/lm-backend-${var.environment}"
}

resource "aws_ecs_task_definition" "loan_marketplace_task" {
  family                   = "lm-backend-${var.environment}-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"  # adjust as necessary
  memory                   = "1024" # adjust as necessary
  execution_role_arn       = aws_iam_role.loan_marketplace_ecs_execution_role.arn
  container_definitions = templatefile("task-definitions/lm-backend-task-definition.json",
    {
      aws_region                                          = var.region
      environment                                         = var.environment
      aws_log_group                                       = aws_cloudwatch_log_group.loan_marketplace_logs.name
      aws_log_stream_prefix                               = "ecs"
      ecr_repository                                      = "${var.ecr_image_prefix}-${var.environment}"
      docker_image_tag                                    = jsondecode(data.aws_secretsmanager_secret_version.image_tag_version.secret_string)["tag"]
      SPRING_DATASOURCE_USERNAME                          = "${data.aws_secretsmanager_secret_version.db_credentials_version.arn}:username::"
      SPRING_DATASOURCE_PASSWORD                          = "${data.aws_secretsmanager_secret_version.db_credentials_version.arn}:password::"
      SPRING_DATASOURCE_URL                               = "${data.aws_secretsmanager_secret_version.db_credentials_version.arn}:datasource_url::"
      SPRING_PROFILES_ACTIVE                              = "${data.aws_secretsmanager_secret_version.environment_variables_version.arn}:SPRING_PROFILES_ACTIVE::"
      SECRET                                              = "${data.aws_secretsmanager_secret_version.environment_variables_version.arn}:SECRET::"
      S3_FILE_UPLOAD_BUCKET                               = "${data.aws_secretsmanager_secret_version.environment_variables_version.arn}:S3_FILE_UPLOAD_BUCKET::"
      S3_FILE_UPLOAD_REGION                               = "${data.aws_secretsmanager_secret_version.environment_variables_version.arn}:S3_FILE_UPLOAD_REGION::"
      S3_FILE_UPLOAD_ACCESS_KEY_ID                        = "${data.aws_secretsmanager_secret_version.environment_variables_version.arn}:S3_FILE_UPLOAD_ACCESS_KEY_ID::"
      S3_FILE_UPLOAD_SECRET_ACCESS_KEY                    = "${data.aws_secretsmanager_secret_version.environment_variables_version.arn}:S3_FILE_UPLOAD_SECRET_ACCESS_KEY::"
      AWS_ACCESS_KEY_ID                                   = "${data.aws_secretsmanager_secret_version.environment_variables_version.arn}:AWS_ACCESS_KEY_ID::"
      AWS_SECRET_ACCESS_KEY                               = "${data.aws_secretsmanager_secret_version.environment_variables_version.arn}:AWS_SECRET_ACCESS_KEY::"
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUERURI = "${data.aws_secretsmanager_secret_version.cognito_variables_version.arn}:ISSUER_URI::"
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWKSETURI = "${data.aws_secretsmanager_secret_version.cognito_variables_version.arn}:JWK_SET_URI::"
  })
}

resource "aws_ecs_service" "loan_marketplace_service" {
  name                 = "lm-backend-${var.environment}-service"
  cluster              = aws_ecs_cluster.loan_marketplace.id
  task_definition      = aws_ecs_task_definition.loan_marketplace_task.arn
  launch_type          = "FARGATE"
  desired_count        = 1
  force_new_deployment = true
  triggers = {
    redeployment = plantimestamp()
  }

  network_configuration {
    subnets          = var.environment == "prod" ? [aws_subnet.private_subnet_c.id, aws_subnet.private_subnet_d.id] : [var.subnet_us_east_1a, var.subnet_us_east_1b, var.subnet_us_east_1e, var.subnet_us_east_1f]
    security_groups  = [aws_security_group.loan_marketplace_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.loan_marketplace_tg.arn
    container_name   = "lm-backend-${var.environment}"
    container_port   = 10800
  }

  deployment_controller {
    type = "ECS"
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }
}

resource "aws_appautoscaling_target" "ecs_target" {
  max_capacity       = 5
  min_capacity       = 1
  resource_id        = "service/${aws_ecs_cluster.loan_marketplace.name}/${aws_ecs_service.loan_marketplace_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "ecs_cpu_scaling_policy" {
  name               = "scale-policy-cpu"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }

    target_value       = 75
    scale_in_cooldown  = 300
    scale_out_cooldown = 300
  }
}

resource "aws_appautoscaling_policy" "ecs_memory_scaling_policy" {
  name               = "scale-policy-memory"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageMemoryUtilization"
    }

    target_value       = 75
    scale_in_cooldown  = 300
    scale_out_cooldown = 300
  }
}
