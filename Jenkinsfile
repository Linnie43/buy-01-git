pipeline {
    agent any

    environment {
            SLACK_WEBHOOK = credentials('slack-webhook')
        }

    parameters {
        string(name: 'BRANCH', defaultValue: 'maris', description: 'Branch to build')
    }

    tools {
        maven 'maven'
        nodejs 'NodeJS-20'
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Checking out branch: ${params.BRANCH}"
                git branch: "${params.BRANCH}",
                    url: 'https://github.com/Linnie43/buy-01-git'
            }
        }

        stage('Build Backend') {
            steps {
                echo "Building backend microservices"
                dir('backend') {
                    sh 'mvn clean package -DskipTests -Dspring-boot.repackage.skip=true'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                echo "Building frontend application"
                dir('frontend') {
                sh 'npm install'
                sh 'npm run build'
                withEnv(["CHROME_BIN=/usr/bin/chromium"]) {
                sh 'npm test -- --watch=false --browsers=ChromeHeadless'
                }
             }
          }
       }

       stage('Test User Service') {
           steps {
               echo "Running user service"
               dir('backend/user-service') {
                   sh 'mvn test'
               }
           }
       }

       stage('Test Product Service') {
            steps {
                echo "Running product service tests"
                dir('backend/product-service') {
                    sh 'mvn test'
                }
            }
       }

       stage('Test Media Service') {
            steps {
                echo "Running media service tests"
                dir('backend/media-service') {
                    sh 'mvn test'
                }
            }
       }

        stage('Deploy') {
                   steps {
                        dir("$WORKSPACE") {
                            script {
                                   try {
                                       sh 'docker compose -f docker-compose.dev.yml down'
                                       sh 'docker compose -f docker-compose.dev.yml up -d --build'
                                   } catch (Exception e) {
                                       echo "Deployment failed — keeping previous version"
                                       sh 'docker compose -f docker-compose.dev.yml up -d'
                                   }
                            }
                        }
                   }
        }

    }

    post {
        always {
            script {
            if (env.WORKSPACE) {
                cleanWs notFailBuild: true //clean the workspace after build
            } else {
                echo "No workspace available; skipping cleanWs"
            }
          }
        }

        success {
            script {
                sh """curl -X POST -H 'Content-type: application/json' --data '{"text": "Build SUCCESS ✅"}' ${env.SLACK_WEBHOOK}"""
            }
        }
        failure {
            script {
                sh """curl -X POST -H 'Content-type: application/json' --data '{"text": "Build FAILED ❌"}' ${env.SLACK_WEBHOOK}"""
            }
        }

    }

}
