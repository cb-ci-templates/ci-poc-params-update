#! /bin/bash


echo "Usage: $0: JENKINS_TOKEN CHOICE_VALUES"
echo "Example: : $0 youradminuser:yourjenkinstoken '[\"value1\",\"value2\"]'"
echo "Call: $0 $@"
export CONTROLLER_URL=${JENKINS_URL}
export JENKINS_TOKEN=${1:-"user:token"}
export PARAM_CHOICE_VALUES=${2:-'["new1", "mew2"]'}
#We got JOB_NAME from Jenkins, we must delete the last path entry to get the Folder
export JOB_FOLDER=${3:-"$(echo "$JOB_NAME" | sed 's|/[^/]*$|/|')"}

function updateJob(){
  echo "------------------  CREATE/UPDATE Job:  yaml:$1 in folder $JOB_FOLDER using $JENKINS_TOKEN------------------"
  #see https://docs.cloudbees.com/docs/cloudbees-ci-api/latest/bundle-management-api
  curl -Ls -XPOST \
     --user $JENKINS_TOKEN \
     "${CONTROLLER_URL}/casc-items/create-items?path=$JOB_FOLDER" \
      -H "Content-Type:text/yaml" \
     --data-binary @$1
}

yq '.items[0].properties[0].parameters.parameterDefinitions[0].choice.choices = env(PARAM_CHOICE_VALUES)' ./casc-pipelinejob.yaml > ./updated-casc-pipelinejob.yaml
diff -u ./casc-pipelinejob.yaml ./updated-casc-pipelinejob.yaml
updateJob updated-casc-pipelinejob.yaml

#For Freestyle jobs just the yq path is a bit different
#yq -i '.items[0].parameters[0].choice.choices = env(PARAM_CHOICE_VALUES)' ./casc-freestylejob.yaml > ./updated-casc-freestylejob.yaml
#cat ./casc-freestylejob.yaml
#updateJob ./casc-freestylejob.yaml



