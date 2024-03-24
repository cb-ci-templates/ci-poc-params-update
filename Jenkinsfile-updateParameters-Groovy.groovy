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
    def parameterDescription = 'Your parameter description'
    def choices = ['Choice1', 'Choice2', 'Choice3'] // List of choices
    def choiceParameter = new ChoiceParameterDefinition(paramName, choices.join('\n'), parameterDescription)

    //Retrieve the Job by name
    Job job = Jenkins.instance.getAllItems(Job.class).find { job -> jobName == job.name }
    //Retrieve the ParametersDefinitionProperty that contains the list of parameters.
    ParametersDefinitionProperty jobProp = job.getProperty(ParametersDefinitionProperty.class)
    if (jobProp != null) {
        //Retrieve the ParameterDefinition by name
        ParameterDefinition parameterDefinition = jobProp.getParameterDefinition(paramName)
        //If the parameter exists, remove it
        if (parameterDefinition) {
            println("--- Parameter ${paramName} already exists, removing it ---")
            jobProp.getParameterDefinitions().remove(parameterDefinition)
        }
        //Add the parameter (here a StringParameter)
        println("--- Add Parameter(key=${jobName}, defaultValue=${paramName})  ---")
        // Update the choices
        jobProp.getParameterDefinitions().add(new ParametersDefinition(choiceParameter))
        //jobProp.getParameterDefinitions().add(choiceParameter)
        //Save the job
        job.save()
    }
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
        stage('UpdateParams') {
            steps {
                container("shell") {
                    updateParams("example-dsl-pipelinejob","OPTION")
                }
            }
        }
    }
}

