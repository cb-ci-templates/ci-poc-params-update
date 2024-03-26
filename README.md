

# Extended Choice Parameter Plugin 
* (Extended Choice Parameter Plugi)[https://plugins.jenkins.io/extended-choice-parameter/]
* Extended Choice Parameter Plugin is END OF LIFE, don`t use it anymore!!!
  Given the age of this plugin and the number of security issues with the code base, no further development is expected. There are many excellent alternatives that may suit your purpose.

* ALTERNATIVES
There are other parameter plugins to use for user inputs.
* [Active Choices](ttps://plugins.jenkins.io/uno-choice)
* [Json Editor Parameter](https://plugins.jenkins.io/json-editor-parameter/)


# Active Choice dynamic options

We are using the `script` in the example to get GitBranches as options
https://plugins.jenkins.io/uno-choice/#plugin-content-the-script

```
def CREDENTIAL_ID = "gh-token-ci-templates-repo-classic"
def SECRET = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getStore().getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global()).find { it.getId().equals(CREDENTIAL_ID) }.getSecret().getPlainText()
def URL = "https://" + SECRET + "@github.com/cb-ci-templates/ci-poc-params-update.git"
def result = ["/bin/bash", "-c", "git ls-remote -h " + URL + " | sed 's/.*refs\/heads\/\(.*\)/\1/'"].execute().text.tokenize();
return result   
```
CREDENTIAL_ID: GtHub access token, configured in Jenkins credentials store with id "gh-token-ci-templates-repo-classic" as secret text 
URL: The Git repo http url. This is the repo we want to scan the branches 




# Groovy 

## Get Branches

* https://gist.github.com/jseed/ac0218e86c88751942c847b10637bb56

## Credentials

To reference credentials in the Active Choice Parameters Groovy script section:

```def CREDENTIAL_ID = "<key_credential_id"```

One liner to get a private key credential:
See [ssh credentials implementations](https://javadoc.jenkins.io/plugin/ssh-credentials/com/cloudbees/jenkins/plugins/sshcredentials/impl/) for methods to extract values

```def PRIVATE_KEY = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getStore().getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global()).find { it.getId().equals(CREDENTIAL_ID) }.getPrivateKey()```

One liner to get a username/password credentials:
See [username password credentials implementations](https://javadoc.jenkins.io/plugin/credentials/com/cloudbees/plugins/credentials/impl/UsernamePasswordCredentialsImpl.html) for methods to extract values

```
def PASSWORD = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getStore().getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global()).find { it.getId().equals(CREDENTIAL_ID) }.getPassword()
def USERNAME = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getStore().getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global()).find { it.getId().equals(CREDENTIAL_ID) }.getUsername()
```

One liner to get a string credential:
See [plain credentials implementation](https://stackoverflow.com/questions/35205665/jenkins-credentials-store-access-via-groovy#:~:text=plain%20credentials%20implementation) for methods to extract values

```
def SECRET = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getStore().getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global()).find { it.getId().equals(CREDENTIAL_ID) }.getSecret().getPlainText()
```

see  https://stackoverflow.com/questions/35205665/jenkins-credentials-store-access-via-groovy

