resource "aws_guardduty_detector" "gd_detector" {
  enable = true
}

resource "aws_guardduty_detector_feature" "runtime_monitoring" {
  detector_id = aws_guardduty_detector.gd_detector.id
  name        = "RUNTIME_MONITORING"
  status      = "ENABLED"

  additional_configuration {
    name   = "EKS_ADDON_MANAGEMENT"
    status = "DISABLED"
  }
  additional_configuration {
    name   = "ECS_FARGATE_AGENT_MANAGEMENT"
    status = "ENABLED"
  }
}

resource "aws_guardduty_detector_feature" "rds_login_monitoring" {
  detector_id = aws_guardduty_detector.gd_detector.id
  name        = "RDS_LOGIN_EVENTS"
  status      = "ENABLED"
}

resource "aws_guardduty_detector_feature" "s3_events_monitoring" {
  detector_id = aws_guardduty_detector.gd_detector.id
  name        = "S3_DATA_EVENTS"
  status      = "ENABLED"
}

resource "aws_guardduty_detector_feature" "lambda_network_logs" {
  detector_id = aws_guardduty_detector.gd_detector.id
  name        = "LAMBDA_NETWORK_LOGS"
  status      = "DISABLED"
}

resource "aws_guardduty_detector_feature" "eks_audit_logs" {
  detector_id = aws_guardduty_detector.gd_detector.id
  name        = "EKS_AUDIT_LOGS"
  status      = "DISABLED"
}

resource "aws_iam_role" "guard_duty_role" {
  name = "malware_protection_plan"
  assume_role_policy = jsonencode({
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "malware-protection-plan.guardduty.amazonaws.com"
        }
      },
    ]
  })

  inline_policy {
    name = "monitor_s3_buckets"
    policy = jsonencode({
      Version = "2012-10-17"
      Statement = [
        {
          "Sid" : "AllowManagedRuleToSendS3EventsToGuardDuty",
          "Effect" : "Allow",
          "Action" : [
            "events:PutRule",
            "events:DeleteRule",
            "events:PutTargets",
            "events:RemoveTargets"
          ],
          "Resource" : [
            "arn:aws:events:us-east-1:710894263335:rule/DO-NOT-DELETE-AmazonGuardDutyMalwareProtectionS3*"
          ],
          "Condition" : {
            "StringLike" : {
              "events:ManagedBy" : "malware-protection-plan.guardduty.amazonaws.com"
            }
          }
        },
        {
          "Sid" : "AllowGuardDutyToMonitorEventBridgeManagedRule",
          "Effect" : "Allow",
          "Action" : [
            "events:DescribeRule",
            "events:ListTargetsByRule"
          ],
          "Resource" : [
            "arn:aws:events:us-east-1:710894263335:rule/DO-NOT-DELETE-AmazonGuardDutyMalwareProtectionS3*"
          ]
        },
        {
          "Sid" : "AllowPostScanTag",
          "Effect" : "Allow",
          "Action" : [
            "s3:PutObjectTagging",
            "s3:GetObjectTagging",
            "s3:PutObjectVersionTagging",
            "s3:GetObjectVersionTagging"
          ],
          "Resource" : [
            "arn:aws:s3:::lm-file-uploads-prod/*",
            "arn:aws:s3:::lm-file-uploads-qa/*"
          ]
        },
        {
          "Sid" : "AllowEnableS3EventBridgeEvents",
          "Effect" : "Allow",
          "Action" : [
            "s3:PutBucketNotification",
            "s3:GetBucketNotification"
          ],
          "Resource" : [
            "arn:aws:s3:::lm-file-uploads-prod",
            "arn:aws:s3:::lm-file-uploads-qa"
          ]
        },
        {
          "Sid" : "AllowPutValidationObject",
          "Effect" : "Allow",
          "Action" : [
            "s3:PutObject"
          ],
          "Resource" : [
            "arn:aws:s3:::lm-file-uploads-prod/malware-protection-resource-validation-object",
            "arn:aws:s3:::lm-file-uploads-qa/malware-protection-resource-validation-object"
          ]
        },
        {
          "Effect" : "Allow",
          "Action" : [
            "s3:ListBucket"
          ],
          "Resource" : [
            "arn:aws:s3:::lm-file-uploads-prod",
            "arn:aws:s3:::lm-file-uploads-qa"
          ]
        },
        {
          "Sid" : "AllowMalwareScan",
          "Effect" : "Allow",
          "Action" : [
            "s3:GetObject",
            "s3:GetObjectVersion"
          ],
          "Resource" : [
            "arn:aws:s3:::lm-file-uploads-prod/*",
            "arn:aws:s3:::lm-file-uploads-qa/*"
          ]
        }
      ]
    })
  }
}

resource "aws_guardduty_malware_protection_plan" "s3_malware_scan_qa" {
  role = aws_iam_role.guard_duty_role.arn

  protected_resource {
    s3_bucket {
      bucket_name = "lm-file-uploads-qa"
    }
  }

  actions {
    tagging {
      status = "DISABLED"
    }
  }
}

resource "aws_guardduty_malware_protection_plan" "s3_malware_scan_prod" {
  role = aws_iam_role.guard_duty_role.arn

  protected_resource {
    s3_bucket {
      bucket_name = "lm-file-uploads-prod"
    }
  }

  actions {
    tagging {
      status = "DISABLED"
    }
  }
}
