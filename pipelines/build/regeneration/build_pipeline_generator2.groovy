import java.nio.file.NoSuchFileException
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

/* 
file used as jenkinsfile to generator nightly and weekly pipeline
*/

node('worker') {
        // Pull in Adopt defaults
        //String ADOPT_DEFAULTS_FILE_URL = 'https://raw.githubusercontent.com/adoptium/ci-jenkins-pipelines/master/pipelines/defaults.json'
        String ADOPT_DEFAULTS_FILE_URL = 'http://sysdev.loongson.cn/attachments/download/94894/defaults.json'
/*
        def getAdopt = null
        URL getAdoptUrl = getAdoptUrl = new URL(ADOPT_DEFAULTS_FILE_URL)
        try {
        getAdopt = getAdoptUrl.openConnection()
        //println getAdopt
         } catch (IOException e) {
        e.printStackTrace();
        }
*/
       def TEST_CONF = params.TEST_CONF ? params.TEST_CONF : ""
       println TEST_CONF
       //Map<String, ?> ADOPT_DEFAULTS_JSON = new JsonSlurper().parseText(TEST_CONF) as Map
       def ADOPT_DEFAULTS_JSON = new JsonSlurper().parseText(TEST_CONF) as Map
        // Always clean up, even on failure (doesn't delete the created jobs)
        println '[INFO] Cleaning up...'
        //cleanWs deleteDirs: true
}
