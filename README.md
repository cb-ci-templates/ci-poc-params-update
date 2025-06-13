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

* Case1: A parametrized Pipeline will be started using the "Build with parameters" option in the UI. The Pipeline uses the Active Choice Parameter Plugin and its  [Groovy script](https://plugins.jenkins.io/uno-choice/#plugin-content-the-script)hook to get the dynamic values in a Pipeline pre-parameter render phase 
* Case2: Another Pipeline runs before the actual parametrized Pipeline and updates the Parametrized Pipeline config before the button "Build with Parameters" is clicked 

Note: The samples in this repo are scanning a remote Git repo (this) for all branches and supply the branch list as parameter values/choices ("Build-with-parameters")
Other integrations are also possible, f.e referencing Nexus, gcp-buckets, s3 buckets or getting values from `lastSuccessfulBuild/artifact/` data file from another Job URL (see below)

# Pipelines to test

* All test pipelines below will scan this Git repo for its branches to display them as generic values in a choice parameter list
* Whenever a branch is added or deleted, the updated branch list will appear as a drop down parameter list

| Pipeline                                        | Approach                                                              | Pro                                                                                                                             | Con                                                        |
|-------------------------------------------------|-----------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------|
| Jenkinsfile-ActiveChoice-GroovyScript.groovy    | Groovy script hook in a Pre-render phase                              | AC Plugin built in feature, zero controller executors,no Init Job required                                                      | Active Choice Plugin required.script approvals required    |
| Jenkinsfile-updateParameters-CasC.groovy        | Init Pipeline, using Configuration as Code to update parameter values | CloudBees only, no script approvals required, zero controller executors, Can update many Jobs, Can update on remote Controllers | CasC Plugin required, Init Job required                    |
| Jenkinsfile-updateParameters-DSL.groovy         | Init Pipeline, using JobDsl to update parameter values                | Zero controller executors                                                                                                       | JobDsl plugin required,script approvals required           |
| Jenkinsfile-updateParameters-Groovy.groovy      | Init Pipeline, using Groovy                                           | Zero controller executors  No additional Plugin required                                                                        | script approvals required, can lead to complex Groovy code |

The diagram below shows how the four Jenkinsfiles in this repository are related and what the workflow is.
* Just the `Jenkinsfile-ActiveChoice-GroovyScript.groovy Pipeline updates it own parameters
* The other 3 Pipeline can be seen as init Pipeline that are running before the actual parametrized Job just to get the branch names from git and to update the parametrized `example-pipeline`. 
* The `example-pipeline` will be created either by the `Jenkinsfile-updateParameters-CasC.groovy`Pipeline or by the `Jenkinsfile-updateParameters-DSL.groovy`
  ![Parameters](images/Parameters.svg)

# Conclusion/Recommendation

* For a single Pipeline that requires dynamic parameters the Active Choice with Groovy Hooks is seen as the best approach, however, it could require a lot of script aprovals
* When more dynamic parametrized Pipelines need to be managed, CasC or JobDSL seems to be a more efficient approach (CasC should be preferred in that case!)

# Notes Active Choice dynamic options

We are using the [script](https://plugins.jenkins.io/uno-choice/#plugin-content-the-script) in the example to get GitBranches as options
* CREDENTIAL_ID: GtHub access token, configured in Jenkins credentials store with id "gh-token-ci-templates-repo-classic" as secret text
* URL: The Git repo http url. This is the repo we want to scan the branches
* Note: Secrets are retrieved here in this example from the Global credentials store, folder level credentials look a bit different

```
def CREDENTIAL_ID = "gh-token-ci-templates-repo-classic"
def SECRET = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getStore().getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global()).find { it.getId().equals(CREDENTIAL_ID) }.getSecret().getPlainText()
def URL = "https://" + SECRET + "@github.com/cb-ci-templates/ci-poc-params-update.git"
def result = ["/bin/bash", "-c", "git ls-remote -h " + URL + " | sed 's/.*refs\/heads\/\(.*\)/\1/'"].execute().text.tokenize();
return result   
```

To retrieve param data from the archiveArtifact step of another Job you can use the following approach:
* CREDENTIAL_ID: jenkins-token, configured in Jenkins credentials store with id "jenkins-token" as secret text. Format of the secret-text: `user:jenkinstoken`
* URL: The Git repo http url. This is the repo we want to scan the branches
* Note: Secrets are retrieved here in this example from the Global credentials store, folder level credentials look a bit different 

Init job that uses the `archiveArtifact` Step to create some parameter data
The file can later be referenced in the ActiveChoice as: 
> https://<JENKINS_URL>/<PATH_TO_JOB>/lastSuccessfulBuild/artifact/newparams.txt

Example:
```
...
  steps {
      sh 'echo "one\ntwo\nthree\n" > newparams.txt'
      sh "cat newparams.txt"
      archiveArtifacts artifacts: 'newparams.txt', fingerprint: true, followSymlinks: false
  }
}
 ...
```

* ActiveChoice Groovy script to retrieve data from the last `lastSuccessfulBuild/artifact/` URL
* Note: You need to adjust the URL and job path below to your needs `your.controller.com/sb/job/ci-templates-demo/job/DEMO-ParameterUsage/job/initData/lastSuccessfulBuild/artifact/newparams.txt/*view*/`

```
 def CREDENTIAL_ID = "jenkins-token"
 def SECRET = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getStore().getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global()).find { it.getId().equals(CREDENTIAL_ID) }.getSecret().getPlainText()
 def url = "https://"+ SECRET + "@your.controller.com/sb/job/ci-templates-demo/job/DEMO-ParameterUsage/job/initData/lastSuccessfulBuild/artifact/newparams.txt/*view*/"
 def result = ["/bin/bash", "-c", "curl -L " + url].execute().text.tokenize();
 return result
```

* To download from GitHib  raw URL will NOT work because GitHib has a cache that expires just every 5 min.
* There is no way to bypass the cache, so DON`T try the following:

```
def content=new URL("https://raw.githubusercontent.com/cb-ci-templates/ci-poc-params-update/main/resources/choices.txt").getText()
def values = []
for(def line : content.split(',')) {
    values.add(line.trim())
}
return values
```

# Extended Choice Parameter Plugin

* [Extended Choice Parameter Plugin](https://plugins.jenkins.io/extended-choice-parameter/) is END OF LIFE, don`t use it anymore!!!
* Given the age of this plugin and the number of security issues with the code base, no further development is expected. There are many excellent alternatives that may suit your purpose.
* ALTERNATIVES
  There are other parameter plugins to use for user inputs.
* [Active Choices](https://plugins.jenkins.io/uno-choice)
* [Json Editor Parameter](https://plugins.jenkins.io/json-editor-parameter/)

---

## 🔬 Useful Groovy Snippets

### Fetch Git Branches
* https://gist.github.com/jseed/ac0218e86c88751942c847b10637bb56

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
