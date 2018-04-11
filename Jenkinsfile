#!/usr/bin/env groovy
def mvnHome, mvn, scannerHome // tools

pipeline {
	agent any
	tools {
		maven 'default'
	}
	environment {
		LDAP_URL="ldapgw.test.local"
	}
	stages {
        stage('initialize') {
            mvnHome = tool 'Maven'
            mvn = "${mvnHome}/bin/mvn"
            scannerHome = tool 'SonarQube'
        }
		stage('build') {
			steps {
				sh '${mvn} -B -DskipTests clean package'
			}
		}
		stage('test') {
			steps {
				sh '${mvn} test'
				junit 'trekk-app/target/surefire-reports/*.xml'
			}
		}
		stage('deploy docker image') {
			steps {
				script {
					checkout scm
					docker.withRegistry('https://docker.adeo.no:5000/') {
						def image = docker.build("okonomi/trekk:1.1.${env.BUILD_ID}")
						image.push()
						image.push 'latest'
					}
				}
			}
		}
		stage('deploy nais.yaml to nexus m2internal') {
			steps {
				script {
					withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus-user', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD']]) {
						sh "nais validate"
						sh "nais upload --app trekk -v 1.1.${env.BUILD_ID}"
					}
				}
			}
		}
		stage('deploy to nais') {
			steps {
				script {
   					withCredentials([[$class: "UsernamePasswordMultiBinding", credentialsId: 'nais-user2', usernameVariable: "NAIS_USERNAME", passwordVariable: "NAIS_PASSWORD"]]) {
			            def postBody = [
			                    application: "trekk",
			                    fasitEnvironment: "t1",
			                    version    : "1.1.${env.BUILD_ID}",
			                    fasitUsername   : "${env.NAIS_USERNAME}",
			                    fasitPassword   : "${env.NAIS_PASSWORD}",
			                    zone       : "fss",
			                    namespace  : "default"
			            ]
			            def naisdPayload = groovy.json.JsonOutput.toJson(postBody)

			            echo naisdPayload

			            def response = httpRequest([
			                    url                   : "https://daemon.nais.preprod.local/deploy",
			                    consoleLogResponseBody: true,
			                    contentType           : "APPLICATION_JSON",
			                    httpMode              : "POST",
			                    requestBody           : naisdPayload,
			                    ignoreSslErrors       : true
			            ])

			            echo "$response.status: $response.content"

			            if (response.status != 200) {
			                currentBuild.description = "Failed - $response.content"
			                currentBuild.result = "FAILED"
			            }
			        }
				}
			}
		}
        stage('sonar') {
            sh "echo 'sonar.branch=${BRANCH_NAME}' >> sonar-project.properties"
            withSonarQubeEnv('Default') {
                sh "${scannerHome}/bin/sonar-scanner"
            }
        }
        stage('clean up') {
            sh "${mvn} clean"
        }
	}
	post {
        always {
			archive 'trekk-app/target/*.jar'
			deleteDir()
        }

    }
}