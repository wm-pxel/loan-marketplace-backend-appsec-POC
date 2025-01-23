resource "aws_lb" "loan_marketplace_lb" {
  name               = "lm-backend-${var.environment}-lb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.loan_marketplace_sg.id]
  subnets =  var.environment == "prod" ? [aws_subnet.public_subnet_c.id, aws_subnet.public_subnet_d.id] : [var.subnet_us_east_1a, var.subnet_us_east_1b, var.subnet_us_east_1e, var.subnet_us_east_1f]
}

resource "aws_lb_target_group" "loan_marketplace_tg" {
  name        = "lm-backend-${var.environment}-tg"
  port        = 10800
  protocol    = "HTTP"
  vpc_id      = data.aws_vpc.selected.id
  target_type = "ip"
  health_check {
    protocol            = "HTTP"
    path                = "/actuator/info"
    healthy_threshold   = 2     # Number of successful checks before considered healthy.
    unhealthy_threshold = 5     # Number of failed checks before considered unhealthy.
    timeout             = 5     # Seconds to wait during a health check before failing.
    interval            = 60    # Seconds to wait between health checks.
    matcher             = "200" # The HTTP response codes to indicate health.
  }
}

resource "aws_lb_listener" "loan_marketplace_listener" {
  load_balancer_arn = aws_lb.loan_marketplace_lb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.loan_marketplace_tg.arn
  }
}