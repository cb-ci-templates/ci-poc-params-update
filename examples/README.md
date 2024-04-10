# Examples

* Create a Pipeline with `Jenkinsfile-initparamsArchiveArtifact.groovy` and exute it
* Create a file credential with id `jenkins-token`
  * format for the secret: `user:token`
    * create it on Folder level to test Example1
    * create it on System level to test Example2
* Jenkinsfile-initparamsArchiveArtifact.groovy
  * This Job creates parameters in a file `newparams.txt`
  * Each param value is a new line
  * The param file is archived using the `archiveArtifact`step
  * It can be referenced by https://user:token@<CONTROLLER_URL>/<YOUR_PATH_TO_INIT_JOB>/lastSuccessfulBuild/artifact/newparams.txt/*view*/"
* `Example1-Jenkinsfile-ActiveChoice-FoldlerCredentials.groovy`
  * This example references the `newparams.txt` file created by the init job `Jenkinsfile-initparamsArchiveArtifact.groovy`
  * Credentials will be retrieved from Folder credentials
  * You need to add a `jenkins-token` file credential (user:token) on as FOLDER credentials
  * It uses the ActiveChoice Groovy script Hook to read parameters values from  https://<CONTROLLER_URL>/<YOUR_PATH_TO_INIT_JOB>/lastSuccessfulBuild/artifact/newparams.txt/*view*/"
* `Example2-Jenkinsfile-ActiveChoice-readParamsFromOtherArchiveArtifact.groovy`
  * Same as Example1, but reads `jenkins-token` file credential from Jenkins Global system credentials store (Manage Jenkins-> credentials)
* in both Examples, you MUST adjust your folder path to the init job, f.e. 
  * sb/job/ci-templates-demo/job/DEMO-ParameterUsage/job/initData/lastSuccessfulBuild/artifact/newparams.txt/*view*/
  * <YOUR_PATH_TO_INIT_JOB>/lastSuccessfulBuild/artifact/newparams.txt/*view*/
  * Keep in mind that the two examples require script approvals under manage jenkins -> script approvals

