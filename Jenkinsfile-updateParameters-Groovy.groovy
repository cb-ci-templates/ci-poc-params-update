library identifier: 'ci-shared-library@main', retriever: modernSCM(
        [$class: 'GitSCMSource',
         remote: 'https://github.com/cb-ci-templates/ci-shared-library.git'])

import hudson.model.Job
import hudson.model.ParametersDefinitionProperty
import hudson.model.ChoiceParameterDefinition
import jenkins.model.Jenkins

//see https://gist.github.com/jgraglia/44a7443847cff6f0d87387a46c7bb82f
def createParam(String name,String choice){
    //API https://javadoc.jenkins.io/plugin/extended-choice-parameter/com/cwctravel/hudson/plugins/extended_choice_parameter/ExtendedChoiceParameterDefinition.html
    com.cwctravel.hudson.plugins.extended_choice_parameter.ExtendedChoiceParameterDefinition nyparam = new com.cwctravel.hudson.plugins.extended_choice_parameter.ExtendedChoiceParameterDefinition(
            name,
            "PT_SINGLE_SELECT",
            choice,
            null,//project name
            null,
            null,
            null,
            null,// bindings
            null,
            null, // propertykey
            "VALUE, B", //default value
            null,
            null,
            null,
            null, //default bindings
            null,
            null,
            null, //descriptionPropertyValue
            null,
            null,
            null,
            null,
            null,
            null,
            null,// javascript file
            null, // javascript
            false, // save json param to file
            false, // quote
            2, // visible item count
            "DESC",
            ","
    )
    return nyparam
}



/**
 * Add a parameter. Override if it does already exist.
 */
def updateParams(String jobName,String paramName) {
    def parameterDescription = 'Your parameter description'
    def choices = "[Choice1,Choice2,Choice3]" // List of choices
    def choiceList = new ArrayList([choices]).asList(String) // List of choices

    //Retrieve the Job by name
    Job job = Jenkins.instance.getAllItems(Job.class).find { job -> jobName == job.name }
    //Retrieve the ParametersDefinitionProperty that contains the list of parameters.
    println job.getProperty(ParametersDefinitionProperty.class)
    ParametersDefinitionProperty parametersDefinitionProperty = job.getProperty(ParametersDefinitionProperty.class)
    println "Here"
    if (parametersDefinitionProperty != null) {
        //Retrieve the ParameterDefinition by name
        def parameterDefinition = parametersDefinitionProperty.getParameterDefinition(paramName)
        //println parameterDefinition.class
        //If the parameter exists, remove it
        if (parameterDefinition) {
            println("--- Parameter ${paramName} already exists, removing it ---")
            parametersDefinitionProperty.getParameterDefinitions().remove(parameterDefinition)
        }
        println("--- Add Parameter(key=${jobName}, defaultValue=${paramName})  ---")
        // Update the choices
        //ChoiceParameterDefinition choiceParameter = new ChoiceParameterDefinition(paramName, choices.join('\n'), parameterDescription)
        ChoiceParameterDefinition choiceParameter = new ChoiceParameterDefinition(paramName, choiceList, parameterDescription)
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

