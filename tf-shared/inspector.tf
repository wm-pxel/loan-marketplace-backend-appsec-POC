resource "aws_inspector2_enabler" "inspector" {
  account_ids    = ["710894263335"]
  resource_types = ["ECR", "LAMBDA", "LAMBDA_CODE"]
}
