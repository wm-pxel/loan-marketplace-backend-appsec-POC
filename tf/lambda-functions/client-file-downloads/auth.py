import os
import json
import base64

def parse_secret(secret_response):
    if 'SecretString' in secret_response:
        secret = json.loads(secret_response['SecretString'])
    else:
        secret = base64.b64decode(secret_response['SecretBinary'])

    return secret

def get_tokens(sm_client, http, institution_uid):
    secret_name_sf = 'lamina-service-sf-secret-{}'.format(os.getenv('ENV'))
    secret_name_cognito = 'lamina-service-cognito-credentials-{}'.format(os.getenv('ENV'))

    secret_value_response = parse_secret(sm_client.get_secret_value(SecretId=secret_name_cognito))
    secret_value_sf_response = parse_secret(sm_client.get_secret_value(SecretId=secret_name_sf))

    cognito_issuer = secret_value_response['issuer']
    sf_issuer = secret_value_sf_response['%s-issuer' % institution_uid]


    headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    }

    # Exchange cognito credentials for a token
    cognito_token_response = http.request_encode_body(
        "POST",
        cognito_issuer + '/oauth2/token',
        fields={
            "grant_type": "client_credentials",
            "client_id": secret_value_response["client_id"],
            "client_secret": secret_value_response["client_secret"]
        },
        headers=headers,
        encode_multipart=False
    )

    # Exchange SF credentials for a token
    salesforce_token_response = http.request_encode_body(
        "POST",
        sf_issuer + "/services/oauth2/token",
        fields={
            "grant_type": "client_credentials",
            "client_id": secret_value_sf_response['%s-keyid' % institution_uid],
            "client_secret": secret_value_sf_response['%s-secret' % institution_uid]
        },
        headers=headers,
        encode_multipart=False
        )

    return (
        json.loads(cognito_token_response.data)["access_token"],
        json.loads(salesforce_token_response.data)["access_token"],
    )