
locals {
  # Use a subdomain only for non-prod envs
  fqdn = var.sub_domain == null ? var.domain : "${var.sub_domain}.${var.domain}"

  # Allow the dev user pool to redirect to localhost
  web_client_redirect = flatten(["https://${local.fqdn}", var.allow_local_login ? ["http://localhost:8080"] : []])
}
