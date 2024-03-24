#! /bin/bash



echo "Usage: $0: JENKINS_TOKEN CHOICE_VALUES"
echo "Call: $0 $@"
export CONTROLLER_URL=${JENKINS_URL}
export JENKINS_TOKEN=${1:-"user:token"}
export PARAM_CHOICE_VALUES=${2:-'["new1", "mew2"]'}

function updateJob(){
  echo "------------------  CREATE/UPDATE JOBS $1 | $JENKINS_TOKEN------------------"
  echo "$1"
  curl -Lv -XPOST \
     --user $JENKINS_TOKEN \
     "${CONTROLLER_URL}/casc-items/create-items" \
      -H "Content-Type:text/yaml" \
     --data-binary @$1
}

YQ=".items[0].properties[0].parameters.parameterDefinitions[0].choice.choices = $PARAM_CHOICE_VALUES"
echo $YQ
#yq 'eval(env(YQ))' ./casc-pipelinejob.yaml > ./updated-casc-pipelinejob.yaml
yq '.items[0].properties[0].parameters.parameterDefinitions[0].choice.choices = env(PARAM_CHOICE_VALUES)' ./casc-pipelinejob.yaml > ./updated-casc-pipelinejob.yaml
diff -u ./casc-pipelinejob.yaml ./updated-casc-pipelinejob.yaml
updateJob updated-casc-pipelinejob.yaml

#yq -i '.items[0].parameters[0].choice.choices = env(PARAM_CHOICE_VALUES)' ./casc-freestylejob.yaml > ./updated-casc-freestylejob.yaml
#cat ./casc-freestylejob.yaml
#updateJob ./casc-freestylejob.yaml



