/* global fetch */
import {GetSecretValueCommand, SecretsManagerClient,} from "@aws-sdk/client-secrets-manager";

const getSecret = async (secretName, region) => {
  const client = new SecretsManagerClient({ region: region });
  try {
    const response = await client.send(
      new GetSecretValueCommand({
        SecretId: secretName,
      })
    );

    return response.SecretString;
  } catch (err) {
    console.log(`Error retrieving secret ${err.code}`);
    throw err;
  }
}

export const handler = async function (event, context) {
  console.log('Event data:', event)
  const { detail = {} } = event
  const { firstObservedAt = '', remediation: { recommendation: { text = '' } = {} } = {} } = detail

  // only make tickets for findings that were observed in the last day
  const oneDayAgo = new Date().getTime() - (24 * 60 * 60 * 1000)
  if (new Date(firstObservedAt) > oneDayAgo) {
    const body = {
      fields: {
        project: {
          id: '10200'
        },
        issuetype: {
          id: '10002'
        },
        summary: 'DevOps - Inspector Vulnerability',
        description: {
          version: 1,
          type: 'doc',
          content: [{ 
            type:'paragraph',
            content: [{ 
              type: 'text',
              text
            }]
          }]
        },
        reporter: { id: '630cfa7d62fe1e6eac6bdf0b' },
        assignee: { id: '630cfa7d62fe1e6eac6bdf0b' },
        priority: {
          name: 'High',
          id: '2'
        }
      }
    }
  
    const secretName = process.env.JIRA_TOKEN_SECRET_NAME;
    const region = process.env.AWS_REGION;
  
    try {
      const jiraToken = await getSecret(secretName, region)
      const encodedAuth = Buffer.from(`dosaki@westmonroe.com:${jiraToken}`).toString('base64')
  
      const response = await fetch(
        'https://westmonroe.atlassian.net/rest/api/3/issue?updateHistory=true&applyDefaultValues=false&skipAutoWatch=true',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-Force-Accept-Language': true,
            'Accept-Language': 'en',
            Authorization: `Basic ${encodedAuth}`
          },
          body: JSON.stringify(body)
        }
      )
      const jiraResponse = await response.json();
      console.log("jira item created: \n" + JSON.stringify(jiraResponse, null, 2));
    } catch (error) {
      console.log('Error creating jira item', error)
      throw error
    }
  }
  
  return
};
