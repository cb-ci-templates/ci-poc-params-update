

# Extended Choice Parameter Plugin 
* https://plugins.jenkins.io/extended-choice-parameter/ 
* END OF LIFE
  Given the age of this plugin and the number of security issues with the code base, no further development is expected. There are many excellent alternatives that may suit your purpose.
* ALTERNATIVES
There are other parameter plugins to use for user inputs.
Json Editor Parameter
Active Choices: https://plugins.jenkins.io/uno-choice
Extensible Choice
Editable Choice

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
* https://stackoverflow.com/questions/35205665/jenkins-credentials-store-access-via-groovy
