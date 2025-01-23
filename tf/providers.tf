provider "aws" {
  region = var.region
}

terraform {
  backend "s3" {}
  required_providers {
    aws = {
      version = "~> 5.83.1"
    }
  }
}