import java.nio.file.NoSuchFileException
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

/* 
file used as jenkinsfile to generator nightly and weekly pipeline
*/

node('worker') {
    try {
        // Pull in Adopt defaults
        //String ADOPT_DEFAULTS_FILE_URL = 'https://raw.githubusercontent.com/adoptium/ci-jenkins-pipelines/master/pipelines/defaults.json'
        String ADOPT_DEFAULTS_FILE_URL = 'http://sysdev.loongson.cn/attachments/download/94894/defaults.json'
        //def getAdopt = null
        URL getAdoptUrl = null 
        try {
        getAdoptUrl = new URL(ADOPT_DEFAULTS_FILE_URL)
        def getAdopt = getAdoptUrl.openConnection()
        //println getAdopt
         } catch (IOException e) {
        e.printStackTrace();
        }
/*
         Map<String, ?> ADOPT_DEFAULTS_JSON = [:]
        try {
        ADOPT_DEFAULTS_JSON = new JsonSlurper().parseText(getAdopt.getInputStream().getText()) as Map
         } catch (IOException e) {
        e.printStackTrace();
        }
        if (!ADOPT_DEFAULTS_JSON || !Map.isInstance(ADOPT_DEFAULTS_JSON)) {
            throw new Exception("[ERROR] No ADOPT_DEFAULTS_JSON found at ${ADOPT_DEFAULTS_FILE_URL} or it is not a valid JSON object. Please ensure this path is correct and leads to a JSON or Map object file. NOTE: Since this adopt's defaults and unlikely to change location, this is likely a network or GitHub issue.")
        }
*/
/*
        // Pull in User defaults
        String DEFAULTS_FILE_URL = (params.DEFAULTS_URL) ?: ADOPT_DEFAULTS_FILE_URL
        def getUser = new URL(DEFAULTS_FILE_URL).openConnection()
        Map<String, ?> DEFAULTS_JSON = new JsonSlurper().parseText(getUser.getInputStream().getText()) as Map
        if (!DEFAULTS_JSON || !Map.isInstance(DEFAULTS_JSON)) {
            throw new Exception("[ERROR] No DEFAULTS_JSON found at ${DEFAULTS_FILE_URL} or it is not a valid JSON object. Please ensure this path is correct and leads to a JSON or Map object file.")
        }
*/

        Map remoteConfigs = [:]
        def repoBranch = null
    /*
    Changes dir to Adopt's repo. Use closures as functions aren't accepted inside node blocks
    */
/*
        def checkoutAdoptPipelines = { ->
            checkout([$class: 'GitSCM',
                branches: [ [ name: ADOPT_DEFAULTS_JSON['repository']['pipeline_branch'] ] ],
                userRemoteConfigs: [ [ url: ADOPT_DEFAULTS_JSON['repository']['pipeline_url'] ] ]
            ])
        }
*/

    /*
    Changes dir to the user's repo. Use closures as functions aren't accepted inside node blocks
    */
            remoteConfigs = [ url: "https://github.com/adoptium/ci-jenkins-pipelines.git" ]
            repoBranch = "master"
        def checkoutUserPipelines = { ->
            checkout([$class: 'GitSCM',
                branches: [ [ name: repoBranch ] ],
                userRemoteConfigs: [ remoteConfigs ]
            ])
            //git branch: 'master', url: 'https://github.com/yaqsun/ci-jenkins-pipelines.git'
            //sh "git log -4"
        }

        timestamps {
            //def retiredVersions = [9, 10, 12, 13, 14, 15, 16, 18, 19, 20]
            //def generatedPipelines = []

            // Load git url and branch and gitBranch. These determine where we will be pulling user configs from.
            //def repoUri = (params.REPOSITORY_URL) ?: DEFAULTS_JSON['repository']['pipeline_url']
            //repoBranch = (params.REPOSITORY_BRANCH) ?: DEFAULTS_JSON['repository']['pipeline_branch']

            // Load credentials to be used in checking out. This is in case we are checking out a URL that is not Adopts and they don't have their ssh key on the machine.
            def checkoutCreds = (params.CHECKOUT_CREDENTIALS) ?: ''
            //remoteConfigs = [ url: repoUri ]
/*
            if (checkoutCreds != '') {
                // NOTE: This currently does not work with user credentials due to https://issues.jenkins.io/browse/JENKINS-60349
                remoteConfigs.put('credentials', "${checkoutCreds}")
            } else {
                println "[WARNING] CHECKOUT_CREDENTIALS not specified! Checkout to $repoUri may fail if you do not have your ssh key on this machine."
            }
*/
            // Checkout into user repository
            checkoutUserPipelines()
       }
    } finally {
        // Always clean up, even on failure (doesn't delete the created jobs)
        println '[INFO] Cleaning up...'
        //cleanWs deleteDirs: true
    }
}
