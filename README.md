# About

This repository contains four approaches on how to deal with dynamic Parameter values declarative pipelines.
Each of the approaches has pro and cons that are reflected here.

# Objective

The overall objective when using dynamic Parameter values for Pipelines is to

* [Set the number of executors on a Jenkins Controller always to zero](https://docs.cloudbees.com/docs/cloudbees-ci-kb/latest/best-practices/app-performance-best-practices#_built_in_node_formerly_known_as_master_node_build_executors)
* [Do all work within an agent](https://docs.cloudbees.com/docs/cloudbees-ci/latest/pipelines/pipeline-best-practices#_do_all_the_work_within_an_agent)
* [Reduce the amount of Groovy](https://docs.cloudbees.com/docs/cloudbees-ci/latest/pipelines/pipeline-best-practices#_reduce_the_amount_of_groovy_code_executed_by_pipelines)
* [Avoid script approvals](https://docs.cloudbees.com/docs/cloudbees-ci/latest/pipelines/pipeline-best-practices#_avoid_script_security_exceptions)

# Use case

two main use cases of dynamic parameter values will be reflected here

* A parametrized pipeline will be started using the "Build with parameters" option in the UI. The Pipeline will 
  * The Pipeline uses the Active Choice Parameter Plugin and its "groovy script" hook to get the dynamic values in a Pipeline pre-parameter render phase 
* Another Pipeline runs before the actual parametrized Pipeline and updates the Parametrized Pipeline config before the button "Build with Parameters" is clicked 


# Pipelines to test

* All test pipelines below will scan this Git repo for its branches to display them as generic values in a choice parameter list
* Whenever a branch is added or deleted, the updated branch list will appear as a drop down parameter list

| Pipeline                                        | Approach                                                              | Pro                                                                        | Con                                                        |
|-------------------------------------------------|-----------------------------------------------------------------------|----------------------------------------------------------------------------|------------------------------------------------------------|
| Jenkinsfile-ActiveChoice-GroovyScript.groovy    | Groovy script hook in a Pre-render phase                              | AC Plugin built in feature, zero controller executors,no Init Job required | Active Choice Plugin required.script approvals required    |
| Jenkinsfile-updateParameters-CasC.groovy        | Init Pipeline, using Configuration as Code to update parameter values | no script approvals required, zero controller executors                    | CasC Plugin required, Init Job required                    |
| Jenkinsfile-updateParameters-DSL.groovy         | Init Pipeline, using JobDsl to update parameter values                | zero controller executors                                                  | JobDasl plugin required,script approvals required          |
| Jenkinsfile-updateParameters-Groovy.groovy      | Init Pipeline, using Groovy                                           | zero controller executors  No additional Plugin required                   | script approvals required, can lead to complex Groovy code |


![Parameters](images/Parameters.svg)


# Notes Active Choice dynamic options

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

# Extended Choice Parameter Plugin

* (Extended Choice Parameter Plugin)[https://plugins.jenkins.io/extended-choice-parameter/]
* Extended Choice Parameter Plugin is END OF LIFE, don`t use it anymore!!!
  Given the age of this plugin and the number of security issues with the code base, no further development is expected. There are many excellent alternatives that may suit your purpose.
* ALTERNATIVES
  There are other parameter plugins to use for user inputs.
* [Active Choices](ttps://plugins.jenkins.io/uno-choice)
* [Json Editor Parameter](https://plugins.jenkins.io/json-editor-parameter/)

# Groovy Stuff and some useful links and notes

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
