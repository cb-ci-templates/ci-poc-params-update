library identifier: 'ci-shared-library@main', retriever: modernSCM(
        [$class: 'GitSCMSource',
         remote: 'https://github.com/cb-ci-templates/ci-shared-library.git'])

import hudson.model.Job
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import jenkins.model.Jenkins

/**
 * Add a parameter. Override if it does already exist.
 */
def updateParams(String jobName,String paramName) {
    def newChoices = ['New_Choice_1', 'New_Choice_2', 'New_Choice_3']
    // Get the job instance
    def job = Jenkins.instance.getItem(jobName)
    // Find the parameter definition
    def paramDefinition = job.getProperty(ParametersDefinitionProperty.class).getParameterDefinition(paramName)
    // Update the choices
    paramDefinition.setChoices(newChoices)
    // Save the updated job configuration
    job.save()
}

pipeline {
    agent {
        kubernetes {
            yaml '''
                apiVersion: v1
                kind: Pod
                spec:
                  containers:
                  - name: shell
                    image: caternberg/curl-yq-jq
                    command:
                      - cat
                    tty: true
                    workingDir: "/home/jenkins/agent"
                    securityContext:
                      runAsUser: 1000
                '''
            defaultContainer 'shell'
        }
    }
    environment {
        GH_ACCESS_TOKEN = credentials("github-token")
        GH_API_REPO_BRANCH = "https://api.github.com/repos/org-caternberg/dsl-params-update/branches"
        //GIT_REMOTE_BRANCHES = "[one, two, three]" // For testing purpose
    }
    stages {
        stage('SeedDSL') {
            steps {
                container("shell") {
                    updateParams("example-dsl-pipelinejob","OPTION")
                }
            }
        }
    }
}

