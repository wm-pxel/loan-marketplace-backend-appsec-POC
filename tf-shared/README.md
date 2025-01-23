# Terraform Shared Resources
This directory is for setting up resources with terraform that should be shared across environments,
or used at the organization/account level. This way they can be setup once in terraform and not
recreated or attempted to be recreated for each environment's deployment.

For example when AWS Guard Duty is enabled it is turned on at the account level. If it is in the main
tf directory it would be enabled once when deploying to dev, then deploying to test would try to enable
it again, which would fail saying that it is already enabled.

In all other instances, terraform configuration should go in the [../tf](../tf) directory
so it can be applied per environment.
