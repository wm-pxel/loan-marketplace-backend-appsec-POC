import boto3
import json
import urllib3
import os
import uuid

from auth import get_tokens
from utils import get_s3_bucket
from botocore.exceptions import ClientError
from urllib3.filepost import encode_multipart_formdata

s3_client = boto3.client('s3')
sm_client = boto3.client('secretsmanager')

def lambda_handler(event, context):
    #
    # Variable initialization and Process Setup
    #
    http = urllib3.PoolManager()
    institutionUid = event['institutionUid']
    batchId = event['batchId']
    base_url = event['baseUrl']

    print("Start processing batch (id = {}) ...".format(batchId))
    print("Getting access tokens ...")
    (cognito_token, sf_token) = get_tokens(sm_client, http, institutionUid)
    headers = {
        "Authorization": "Bearer {}".format(cognito_token),
        "Content-Type": "application/json"
    }

    #
    # Get the S3 bucket that we'll be retrieving documents from.
    #
    bucket_name = get_s3_bucket(sm_client)
    print(bucket_name)

    #
    # Get the list of documents to start processing.
    #
    print("Getting document batch ...")
    docBatchJson = get_document_list(base_url, batchId, http, headers)

    if len(docBatchJson) == 0:
        print("There were no documents for the batchId ...")
    else:

        # Check if the batch has already been processed. If set then this batch was completed.
        if not ("processEndDate" in docBatchJson):

            if len(docBatchJson['details']) == 0:
                print("No documents ...")
            else:
                dealUid = docBatchJson['dealId']

                # Record the datetime of starting the batch processing.
                set_batch_start_date(base_url, batchId, http, headers)
                print("Printing documents ...")

                for document in docBatchJson['details']:

                    # Check if the document has been processed. If set then this file was completed.
                    if not ("processEndDate" in document):

                        # Record the datetime of starting the file processing.
                        set_batch_file_start_date(base_url, batchId, document['id'], http, headers)

                        # Get the document data from s3
                        data = get_s3_document(bucket_name, dealUid, document['dealDocument']['documentName'], s3_client)

                        sf_result = None
                        # Update document in salesforce
                        if data:
                            sf_result = update_sf_document(http, sf_token, document, data)
                        else:
                            print('Document Data Not Found')

                        if sf_result == 204:
                            # Record the datetime of completing the file processing
                            set_batch_file_end_date(base_url, batchId, document['id'], http, headers)

            # Record the datetime of completing the file processing.
            set_batch_end_date(base_url, batchId, http, headers)

    print("End processing.")

    return {
        'statusCode': 200,
        'body': json.dumps('Successfully processed documents.')
    }

def get_document_list(base_url, batchId, http, headers):
    url = '{}/api/ext/data/documents/{}'.format(base_url, batchId)
    response = http.request("GET", url, headers=headers)

    if response.status == 200:
        return json.loads(response.data)
    else:
        return {}

def set_batch_start_date(base_url, batchId, http, headers):
    url = '{}/api/ext/data/documents/{}/start'.format(base_url, batchId)
    response = http.request("PATCH", url, headers=headers)
    # Successful status will return 200

    if response.status != 200:
        print(batchId, response.data)

    return response.status

def set_batch_end_date(base_url, batchId, http, headers):
    url = '{}/api/ext/data/documents/{}/complete'.format(base_url, batchId)
    response = http.request("PATCH", url, headers=headers)
    # Successful status will return 200
    if response.status != 200:
        print(batchId, response.data)

    return response.status

def set_batch_file_start_date(base_url, batchId, detailId, http, headers):
    url = '{}/api/ext/data/documents/{}/details/{}/start'.format(base_url, batchId, detailId)
    response = http.request("PATCH", url, headers=headers)
    # Successful status will return 200
    if response.status != 200:
        print(batchId, detailId, response.data)

    return response.status

def set_batch_file_end_date(base_url, batchId, detailId, http, headers):
    url = '{}/api/ext/data/documents/{}/details/{}/complete'.format(base_url, batchId, detailId)
    response = http.request("PATCH", url, headers=headers)
    # Successful status will return 200
    if response.status != 200:
        print(batchId, detailId, response.data)

    return response.status

def get_document_data(http, sf_token, url):
    headers = {
        "Authorization": "Bearer {}".format(sf_token),
        "Content-Type": "application/x-www-form-urlencoded"
    }
    response = http.request("GET", url, headers=headers)

    return response.data

def update_sf_document(http, sf_token, document, data):
    # Extract necessary details from document_details
    url = document['url']

    # Construct the metadata
    metadata = json.dumps({"FirstPublishLocationId": document['salesforceId'],
                           "ReasonForChange": "Request sent to lamina",
                           "PathOnClient": document['dealDocument']['displayName']})


    # Generate a unique boundary string
    boundary_string = '----FileDownloadBoundary'

    headers = {
        "Authorization": "Bearer {}".format(sf_token),
        "Content-Type": "multipart/form-data; boundary=\"{}\"".format(boundary_string)
    }

    fields = {
        'entity_content': (None, metadata, 'application/json'),
        'VersionData': (document['dealDocument']['displayName'], data, 'application/octet-stream')
    }

    encoded_data, content_type = encode_multipart_formdata(fields, boundary=boundary_string)

    try:
        response = http.request('POST', url, body=encoded_data, headers=headers)
        print(f"Response Status {response.status}")
        decoded_data = response.data.decode('utf-8')
        response_data = json.loads(decoded_data)

        if response.status == 201 and not response_data["errors"]:
            print("Document updated successfully")
            return 204
        else:
            print("Failed to update document", response.status, response.data)
            return response.status

    except Exception as e:
        print('error', 'update_salesforce_document', e)
        return 500

def get_s3_document(bucket_name, dealUid, documentName, s3_client):
    # Compute the object key
    object_key = f"{dealUid}/{documentName}"
    print(object_key)

    try:
        # Retrieve the object from S3
        response = s3_client.get_object(Bucket=bucket_name, Key=object_key)

        # Read the content of the object
        data = response['Body'].read()

        return data

    except ClientError as e:
        print('error', 'get_s3_document', e)
        return None