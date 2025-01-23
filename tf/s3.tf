resource "aws_s3_bucket" "loan_marketplace_file_uploads" {
  bucket        = "lm-file-uploads-${var.environment}"
  force_destroy = "false"

  object_lock_enabled = "false"

  tags = {
    Name = "lm-file-uploads-${var.environment}"
  }
  tags_all = {
    Name = "lm-file-uploads-${var.environment}"
  }
}

resource "aws_s3_bucket_ownership_controls" "loan_marketplace_file_uploads" {
  bucket = aws_s3_bucket.loan_marketplace_file_uploads.id
  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}
