import boto3
import json
import urllib3

from auth import get_tokens
from datetime import datetime
from utils import get_mime_type
from utils import get_s3_bucket

s3_client = boto3.client('s3')
sm_client = boto3.client('secretsmanager')

def lambda_handler(event, context):
    #
    # Variable initialization and Process Setup
    #
    http = urllib3.PoolManager()
    base_url = event['baseUrl']
    environment = event['environment']
    institutionUid = event['institutionUid']
    batchId = event['batchId']
    send_email_trigger = False

    print("Start processing batch (id = {}) ...".format(batchId))
    print("Getting access tokens ...")
    (cognito_token, sf_token) = get_tokens(sm_client, http, environment, institutionUid)
    headers = {
        "Authorization": "Bearer {}".format(cognito_token),
        "Content-Type": "application/json"
    }

    #
    # Get the S3 bucket that we'll saving documents.
    #
    bucket_name = get_s3_bucket(environment, sm_client)
    print(bucket_name)

    #
    # Get the list of documents to start processing.
    #
    print("Getting document batch ...")
    docBatchJson = get_document_list(base_url, batchId, http, headers)
    dealExternalId = docBatchJson['dealExternalId']

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

                        # Get the document from nCino
                        data = get_document_data(http, sf_token, document['url'])

                        # Create values that we don't get directly from integration but needed in Lamina.
                        document['type'] = get_mime_type(document["extension"])
                        document['documentName'] = datetime.now().strftime("%y%m%d%H%M%S%f") + '.' + document["extension"]

                        # Save file to S3 bucket
                        s3_result = create_s3_document(bucket_name, dealUid, document['documentName'], data, s3_client)

                        # Create deal document record in Lamina
                        lamina_result = create_deal_document(base_url, docBatchJson['dealExternalId'], document, http, headers)

                        if ( s3_result == 201 ) and ( lamina_result == 201 ):
                            # Record the datetime of completing the file processing
                            send_email_trigger = True # Set to true if at least one file is processed successfully.
                            set_batch_file_end_date(base_url, batchId, document['id'], http, headers)
                            create_timeline_activity(base_url, dealExternalId, http, headers, document['documentExternalId'])
    
            # Record the datetime of completing the file processing.
            set_batch_end_date(base_url, batchId, http, headers)

    print("End processing.")

    if send_email_trigger:
        send_email_notification(base_url, dealExternalId, http, headers)

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

def create_s3_document(bucket_name, dealUid, documentName, data, s3_client):
    # Compute the object key
    object_key = dealUid + '/' + documentName
    print(object_key)

    try:

        # Upload the octet stream to S3
        s3_client.put_object(Bucket=bucket_name, Body=data, Key=object_key)

        return 201

    except Exception as e:

        print('error', 'create_s3_document', e)
        return 500

def create_deal_document(base_url, dealExternalId, document, http, headers):
    # Create the payload for creating the document.

    dealDocument = {}
    dealDocument['displayName'] = document['displayName']
    dealDocument['documentName'] = document['documentName']
    dealDocument['type'] = document['type']
    dealDocument['category'] = document['category']
    dealDocument['documentExternalId'] = document['documentExternalId']
    dealDocument['createdById'] = document['createdById']

    # Convert the dictionary to URL-encoded string.
    encoded_data = json.dumps(dealDocument).encode('utf-8')
    
    url = '{}/api/ext/deals/{}/documents'.format(base_url, dealExternalId)
    response = http.request("POST", url, headers=headers, body=encoded_data)

    if response.status != 201:
        print('error', 'create_deal_document', response.status, response.data)

    # Successful status code is 201
    return response.status

def send_email_notification(env, dealExternalId, http, headers):
    url = '{}/api/ext/data/{}/notify'.format(env, dealExternalId)
    response = http.request("GET", url, headers=headers)

    if response.status == 200:
        return json.loads(response.data)
    else:
        return {}


def create_timeline_activity(base_url, dealExternalId, http, headers, documentExternalId):
    url = '{}/api/ext/data/{}/activity'.format(base_url, dealExternalId)
    dealDocument = {}
    dealDocument['documentExternalId'] = documentExternalId
    # Convert the dictionary to URL-encoded string.
    encoded_data = json.dumps(dealDocument).encode('utf-8')

    response = http.request("POST", url, headers=headers, body=encoded_data)

    if response.status == 200:
        return json.loads(response.data)
    else:
        return {}