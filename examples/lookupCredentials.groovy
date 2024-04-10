import jenkins.model.*
import com.cloudbees.hudson.plugins.folder.*
import com.cloudbees.plugins.credentials.*
def folderName = 'ci-templates-demo'
def credentialsID = "jenkins-token"
AbstractFolder myFolder = Jenkins.instance.getAllItems(AbstractFolder.class).find{ (it.name == folderName) }
def creds = CredentialsProvider.lookupCredentials(Credentials.class, myFolder)
def cred=creds.find{(it.id == credentialsID)}
def url = "https://"+ cred.getSecret().getPlainText() + "@sda.acaternberg.flow-training.beescloud.com/sb/job/ci-templates-demo/job/DEMO-ParameterUsage/job/initData/lastSuccessfulBuild/artifact/newparams.txt/*view*/"
def result = ["/bin/bash", "-c", "curl -L " + url].execute().text.tokenize();
return result