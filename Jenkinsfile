#!/usr/bin/env groovy

pipeline {
	agent any
    tools {
        maven 'default'
    }
	environment {
		LDAP_URL="ldapgw.test.local"
	}
	stages {
		stage('build') {
			steps {
				sh 'mvn -B -DskipTests clean package'
			}
		}
		stage('test') {
			steps {
				sh 'mvn test'
				junit 'trekk-app/target/surefire-reports/*.xml'
			}
		}
		stage("sonar") {
			steps {
				script {
					withSonarQubeEnv('Default') {
						def jacocoVersion = "0.8.1"
						def sonarVersion = "3.4.1.1168"
						sh "mvn org.jacoco:jacoco-maven-plugin:${jacocoVersion}:prepare-agent" +
								" clean verify" +
								" org.sonarsource.scanner.maven:sonar-maven-plugin:${sonarVersion}:sonar" +
								" -Djacoco.destFile='${env.WORKSPACE}/target/jacoco.exec'" +
								" -Dsonar.host.url=${env.SONAR_HOST_URL}" +
								" -Dsonar.login=${env.SONAR_AUTH_TOKEN}" +
								" -Dsonar.jacoco.reportPath='${env.WORKSPACE}/target/jacoco.exec'" +
								" -Dsonar.java.source=1.8" +
								" -Dsonar.branch=${BRANCH}" +
								" -Dsonar.coverage.exclusions=**/*Config.java,**/*Exception.java,**/*Entity.java,**/*Alias.java"
					}
				}
			}
		}
		stage('deploy docker image') {
			steps {
				script {
					checkout scm
					docker.withRegistry('https://docker.adeo.no:5000/') {
						def image = docker.build("okonomi/trekk:1.1.${env.BUILD_ID}-T")
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
						sh "nais upload --app trekk -v 1.1.${env.BUILD_ID}-T"
					}
				}
			}
		}
		stage('deploy to nais') {
			steps {
				script {
   					withCredentials([[$class: "UsernamePasswordMultiBinding", credentialsId: 'nais-user', usernameVariable: "NAIS_USERNAME", passwordVariable: "NAIS_PASSWORD"]]) {
			            def postBody = [
			                    application: "trekk",
			                    fasitEnvironment: "tx",
			                    version    : "1.1.${env.BUILD_ID}-T",
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
	}
	post {
        always {
            junit 'trekk-app/target/surefire-reports/*.xml'
			archive 'trekk-app/target/*.jar'
			deleteDir()
            script {
                // clean up Docker builds
                sh "docker system prune -af"
            }
        }

    }
}