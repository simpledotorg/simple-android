# Generating a hash for SMS verification

We use the Play Services [SMS Retriever API](https://developers.google.com/identity/sms-retriever/overview) to automatically read login OTPs.

In order to do this, the SMS needs to contain a hash that identifies the app to deliver the SMS to. Following are the instructions to generate the hash for a given application ID.

### Prerequisites

**JDK 1.8**

This is required to use the `keytool` command. The instructions for setting up the JDK are available on the main [README](https://github.com/simpledotorg/simple-android#pre-requisites).

**`sha256sum`**

This is a command line tool. If you do not have it installed, use the following commands to install it using [Homebrew](https://brew.sh/).

- `brew install coreutils`
- Add the following lines to your shell config file: `PATH="/usr/local/opt/coreutils/libexec/gnubin:$PATH"`


### Generate a hash
Use the [helper script](https://github.com/simpledotorg/simple-android/blob/master/sms-signature) in the project root to generate the SMS signature. The usage is as follows:

`./sms-signature <keystore_location> <key_alias> <application_id>`

For example, if the keystore file is `simple.keystore`, the key alias is `app`, and the application ID is `org.simple.clinic`, use the following command to generate the hash:

`./sms-signature simple.keystore app org.simple.clinic`

**For the Simple developer team**

The keystore used to sign the builds are in possession of the team leads. If you need to add a new build type and generate a hash, talk to the leads to get access to the keystore.
