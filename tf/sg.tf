resource "aws_security_group" "loan_marketplace_sg" {
  name        = "lm-backend-${var.environment}-sg"
  description = "Allow inbound traffic"
  vpc_id      = data.aws_vpc.selected.id

  egress {
    cidr_blocks = ["0.0.0.0/0"]
    from_port   = "0"
    protocol    = "-1"
    self        = "false"
    to_port     = "0"
  }

  ingress {
    cidr_blocks = ["0.0.0.0/0"]
    from_port   = "10800"
    protocol    = "tcp"
    self        = "false"
    to_port     = "10800"
  }

  ingress {
    cidr_blocks = ["0.0.0.0/0"]
    from_port   = "80"
    protocol    = "tcp"
    self        = "false"
    to_port     = "80"
  }
}

resource "aws_security_group" "loan_marketplace_pg_sg" {
  name        = "lm-pg-${var.environment}-sg"
  description = "Security Group for PG"
  vpc_id      = data.aws_vpc.selected.id

  egress {
    cidr_blocks = ["0.0.0.0/0"]
    from_port   = "0"
    protocol    = "-1"
    self        = "false"
    to_port     = "0"
  }

  ingress {
    security_groups  = [aws_security_group.loan_marketplace_sg.id, data.aws_security_group.bastion_sg.id]
    from_port        = "5432"
    to_port          = "5432"
    protocol         = "tcp"
    self             = "false"
  }

  tags = {
    Name = "lm-pg-${var.environment}"
  }

  tags_all = {
    Name = "lm-pg-${var.environment}"
  }

  lifecycle {
    ignore_changes = [ingress]
  }
}
