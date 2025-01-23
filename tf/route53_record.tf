resource "aws_route53_record" "api_domain_record" {
  name    = "api.${local.fqdn}"
  records = [aws_lb.loan_marketplace_lb.dns_name]
  ttl     = "300"
  type    = "CNAME"
  zone_id = data.aws_route53_zone.hosted_zone.id
}

resource "aws_route53_record" "user_pool_dns_a_record" {
  count   = var.use_custom_auth_domain ? 1 : 0
  name    = "auth.${local.fqdn}"
  type    = "A"
  zone_id = data.aws_route53_zone.domain_zone.zone_id

  alias {
    evaluate_target_health = false
    name                   = aws_cognito_user_pool_domain.user_pool_domain.cloudfront_distribution_arn
    # Zone used by AWS to host the HostedUI distributions, does not change.
    zone_id                = "Z2FDTNDATAQYW2"
  }
}

resource "aws_route53_record" "acm_validation_record" {
  for_each = var.use_custom_auth_domain ? {
    for dvo in aws_acm_certificate.auth_cert[0].domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  } : {}

  allow_overwrite = true
  name            = each.value.name
  ttl             = "300"
  records         = [each.value.record]
  type            = each.value.type
  zone_id         = data.aws_route53_zone.domain_zone.zone_id
}
