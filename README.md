# Dynamic Parameters in Declarative Jenkins Pipelines

## Overview

This repository showcases **four distinct approaches** for implementing **dynamic parameter values** in Jenkins Declarative Pipelines. Each method is tailored for specific scenarios and environments, highlighting the trade-offs between flexibility, plugin dependencies, and security considerations.

## Objectives

The primary goal of dynamic parameters is to **streamline UI-driven pipeline executions** while maintaining best practices in Jenkins. These include:

- ✅ [Set Jenkins controller executors to zero](https://docs.cloudbees.com/docs/cloudbees-ci-kb/latest/best-practices/app-performance-best-practices#_built_in_node_formerly_known_as_master_node_build_executors)
- ✅ [Execute all pipeline work within agents](https://docs.cloudbees.com/docs/cloudbees-ci/latest/pipelines/pipeline-best-practices#_do_all_the_work_within_an_agent)
- ✅ [Minimize Groovy execution in pipelines](https://docs.cloudbees.com/docs/cloudbees-ci/latest/pipelines/pipeline-best-practices#_reduce_the_amount_of_groovy_code_executed_by_pipelines)
- ✅ [Avoid script approvals and security exceptions](https://docs.cloudbees.com/docs/cloudbees-ci/latest/pipelines/pipeline-best-practices#_avoid_script_security_exceptions)

## Use Cases


This repository addresses two core use cases:

### 📂 Case 1: UI-based Parameter Selection

- Users start the pipeline using **"Build with Parameters"**.
- Parameters are dynamically populated using the [Active Choice Plugin](https://plugins.jenkins.io/uno-choice/) and Groovy scripts.

### 🚧 Case 2: Pipeline-Driven Configuration

- A separate **initialization pipeline** dynamically configures another pipeline's parameters **before** user interaction.

## Example Integration Sources for Dynamic Values

- Git repository branches (e.g., this repo)
- Nexus, GCP Buckets, S3
- Jenkins job artifacts (e.g., `lastSuccessfulBuild/artifact/`)

## Pipeline Demonstrations

All pipeline examples scan this Git repository for branches and present them as dropdown parameter values.

| Pipeline File                                                                                | Approach                                                                                           | Pros                                                                                      | Cons                                                                                |
|----------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| [Jenkinsfile-ActiveChoice-GroovyScript.groovy](Jenkinsfile-ActiveChoice-GroovyScript.groovy) | Groovy in UI pre-render phase                                                                      | Built-in feature of Active Choice plugin, no need for init job, zero controller executors | Requires plugin and script approvals                                                |
| [Jenkinsfile-updateParameters-CasC.groovy](Jenkinsfile-updateParameters-CasC.groovy)         | Config-as-Code (CasC) + Init Pipeline                                                              | CloudBees native, no script approval needed, scalable across jobs/controllers             | CloudBees only, CasC plugin required                                                |
| [Jenkinsfile-updateParameters-DSL.groovy](Jenkinsfile-updateParameters-DSL.groovy)           | Job DSL + Init Pipeline, references [jobdsl_updateJobParams.groovy](jobdsl_updateJobParams.groovy) | Zero controller executors                                                                 | DSL plugin needed, script approval required                                         |
| [Jenkinsfile-updateParameters-Groovy.groovy](Jenkinsfile-updateParameters-Groovy.groovy)     | Plain Groovy approach, utilisation of Jenkins API                                                  | No additional Plugins required                                                            | complexity grows easily (script approval and controller executor might be required) | 

### Relationship Diagram
The diagram below shows how the four Jenkinsfiles in this repository are related and what the workflow is.
- Only `Jenkinsfile-ActiveChoice-GroovyScript.groovy` updates its own parameter list.
- The other three act as **Init Pipelines**, updating the `example-pipeline` before user input.
- All test pipelines below will scan this Git repo for its branches to display them as generic values in a choice parameter list
- Whenever a branch is added or deleted, the updated branch list will appear as a drop-down parameter list
- The `example-pipeline` is dynamically created and configured via either CasC or DSL. See `Jenkinsfile-updateParameters-CasC.groovy`Pipeline or  `Jenkinsfile-updateParameters-DSL.groovy`
  ![Parameters](images/Parameters.svg)

## Recommendations

- 🌟 Use **Active Choice** for single pipelines where dynamic input is needed.
- 🚀 For managing **multiple jobs at scale**, consider using **CasC** (if on CloudBees) or **Job DSL**.
- 🚀 Consider reading parameters from a property file or yaml config file (per repo branch).

---

## 🔍 Active Choice Plugin Examples

### Fetch Git Branches (Groovy Script)

NOTE: 
* To expose credentials in the URL like `https://SECRET@github.com` is NOT secure!!!
* If you want to integrate this into your Active Choice Script, you need to change
  ```awk '{print \$2}'``` to ```awk '{print \\$2}' ```.  (Two backslashes!) . See sample here [Jenkinsfile-ActiveChoice-GroovyScript.groovy](Jenkinsfile-ActiveChoice-GroovyScript.groovy)
* If you run it from the script console, you can copy it as it is (just one backslash ``` awk '{print \$2}'  ```)

```groovy
def CREDENTIAL_ID = "gh-token-ci-templates-repo-classic"
def SECRET = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance()
  .getStore()
  .getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global())
  .find { it.getId().equals(CREDENTIAL_ID) }
  .getSecret()
  .getPlainText()
def URL = "https://" + SECRET + "@github.com/cb-ci-templates/ci-poc-params-update.git"
def result = ["/bin/bash", "-c", "git ls-remote -h " + URL + " | awk '{print \$2}' | sed 's|refs/heads/||'"].execute().text.tokenize();
return result
```

A more secure alternative looks like this:

```groovy
def CREDENTIAL_ID = "gh-token-ci-templates-repo-classic"
def SECRET = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance()
  .getStore()
  .getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global())
  .find { it.getId().equals(CREDENTIAL_ID) }
  .getSecret()
  .getPlainText()
def url = new URL("https://api.github.com/repos/cb-ci-templates/ci-poc-params-update/branches")
def conn = url.openConnection()
conn.setRequestProperty("Authorization", "token " + SECRET)
def text = conn.inputStream.text
def branches = new groovy.json.JsonSlurper().parseText(text)*.name
return branches

```



### Retrieve Data from Another Job’s Artifact

Init job archives a file (basicly the parameter list we want to reference):

```groovy
steps {
    sh 'echo "one\ntwo\nthree\n" > newparams.txt'
    archiveArtifacts artifacts: 'newparams.txt'
}
```

* ActiveChoice Groovy script to retrieve data from the last `lastSuccessfulBuild/artifact/` URL
* Note: 
  * You need to adjust the URL and job path below to your needs: your.controller.com/sb/job/ci-templates-demo/job/DEMO-ParameterUsage/job/initData/lastSuccessfulBuild/artifact/newparams.txt/*view*/`
  * To expose credentials in the URL like `https://SECRET@github.com` is NOT secure!!!

```groovy
  def CREDENTIAL_ID = "jenkins-token"
  def SECRET = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance()
    .getStore()
    .getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global())
    .find { it.getId().equals(CREDENTIAL_ID) }
    .getSecret().getPlainText()
  def url = "https://${SECRET}@your.controller.com/job/example/job/init/lastSuccessfulBuild/artifact/newparams.txt/*view*/"
  def result = ["/bin/bash", "-c", "curl -L " + url].execute().text.tokenize()
  return result
```

> ⚠️ Avoid using GitHub raw links as input source. GitHub caches raw content for up to 5 minutes, which is incompatible with dynamic parameter refresh.

---


## ⛔ Deprecated Plugin Warning

### Extended Choice Parameter Plugin

- ❌ [DO NOT USE](https://plugins.jenkins.io/extended-choice-parameter/)
- It's EOL, unmaintained, and contains known security issues.

**Use these alternatives instead:**

- ✔️ [Active Choice Plugin](https://plugins.jenkins.io/uno-choice/)
- ✔️ [JSON Editor Parameter Plugin](https://plugins.jenkins.io/json-editor-parameter/)

---

## 🔬 Useful Groovy Snippets

## Credentials

One liner to get a private key credential:
See [ssh credentials implementations](nkins.io/plugin/ssh-credentials/com/cloudbees/jenkins/plugins/sshcredentials/package-summary.html) for methods to extract values

```
def PRIVATE_KEY = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getStore().getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global()).find { it.getId().equals(CREDENTIAL_ID) }.getPrivateKey()
```

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

## Json Slurper/SnakeYaml

```
  def yamlText = """
  project:
    name: hello-world
    version: 1.0
  """    
  def yaml = new org.yaml.snakeyaml.Yaml()
  println yaml.load(yamlText)
  
  def jsonText = '{"app": {"name": "test-app", "env": "prod"}}'
  def jsonSlurper = new groovy.json.JsonSlurper()
  println jsonSlurper.parseText(jsonText)
```


To use **`JsonSlurper`** and **`SnakeYAML`** in Jenkins pipelines, the requirements differ slightly:

---

### ✅ **1. JsonSlurper**

**✅ No plugin required**
`JsonSlurper` is a **built-in Groovy class**, so it's available in Jenkins Pipeline out of the box.

You can use it in both `script {}` and `@NonCPS` contexts like this:

```groovy
def json = new groovy.json.JsonSlurper().parseText('{"key":"value"}')
```

---

### ✅ **2. SnakeYAML**

**🔧 Required Plugin:**

* **[Pipeline: Groovy (workflow-cps)](https://plugins.jenkins.io/workflow-cps/)** (for using `@NonCPS`)
* **[SnakeYAML API Plugin](https://plugins.jenkins.io/snakeyaml-api/)** (provides the `org.yaml.snakeyaml.Yaml` class)

🟡 Optional but commonly included:

* **Pipeline Utility Steps Plugin** – sometimes used for simplified YAML loading (e.g. `readYaml()`), though it uses its own YAML parser internally.

**Usage example:**

```groovy
@NonCPS
def parseYaml(text) {
    new org.yaml.snakeyaml.Yaml().load(text)
}
```

---

### 🛠️ Additional Recommendations

* Make sure `scriptApproval.xml` allows the use of these classes or mark them as safe if required.
* If you're using `sandbox=true`, Jenkins may block some reflective calls unless approved manually.


