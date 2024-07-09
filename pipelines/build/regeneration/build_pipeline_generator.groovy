import java.nio.file.NoSuchFileException
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

/* 
file used as jenkinsfile to generator nightly and weekly pipeline
*/

node('worker') {
    try {
        // Pull in Adopt defaults
        String ADOPT_DEFAULTS_FILE_URL = 'https://raw.githubusercontent.com/adoptium/ci-jenkins-pipelines/master/pipelines/defaults.json'
        def getAdopt = new URL(ADOPT_DEFAULTS_FILE_URL).openConnection()
        Map<String, ?> ADOPT_DEFAULTS_JSON = new JsonSlurper().parseText(getAdopt.getInputStream().getText()) as Map
        if (!ADOPT_DEFAULTS_JSON || !Map.isInstance(ADOPT_DEFAULTS_JSON)) {
            throw new Exception("[ERROR] No ADOPT_DEFAULTS_JSON found at ${ADOPT_DEFAULTS_FILE_URL} or it is not a valid JSON object. Please ensure this path is correct and leads to a JSON or Map object file. NOTE: Since this adopt's defaults and unlikely to change location, this is likely a network or GitHub issue.")
        }

        // Pull in User defaults
        String DEFAULTS_FILE_URL = (params.DEFAULTS_URL) ?: ADOPT_DEFAULTS_FILE_URL
        def getUser = new URL(DEFAULTS_FILE_URL).openConnection()
        Map<String, ?> DEFAULTS_JSON = new JsonSlurper().parseText(getUser.getInputStream().getText()) as Map
        if (!DEFAULTS_JSON || !Map.isInstance(DEFAULTS_JSON)) {
            throw new Exception("[ERROR] No DEFAULTS_JSON found at ${DEFAULTS_FILE_URL} or it is not a valid JSON object. Please ensure this path is correct and leads to a JSON or Map object file.")
        }

        Map remoteConfigs = [:]
        def repoBranch = null

    /*
    Changes dir to Adopt's repo. Use closures as functions aren't accepted inside node blocks
    */
        def checkoutAdoptPipelines = { ->
            checkout([$class: 'GitSCM',
                branches: [ [ name: ADOPT_DEFAULTS_JSON['repository']['pipeline_branch'] ] ],
                userRemoteConfigs: [ [ url: ADOPT_DEFAULTS_JSON['repository']['pipeline_url'] ] ]
            ])
        }

    /*
    Changes dir to the user's repo. Use closures as functions aren't accepted inside node blocks
    */
        def checkoutUserPipelines = { ->
            checkout([$class: 'GitSCM',
                branches: [ [ name: repoBranch ] ],
                userRemoteConfigs: [ remoteConfigs ]
            ])
        }
         
        //sh "ls /home/jenkins-slave/jenkins-node/workspace/build-scripts/utils"
        //timestamps {
            def retiredVersions = [9, 10, 12, 13, 14, 15, 16, 18, 19, 20]
            def generatedPipelines = []

            // Load git url and branch and gitBranch. These determine where we will be pulling user configs from.
            def repoUri = (params.REPOSITORY_URL) ?: DEFAULTS_JSON['repository']['pipeline_url']
            repoBranch = (params.REPOSITORY_BRANCH) ?: DEFAULTS_JSON['repository']['pipeline_branch']

            // Load credentials to be used in checking out. This is in case we are checking out a URL that is not Adopts and they don't have their ssh key on the machine.
            def checkoutCreds = (params.CHECKOUT_CREDENTIALS) ?: ''
            remoteConfigs = [ url: repoUri ]
            //remoteConfigs = [ url: "ssh://sunyaqi@rd.loongson.cn:29418/vm-infra" ]
            //remoteConfigs = [ url: "https://github.com/adoptium/jenkins-helper.git" ]
            if (checkoutCreds != '') {
                // NOTE: This currently does not work with user credentials due to https://issues.jenkins.io/browse/JENKINS-60349
                remoteConfigs.put('credentials', "${checkoutCreds}")
            } else {
                println "[WARNING] CHECKOUT_CREDENTIALS not specified! Checkout to $repoUri may fail if you do not have your ssh key on this machine."
            }
            //remoteConfigs = [ url: 'https://github.com/yaqsun/ci-jenkins-pipelines.git']
            //repoBranch = "master"
            //dir("$WORKSPACE/ci-jenkins-pipelines") {
            //  deleteDir()
            //  git branch: 'master', url: 'https://github.com/yaqsun/ci-jenkins-pipelines.git'
            //  sh "git log -4"
            //}

            // Checkout into user repository
            try {
            checkoutUserPipelines()
            println "checkoutUserPipelines() ======= 11111"
            //def stash = checkout([$class: 'GitSCM',
            //    branches: [ [ name: repoBranch ] ],
            //    userRemoteConfigs: [ remoteConfigs ]
            //])
            //println "Checked out commit: ${stash.GIT_COMMIT}"
            } catch (Exception e1) {
               println "================= IOException e1"
               //println "Checked out commit: ${stash.GIT_COMMIT}"
               e1.printStackTrace();
               echo "检出代码时发生错误: ${e1.getMessage()}"
            println "checkoutUserPipelines() ======= successful11111eeeeeeeee"
            }
            println "checkoutUserPipelines() ======= successful"

            String helperRef = DEFAULTS_JSON['repository']['helper_ref']
            try {
            library(identifier: "openjdk-jenkins-helper@${helperRef}")
            } catch (Exception e2) {
            println "checkoutUserPipelines() ======= successful22222"
               echo "检出代码时发生错误: ${e2.getMessage()}"
               e2.printStackTrace();
            println "checkoutUserPipelines() ======= successful22222eeeeeeeee"
            }
            println "checkoutUserPipelines() ======= successful33333"

            // Load jobRoot. This is where the openjdkxx-pipeline jobs will be created.
            def jobRoot = (params.JOB_ROOT) ?: DEFAULTS_JSON['jenkinsDetails']['rootDirectory']

        /*
        Load scriptFolderPath. This is the folder where the openjdk_pipeline.groovy code is located compared to the repository root.
        These are the top level pipeline jobs.
        */
            def scriptFolderPath = (params.SCRIPT_FOLDER_PATH) ?: DEFAULTS_JSON['scriptDirectories']['upstream']
            println scriptFolderPath
            //println fileExists(scriptFolderPath)
            try {
            if (!fileExists(scriptFolderPath)) {
        println "*****************************"
                //println "[WARNING] ${scriptFolderPath} does not exist in your chosen repository. Updating it to use Adopt's instead"
                //checkoutAdoptPipelines()
                //scriptFolderPath = ADOPT_DEFAULTS_JSON['scriptDirectories']['upstream']
                //println "[SUCCESS] The path is now ${scriptFolderPath} relative to ${ADOPT_DEFAULTS_JSON['repository']['pipeline_url']}"
                //checkoutUserPipelines()
            }
            } catch (IOException e3) {
               echo "检出代码时发生错误: ${e3.getMessage()}"
            println "checkoutUserPipelines() ======= successful44444"
               e3.printStackTrace();
            }
        /*
        Load nightlyFolderPath. This is the folder where the configurations/jdkxx_pipeline_config.groovy code is located compared to the repository root.
        These define what the default set of nightlies will be.
        */
            def nightlyFolderPath = (params.NIGHTLY_FOLDER_PATH) ?: DEFAULTS_JSON['configDirectories']['nightly']
            println "nightlyFolderPath ===== ${nightlyFolderPath}"

            try {
            if (!fileExists(nightlyFolderPath)) {
                println "[WARNING] ${nightlyFolderPath} does not exist in your chosen repository. Updating it to use Adopt's instead"
                checkoutAdoptPipelines()
                nightlyFolderPath = ADOPT_DEFAULTS_JSON['configDirectories']['nightly']
                println "[SUCCESS] The path is now ${nightlyFolderPath} relative to ${ADOPT_DEFAULTS_JSON['repository']['pipeline_url']}"
                checkoutUserPipelines()
            }
            } catch (IOException e3) {
               echo "检出代码时发生错误: ${e3.getMessage()}"
            println "checkoutUserPipelines() ======= successful44444"
               e3.printStackTrace();
            }

            //}
    } finally {
        // Always clean up, even on failure (doesn't delete the created jobs)
        println '[INFO] Cleaning up...'
        sh "ls ${WORKSPACE}"
        sh "pwd"
        //cleanWs deleteDirs: true
    }
}
