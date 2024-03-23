#! /bin/bash



echo "USage: $0: JENKINS_TOKEN myjobname"

export CONTROLLER_URL=${JENKINS_URL}
export JENKINS_TOKEN=${1:-"user:token"}
export PARAM_CHOICE_VALUES=${2:-'["value1", "value2"]'}

function updateJob(){
  echo "------------------  CREATE/UPDATE JOBS $1 | $JENKINS_TOKEN------------------"
  echo "$1"
  curl -L -XPOST \
     --user $JENKINS_TOKEN \
     "${CONTROLLER_URL}/casc-items/create-items" \
      -H "Content-Type:text/yaml" \
     --data-binary @$1
}

#yq  '.items[0].properties[0].parameters.parameterDefinitions[0].choice.choices = ["dev","test"]' tmp-casc-pipelinejob.yaml
yq -i ".items[0].properties[0].parameters.parameterDefinitions[0].choice.choices = ${PARAM_CHOICE_VALUES}" ./casc-pipelinejob.yaml
yq -i ".items[0].parameters[0].choice.choices = ${PARAM_CHOICE_VALUES}" ./casc-freestylejob.yaml
cat ./casc-freestylejob.yaml
cat ./casc-pipelinejob.yaml
updateJob ./casc-freestylejob.yaml
updateJob ./casc-pipelinejob.yaml


