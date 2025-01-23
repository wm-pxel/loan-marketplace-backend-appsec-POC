
resource "aws_acm_certificate" "auth_cert" {
  count             = var.use_custom_auth_domain ? 1 : 0
  domain_name       = "*.${local.fqdn}"
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_acm_certificate_validation" "auth_cert_validation" {
  count = var.use_custom_auth_domain ? 1 : 0
  certificate_arn = aws_acm_certificate.auth_cert[0].arn
}