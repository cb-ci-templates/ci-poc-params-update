#! /bin/bash


GH_ACCESS_TOKEN=${1:-YOUR_GITHUB_TOKEN}
REPO_BRANCH=${2:-"https://api.github.com/repos/org-caternberg/dsl-params-update/branches"}
env | sort
echo $PATH
RESULT=$(curl -L \
 -H "Accept: application/vnd.github+json" \
 -H "Authorization: Bearer ${GH_ACCESS_TOKEN}" \
 -H "X-GitHub-Api-Version: 2022-11-28" \
 $REPO_BRANCH)
#echo $RESULT |jq -r '.[] | .name'
echo $RESULT  |jq -r '.[] | .name' | tr '\n' ', ' | sed 's/,$//'