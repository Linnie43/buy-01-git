pipeline {
    agent any

    environment {
            SLACK_WEBHOOK = credentials('slack-webhook')
            VERSION = "v${env.BUILD_NUMBER}"
            STABLE_TAG = "stable"
        }

    parameters {
        string(name: 'BRANCH', defaultValue: 'deployment-test', description: 'Branch to build')
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
                sh 'npm install --save-dev karma-chrome-launcher'
                sh 'npm run build'
                withEnv(["CHROMIUM_BIN=/usr/bin/chromium", "CHROME_BIN=/usr/bin/chromium"]) {
                sh 'npm test'
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

       stage('Build Images') {
                   steps {
                       script {
                           echo "Building Docker images with tag: ${VERSION}"
                           // We build explicitly first to ensure the images exist with the specific tag
                           withEnv(["IMAGE_TAG=${VERSION}"]) {
                               sh 'docker compose -f docker-compose.dev.yml build'
                           }
                       }
                   }
               }

        stage('Deploy & Verify') {
                    steps {
                        dir("$WORKSPACE") {
                            script {
                                try {
                                    echo "Deploying version: ${VERSION}"

                                    // 1. Deploy the NEW version
                                    // We do NOT run 'down' here to avoid downtime during the switch
                                    withEnv(["IMAGE_TAG=${VERSION}"]) {
                                        sh 'docker compose -f docker-compose.dev.yml up -d'
                                    }

                                    // 2. Health Check / Verification
                                    echo "Waiting for services to stabilize..."
                                    sleep 15 // Give Spring Boot time to start up

                                    // Simple Check: Are the containers running?
                                    // This looks for any container in the stack that has "Exit" (crashed)
                                    sh """
                                        if docker compose -f docker-compose.dev.yml ps | grep "Exit"; then
                                            echo "Detected crashed containers!"
                                            exit 1
                                        fi
                                    """

                                    echo "Deployment Verification Passed!"

                                    // 3. Promote to Stable (The Magic Step)
                                    // Since it worked, we retag these specific images as 'stable'
                                    // This ensures next time we rollback, we go back to THIS state.
                                    sh "docker tag frontend:${VERSION} frontend:${STABLE_TAG}"
                                    sh "docker tag user-service:${VERSION} user-service:${STABLE_TAG}"
                                    sh "docker tag product-service:${VERSION} product-service:${STABLE_TAG}"
                                    sh "docker tag media-service:${VERSION} media-service:${STABLE_TAG}"
                                    sh "docker tag gateway:${VERSION} gateway:${STABLE_TAG}"
                                    sh "docker tag discovery:${VERSION} discovery:${STABLE_TAG}"


                                } catch (Exception e) {
                                    echo "Deployment failed or crashed! Initiating Rollback..."

                                    // 4. ROLLBACK
                                    // We redeploy using the 'stable' tag
                                    try {
                                        withEnv(["IMAGE_TAG=${STABLE_TAG}"]) {
                                            sh 'docker compose -f docker-compose.dev.yml up -d'
                                        }
                                        echo "Rolled back to previous stable version."
                                    } catch (Exception rollbackErr) {
                                        echo "FATAL: Rollback failed (maybe no stable version exists yet)."
                                    }

                                    // Fail the build so we get a notification
                                    error "Deployment Failed - Rolled back."
                                }
                            }
                        }
                    }
                }
            }

    post {
        always {
            cleanWs notFailBuild: true //clean the workspace after build

        }

        success {
            sh """curl -X POST -H 'Content-type: application/json' --data '{"text": "Build SUCCESS ✅"}' ${env.SLACK_WEBHOOK}"""

        }
        failure {
            sh """curl -X POST -H 'Content-type: application/json' --data '{"text": "Build FAILED ❌"}' ${env.SLACK_WEBHOOK}"""
        }

    }

}