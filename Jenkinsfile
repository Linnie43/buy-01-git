pipeline {
    agent any

    environment {
            SLACK_WEBHOOK = credentials('slack-webhook')
            VERSION = "v${env.BUILD_NUMBER}"
            STABLE_TAG = "stable"
            SONAR_SCANNER_HOME = tool 'SonarScanner'
        }

    parameters {
        string(name: 'BRANCH', defaultValue: 'main', description: 'Branch to build')
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
                dir('backend/discovery') {
                    sh 'mvn clean package -DskipTests -Dspring-boot.repackage.skip=true'
                }
                dir('backend/gateway') {
                    sh 'mvn clean package -DskipTests -Dspring-boot.repackage.skip=true'
                }
                dir('backend/user-service') {
                    sh 'mvn clean package -DskipTests -Dspring-boot.repackage.skip=true'
                }
                dir('backend/product-service') {
                    sh 'mvn clean package -DskipTests -Dspring-boot.repackage.skip=true'
                }
                dir('backend/media-service') {
                    sh 'mvn clean package -DskipTests -Dspring-boot.repackage.skip=true'
                }
                dir('backend/order-service') {
                    sh 'mvn clean package -DskipTests -Dspring-boot.repackage.skip=true'
            }
        }

        stage('Build Frontend') {
            steps {
                echo "Building frontend application"
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
       }

       stage('Test Frontend') {
            steps {
                echo "Running frontend tests"
                dir('frontend') {
                    sh 'npm install --save-dev karma-chrome-launcher karma-junit-reporter karma-coverage'
                     withEnv(["CHROMIUM_BIN=/usr/bin/chromium", "CHROME_BIN=/usr/bin/chromium"]) {
                        sh 'npm test -- --code-coverage'
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

       stage('Test Order Service') {
            steps {
                 echo "Running order service tests"
                 dir('backend/order-service') {
                      sh 'mvn test'
                 }
            }
         }

       stage('SonarQube Analysis') {
           steps {
               echo "Running SonarQube analysis"
                withSonarQubeEnv('SonarQube') {
                    sh """
                    ${SONAR_SCANNER_HOME}/bin/sonar-scanner -Dproject.settings=sonar-project.properties
                    """
                }
           }
       }

       stage('Quality Gate') {
           steps {
               echo "Checking SonarQube Quality Gate"
               timeout(time: 5, unit: 'MINUTES') {
                  waitForQualityGate abortPipeline: true
               }
           }
       }

       stage('Build Images') {
                   steps {
                       script {
                           echo "Building Docker images with tag: ${VERSION}"
                           // We build explicitly first to ensure the images exist with the specific tag
                           withEnv(["IMAGE_TAG=${VERSION}"]) {
                               sh 'docker compose -f docker-compose.yml build'
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

                                    // Deploy the NEW version
                                    // We do NOT run 'down' here to avoid downtime during the switch
                                    withEnv(["IMAGE_TAG=${VERSION}"]) {
                                        sh 'docker compose -f docker-compose.yml up -d'

                                        // Health Check / Verification
                                        echo "Waiting for services to stabilize..."
                                        sleep 15 // Give Spring Boot time to start up

                                        // This looks for any container in the stack that has "Exit" (crashed)
                                        sh """
                                            if docker compose -f docker-compose.yml ps | grep "Exit"; then
                                                echo "Detected crashed containers!"
                                                exit 1
                                            fi
                                        """
                                    }

                                    echo "Deployment Verification Passed!"

                                    // TAG the new version as 'stable' since deployment succeeded
                                    sh "docker tag frontend:${VERSION} frontend:${STABLE_TAG}"
                                    sh "docker tag user-service:${VERSION} user-service:${STABLE_TAG}"
                                    sh "docker tag product-service:${VERSION} product-service:${STABLE_TAG}"
                                    sh "docker tag media-service:${VERSION} media-service:${STABLE_TAG}"
                                    sh "docker tag order-service:${VERSION} order-service:${STABLE_TAG}"
                                    sh "docker tag gateway:${VERSION} gateway:${STABLE_TAG}"
                                    sh "docker tag discovery:${VERSION} discovery:${STABLE_TAG}"


                                } catch (Exception e) {
                                    echo "Deployment failed or crashed! Initiating Rollback..."

                                    // ROLLBACK
                                    // We redeploy using the 'stable' tag
                                    try {
                                        withEnv(["IMAGE_TAG=${STABLE_TAG}"]) {
                                            sh 'docker compose -f docker-compose.yml up -d'
                                        }
                                        echo "Rolled back to previous stable version."
                                        // Slack notification for successful rollback
                                        sh """
                                        curl -X POST -H 'Content-type: application/json' --data '{
                                            "text": ":information_source: Rollback SUCCESSFUL!\n*Job:* ${env.JOB_NAME}\n*Build:* ${env.BUILD_NUMBER}\n*Branch:* ${params.BRANCH}"
                                        }' ${env.SLACK_WEBHOOK}
                                        """
                                    } catch (Exception rollbackErr) {
                                        echo "FATAL: Rollback failed!"
                                            echo "Reason: ${rollbackErr.getMessage()}"
                                            sh """
                                            curl -X POST -H 'Content-type: application/json' --data '{
                                                "text": ":rotating_light: Rollback FAILED!\n*Reason:* ${rollbackErr.getMessage()}\n*Job:* ${env.JOB_NAME}\n*Build:* ${env.BUILD_NUMBER}\n*Branch:* ${params.BRANCH}\nManual intervention needed!"
                                            }' ${env.SLACK_WEBHOOK}
                                            """
                                    }

                                    // Fail the build
                                    error "Deployment Failed"
                                }
                            }
                        }
                    }
        }
    }

    post {
        always {
            script {
                // Backend test reports
                junit allowEmptyResults: true, testResults: 'backend/*/target/surefire-reports/*.xml'
                archiveArtifacts artifacts: 'backend/*/target/surefire-reports/*.xml', allowEmptyArchive: true

                // Frontend reports
                junit allowEmptyResults: true, testResults: 'frontend/test-results/junit/*.xml'
                archiveArtifacts artifacts: 'frontend/test-results/junit/*.xml', allowEmptyArchive: true

                if (env.WORKSPACE) {
                    cleanWs notFailBuild: true //clean the workspace after build
                } else {
                    echo "No workspace available; skipping cleanWs"
                }
            }
        }

        success {
            echo "Build succeeded!"
            sh """
            curl -X POST -H 'Content-type: application/json' --data '{
                "text": ":white_check_mark: Build SUCCESS\n*Job:* ${env.JOB_NAME}\n*Build:* ${env.BUILD_NUMBER}\n*Branch:* ${params.BRANCH}"
            }' ${env.SLACK_WEBHOOK}
            """
        }

        failure {
            echo "Build failed!"
            sh """
            curl -X POST -H 'Content-type: application/json' --data '{
                "text": ":x: Build FAILED\n*Job:* ${env.JOB_NAME}\n*Build:* ${env.BUILD_NUMBER}\n*Branch:* ${params.BRANCH}\n*Error:* ${currentBuild.currentResult}"
            }' ${env.SLACK_WEBHOOK}
            """
        }
    }
}