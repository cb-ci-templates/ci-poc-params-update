properties([parameters([extendedChoice(name: 'someName',
        description: '',
        visibleItemCount: 50,
        multiSelectDelimiter: ',',
        type: 'PT_SINGLE_SELECT',
        groovyScript: '''
                import groovy.json.JsonSlurper
                try{
                    print "init values"
                    List<String> myvalues = new ArrayList<String>()        
                    def pkgObject = ["curl", "https://raw.githubusercontent.com/cb-ci-templates/ci-shared-library/main/resources/json/keyvalue.json"].execute().text
                    def jsonSlurper = new JsonSlurper()
                    def artifactsJsonObject = jsonSlurper.parseText(pkgObject)
                    def dataA = artifactsJsonObject.items
                    print "init values $dataA"
                    for (i in dataA) {
                        myvalues.add(i.version)
                    }
                    return myvalues
               } catch (Exception e) {
                    print "There was a problem fetching the artifacts"
               }          
            ''')])])
pipeline {
    agent any

    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
            }
        }
    }
}
