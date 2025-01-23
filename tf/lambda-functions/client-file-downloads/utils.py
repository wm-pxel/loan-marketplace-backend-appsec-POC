import json
import os

def get_file_extention(file_name):
    pos = file_name.rfind(".")
    if pos == -1:
        return ''
    else:
        return file_name[pos:]

def get_s3_bucket(sm_client):
    secret_value_response = sm_client.get_secret_value(SecretId='lm-backend-{}-env'.format(os.getenv('ENV')))
    secrets_json = json.loads(secret_value_response['SecretString'])
    return secrets_json["S3_FILE_UPLOAD_BUCKET"]

def get_mime_type(extension):
    match (extension):
        case "doc":
            mime_type = 'application/msword'
        case "docx":
            mime_type = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
        case "gif":
            mime_type = 'application/gif'
        case "jpeg" | "jpg":
            mime_type = 'application/jpeg'
        case "pdf":
            mime_type = 'application/pdf'
        case "png":
            mime_type = 'application/png'
        case "ppt":
            mime_type = 'application/vnd.ms-powerpoint'
        case "pptx":
            mime_type = 'application/vnd.openxmlformats-officedocument.presentationml.presentation'
        case "xls":
            mime_type = 'application/vnd.ms-excel'
        case "xlsx":
            mime_type = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        case _:
            mime_type = 'application/text'

    return mime_type