data "archive_file" "client_file_uploads_zip_file" {
  source_dir  = "${path.module}/lambda-functions/client-file-uploads"
  output_path = "${path.module}/lambda-functions/client-file-uploads.zip"
  type        = "zip"
}

data "archive_file" "client_file_downloads_zip_file" {
  source_dir  = "${path.module}/lambda-functions/client-file-downloads"
  output_path = "${path.module}/lambda-functions/client-file-downloads.zip"
  type        = "zip"
}

data "archive_file" "pre_sign_up_zip_file" {
  source_dir  = "${path.module}/lambda-functions/pre-sign-up"
  output_path = "${path.module}/lambda-functions/pre-sign-up.zip"
  type        = "zip"
}

resource "aws_lambda_function" "client_file_uploads_lambda" {
    function_name   = "arn:aws:lambda:us-east-1:710894263335:function:lamina-fileManagerService"
    handler         = "file_manager_service.lambda_handler"
    runtime         = "python3.12"
    role            = "arn:aws:iam::710894263335:role/lamina-fileManagerService"
    filename        = data.archive_file.client_file_uploads_zip_file.output_path
    memory_size     = 128
    timeout         = 300
    architectures   = ["arm64"]
    source_code_hash = data.archive_file.client_file_uploads_zip_file.output_base64sha256
    ephemeral_storage {
        size = 512
    }

    tracing_config {
        mode="PassThrough"
    }

    logging_config {
        log_format = "Text"
        log_group  = "/aws/lambda/lamina-fileManagerService"
    }
}

resource "aws_lambda_function" "client_file_downloads_lambda" {
  function_name   = "clientFileDownloadService-${var.environment}"
  handler         = "client_file_download_service.lambda_handler"
  runtime         = "python3.12"
  role            = aws_iam_role.client_file_downloads_role.arn
  filename        = data.archive_file.client_file_downloads_zip_file.output_path
  memory_size     = 128
  timeout         = 300
  architectures   = ["arm64"]
  source_code_hash = data.archive_file.client_file_downloads_zip_file.output_base64sha256

  ephemeral_storage {
    size = 512
  }

  environment {
      variables = {
        ENV = var.environment
      }
  }

  tracing_config {
    mode = "PassThrough"
  }
}

resource "aws_lambda_function" "pre_sign_up" {
  function_name   = "preSignUp-${var.environment}"
  handler         = "pre_sign_up.lambda_handler"
  runtime         = "nodejs20.x"
  role            = aws_iam_role.pre_sign_up.arn
  filename        = data.archive_file.pre_sign_up_zip_file.output_path
  memory_size     = 128
  timeout         = 300
  architectures   = ["arm64"]
  source_code_hash = data.archive_file.pre_sign_up_zip_file.output_base64sha256

  ephemeral_storage {
    size = 512
  }

  environment {
    variables = {
      ENV = var.environment
    }
  }

  tracing_config {
    mode = "PassThrough"
  }
}

resource "aws_lambda_permission" "allow_be_user_invoke" {
  statement_id  = "allow-${var.environment}-be-user-invoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.client_file_downloads_lambda.function_name
  principal     = "arn:aws:iam::710894263335:user/lm-be-user"
}