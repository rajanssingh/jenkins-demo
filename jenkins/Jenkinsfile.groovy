
 pipeline {
     agent any

     environment {
         GH_TEST_TOKEN = credentials('gh-test-token')
     }

     parameters {
         // Select project type
         choice(
                 name: 'PROJECT_TYPE',
                 choices: ['P1', 'P2','P3', 'P4'],
                 description: 'Select the CASL project to deploy'
         )
         // TODO List of servers to update dynamically
         // Active choice parameter for dynamically loading list of servers based on PROJECT_TYPE

         reactiveChoice(
             name: 'SERVERS',
             description: 'List of servers to deploy',
             choiceType: 'PT_CHECKBOX',
             filterable: true,
             referencedParameters: 'PROJECT_TYPE',
             script: [
                 $class: 'GroovyScript',
                 fallbackScript: [
                         classpath: [],
                         sandbox: true,
                         script: "return ['Log error (fallbackScript)']"
                 ],
                 script: [
                     classpath: [],
                     sandbox: true,
                     script: """\
                             switch (PROJECT_TYPE) {
                                 case 'P1':
                                     return ["s1","s2","s3"]
                                 case 'P2':
                                     return ["s4","s5","s6"]
                                 case 'P3':
                                     return ["s7","s8","s9"]
                                 case 'P4':
                                     return ["s10","s11","s12"]
                                 default :
                                     return []
                             }
                         """,
                 ]
             ])

         reactiveChoice(
             name: 'ARTIFACT_VERSION_BASED_ON_PROJECT',
             description: 'Artifact version to deploy',
             choiceType: 'PT_SINGLE_SELECT',
             filterable: true,
             referencedParameters: 'PROJECT_TYPE',
             script: [
                     $class: 'GroovyScript',
                     fallbackScript: [
                             classpath: [],
                             sandbox: true,
                             script: "return ['ERROR']"
                     ],
                     script: [
                             classpath: [],
                             sandbox: true,
                             script: """\
                                
                                import com.cloudbees.plugins.credentials.CredentialsProvider
                                import com.cloudbees.plugins.credentials.common.StandardCredentials

                                def credentialId = 'gh-test-token' // Replace with your credential ID
                                def credentials = CredentialsProvider.lookupCredentials(
                                         StandardCredentials.class,
                                         Jenkins.instance,
                                         null,
                                         null
                                 )
                            
                                 def selectedCredential = credentials.find { it.id == credentialsId }
                                 
                                 return selectedCredential.getProperties()


                            """
                     ]
             ])
         string(
                 name: 'ARTIFACT_VERSION',
                 defaultValue: 'TODO - Dynamic drop down',
                 description: 'Enter the artifact version to deploy'
         )
         booleanParam(
                 name: 'IS_ARTIFACT_SNAPSHOT',
                 defaultValue: false,
                 description: 'Is the above artifact a snapshot version'
         )

         choice(
                 name: 'STAGES',
                 choices: ['dev', 'qa', 'stage', 'prod', 'dev,qa', 'dev,qa,stage', 'dev,qa,stage,prod'],
                 description: 'Select the stages for deployment'
         )
     }

     stages {
         stage('Clean Workspace') {
             steps {
                 cleanWs()
             }
         }

         stage('Debug Credentials Access') {
             steps {
                 script {
                     // Try accessing the token directly in a script block to confirm access
                     try {
                         def credentialsId = 'gh-test-token' // Replace with your credential ID
                         def creds = getCredentialsById(credentialsId)

                         // Using the credential values for a GitHub API call
                         def githubToken = creds
                         def props = creds.getProperties()
                         // def secret = creds.getSecret()
                         echo "Token - ${githubToken}\n"
                         echo "Properties - ${props}\n"
                     } catch (Exception e) {
                         echo "Exception occurred: ${e.message}"
                     }
                 }
             }
         }

         stage('Log the choices selected by the user') {
             steps {
                 script {

                     echo "Selected project : ${params.PROJECT_TYPE}"
                     echo "Selected artifact : ${params.ARTIFACT_VERSION}"
                     echo "Selected stages : ${params.STAGES}"
                     echo "Artifact ?? : ${params.IS_ARTIFACT_SNAPSHOT}"
                     env.ARTIFACT_VERSION_TO_DEPLOY = "${params.ARTIFACT_VERSION}${params.IS_ARTIFACT_SNAPSHOT ? "-SNAPSHOT" : ""}"
                     echo "Version to deploy - ${env.ARTIFACT_VERSION_TO_DEPLOY}"

                     echo "Selected seervers : ${params.SERVERS}"
                 }
             }
         }


     }
 }

 def getServer(String projectType){
 //    String projectType = params.PROJECT_TYPE
     switch (projectType) {
         case 'Output-Master':
             return ['cms-arc-casl-bet-output', 'cms-arc-casl-brochure-output', 'cms-arc-casl-dev-output', 'cms-arc-casl-ent-output', 'cms-arc-casl-entws-output', 'cms-arc-casl-intl-output', 'cms-arc-casl-kids-output', 'cms-arc-casl-loader-output', 'cms-arc-casl-music-output', 'cms-arc-casl-nick-i-output', 'cms-arc-casl-pluto-output', 'cms-arc-casl-style-i-output', 'cms-arc-casl-style-1-output', 'cms-arc-casl-style-2-output', 'cms-arc-casl-style-3-output', 'cms-arc-casl-style-4-output', 'cms-arc-casl-tools-output']
         case 'Solr-Worker':
             return ['cms-arc-casl-solr-low', 'cms-arc-casl-solr-medium', 'cms-arc-casl-solr-high', 'cms-arc-casl-solr-lowest']
         case 'Cv-Worker':
             return ['cms-arc-casl-cv-below-low', 'cms-arc-casl-cv-low', 'cms-arc-casl-cv-medium', 'cms-arc-casl-cv-high', 'cms-arc-casl-cv-lowest']
         case 'Osiris2':
             return ['cms-arc-casl-epg-feeds', 'cms-casl-posting-internal']
         default:
             return ['No Server available']
     }}

 import com.cloudbees.plugins.credentials.CredentialsProvider
 import com.cloudbees.plugins.credentials.common.StandardCredentials

 def getCredentialsById(String credentialsId) {

     def credentials = CredentialsProvider.lookupCredentials(
             StandardCredentials.class,
             Jenkins.instance,
             null,
             null
     )

     def selectedCredential = credentials.find { it.id == credentialsId }
     if (!selectedCredential) {
         error("Credential with ID '${credentialsId}' not found")
     }
     return selectedCredential}