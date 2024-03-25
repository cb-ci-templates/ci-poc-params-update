library identifier: 'ci-shared-library@main', retriever: modernSCM(
        [$class: 'GitSCMSource',
         remote: 'https://github.com/cb-ci-templates/ci-shared-library.git'])

import hudson.model.Job
import hudson.model.ParametersDefinitionProperty
import hudson.model.ChoiceParameterDefinition
import jenkins.model.Jenkins


/**
 * Add a parameter. Override if it does already exist.
 */
def updateParams(String jobName,String paramName,String choices) {
    def parameterDescription = 'Updated by Groovy'
    //String[] choices = ["Choice1","Choice2","Choice3"] // List of choices
    String[] tmpChoices=choices.split(',')
    //Retrieve the Job by name
    Job job = Jenkins.instance.getAllItems(Job.class).find { job -> jobName == job.name }
    job.setDescription("Updated by Groovy")
    //Retrieve the ParametersDefinitionProperty that contains the list of parameters.
    println job.getProperty(ParametersDefinitionProperty.class)
    ParametersDefinitionProperty parametersDefinitionProperty = job.getProperty(ParametersDefinitionProperty.class)
    println "Here"
    if (parametersDefinitionProperty != null) {
        //Retrieve the ParameterDefinition by name
        def parameterDefinition = parametersDefinitionProperty.getParameterDefinition(paramName)
        //If the parameter exists, remove it
        if (parameterDefinition) {
            println("--- Parameter ${paramName} already exists, removing it ---")
            parametersDefinitionProperty.getParameterDefinitions().remove(parameterDefinition)
        }
        println("--- Add Parameter(key=${jobName}, defaultValue=${paramName})  ---")
        // Update the choices
        ChoiceParameterDefinition choiceParameter = new ChoiceParameterDefinition(paramName, tmpChoices, parameterDescription)
        parametersDefinitionProperty.getParameterDefinitions().add(choiceParameter)
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
        GH_API_REPO_BRANCH = "https://api.github.com/repos/cb-ci-templates/ci-poc-params-update/branches"
        //GIT_REMOTE_BRANCHES = "[one, two, three]" // For testing purpose
    }
    stages {
        stage('UpdateParams') {
            steps {
                container("shell") {
                    //This shared library method set the git branches to environment variable $GIT_REMOTE_BRANCHES
                    getGitBranches("$GH_ACCESS_TOKEN", "$GH_API_REPO_BRANCH")
                    updateParams("example-pipeline","OPTION","${env.GIT_REPO_BRANCHES}")
                }
            }
        }
    }
}

