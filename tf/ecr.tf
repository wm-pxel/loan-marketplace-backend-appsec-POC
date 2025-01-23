resource "aws_ecr_repository" "loan_marketplace" {
  name                 = "${var.ecr_image_prefix}-${var.environment}" # naming your ECR repository
  image_tag_mutability = "MUTABLE"                                    # allows images to be overwritten

  image_scanning_configuration {
    scan_on_push = true # enables scanning for vulnerabilities on push
  }
}

# If you need to manage the policies for who can access the ECR, you can do so using the following resource
resource "aws_ecr_repository_policy" "loan_marketplace_policy" {
  repository = aws_ecr_repository.loan_marketplace.name

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Sid" : "AllowPushPull",
        "Effect" : "Allow",
        "Principal" : {
          "AWS" : "arn:aws:iam::710894263335:user/deployment",
        },
        "Action" : [
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:DescribeRepositories",
          "ecr:GetRepositoryPolicy",
          "ecr:ListImages",
          "ecr:DeleteRepository",
          "ecr:BatchDeleteImage",
          "ecr:SetRepositoryPolicy",
          "ecr:DeleteRepositoryPolicy"
        ]
      }
    ]
  })
}