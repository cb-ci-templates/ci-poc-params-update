#! /bin/bash



echo "USage: $0: testcontroller myjobname"

export CONTROLLER_NAME=${1:-$CONTROLLER_NAME}
export CONTROLLER_URL=${BASE_URL}"/"${CONTROLLER_NAME}
export JOB_NAME=${2:-$JOB_NAME}
export JENKINS_TOKEN=${3:-"user:token"}
export NEW_VALUE_STRING=${4:-'["value1", "value2"]'}
export RESOURCES_DIR=resources

#items:
#  - kind: pipeline
#    name: ${JOB_NAME}
#    concurrentBuild: true
#    properties:
#      - parameters:
#          parameterDefinitions:
#            - choice:
#                name: OPTION
#                choices:
#                  - dev
#                  - main
#    resumeBlocked: false


#yq  '.items[0].properties[0].parameters.parameterDefinitions[0].choice.choices = ["dev","test"]' tmp-casc-pipelinejob.yaml
yq  '.items[0].properties[0].parameters.parameterDefinitions[0].choice.choices = ["dev","test"]' ./casc-pipelinejob.yaml
yq  '.items[0].parameters[0].choice.choices = ["dev","test"]' ./casc-freestylejob.yaml

echo "------------------  CREATE/UPDATE JOBS------------------"
#curl -v -XPOST \
#   --user $TOKEN \
#   "${CONTROLLER_URL}/casc-items/create-items" \
#    -H "Content-Type:text/yaml" \
#   --data-binary @$RESOURCES_DIR/casc-freestylejob.yaml
