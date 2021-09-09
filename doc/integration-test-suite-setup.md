# Running server integration tests

Simple is heavily dependent on the sync services managed by the Simple server. In order to ensure that these continue to run smoothly, we have a
series of integration tests which verify the behaviour of the sync APIs.

In order to maintain isolation between test runs, we clone the server source code in our CI runs and setup the server
on [Heroku](https://www.heroku.com).

This document describes the Heroku setup with the integration tests and what needs to be done to maintain it.

## Heroku config variables

The server code needs certain config variables to be set in the server environment to build and run. In this section, we are going to talk about how
the server review setup differs from what we are doing on the mobile app test suite and how to maintain them going forward.

The server team uses [Heroku review apps](https://devcenter.heroku.com/articles/github-integration-review-apps) to run pull request scoped server
instances for testing. In accordance with the Heroku review apps conventions, most of the config variables (that don't need to be private) are defined
in a top-level [`app.json`](https://github.com/simpledotorg/simple-server/blob/master/app.json) file in the server side repository.

In addition, there are other private configuration variables that should not be checked into the repository. These variables are set manually on the
review apps configuration on the Heroku dashboard.

When we setup a server for a test run, we need to set both the public and private configuration variables ourselves since the `app.json` configuration
method and the dashboard configuration method are only available Heroku review apps.

### Public Heroku config variables

Since we check out the server side source code as part of the Android test suite, we can manually parse the `app.json` ourselves and set them on the
Heroku apps we start. We wrote a python script, [`server_heroku_env_setup.py`](../.github/scripts/server_heroku_env_setup.py) that does this job.

### Private Heroku config variables

This is a bit more complicated since the only way to set them while keeping them private is via
the [GitHub Actions secrets](https://docs.github.com/en/actions/reference/encrypted-secrets) mechanism. Since passing in multiple secrets via the
GitHub Actions workflow syntax is hard to maintain, we decided to describe the secrets in a multi-line string in the `key=value` format

```properties
KEY1=VALUE1
KEY2=VALUE2
```

Below is a canonical list of the all the properties we set in this way. The actual secret values here are redacted, please ask someone from the server
team for them if you need to edit these properties in anyway.

```properties
SENDGRID_USERNAME=<redacted>
SENTRY_DSN=<redacted>
SEED_GENERATED_ACTIVE_USER_ROLE=Seed User | Active
SEED_GENERATED_ADMIN_PASSWORD=trying to fix hypertension
SEED_GENERATED_INACTIVE_USER_ROLE=Seed User | Inactive
SIMPLE_APP_SIGNATURE=<redacted>
TWILIO_ACCOUNT_SID=<redacted>
TWILIO_REMINDERS_ACCOUNT_SID=<redacted>
SEED_TYPE=empty
SENTRY_SECURITY_HEADER_ENDPOINT=<redacted>
EXOTEL_TOKEN=<redacted>
SENDGRID_PASSWORD=<redacted>
TWILIO_AUTH_TOKEN=<redacted>
TWILIO_PHONE_NUMBER=<redacted>
TWILIO_REMINDERS_ACCOUNT_AUTH_TOKEN=<redacted>
TWILIO_REMINDERS_ACCOUNT_PHONE_NUMBER=<redacted>
```

However, GitHub actions does not support multi-line strings, so the quickest way to get this to work was to encode the secrets string using `Base64`
and store the encoded string as a secret. Then we can decode the string in the test suite and set the Heroku config variables.

A quick way to encode the block shown above is to save the block into a text file (say `heroku_secrets.properties`) and then run:

```shell
base64 --wrap=0 heroku_secrets.properties
```

The output string can then be saved directly as the `HEROKU_SECRET_PROPERTIES` secret property as passed as an argument to the environment setup
script mentioned earlier as additional config variables to be set.

**Note:** While the text block is written is similar to a Java Properties file, it does not support the full spec. i.e, comments, etc are not
supported.
