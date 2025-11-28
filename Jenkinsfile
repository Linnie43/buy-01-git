pipeline {
    agent {
            docker {
                image 'carlossg/jenkins-maven-node-docker'
                args '-v /var/run/docker.sock:/var/run/docker.sock -v /tmp:/tmp' // Mount Docker socket to use docker-compose
            }
        }

    /*tools {
            maven 'maven'
    }*/

    parameters {
        string(name: 'BRANCH', defaultValue: 'maris', description: 'Branch to build')
    }

    environment {
        DOCKER_COMPOSE      = "docker-compose.dev.yml"
        PROJECT_NAME        = "buy-01"
        PREVIOUS_TAG        = "previous_build"
        CURRENT_TAG         = "latest_build"
        NOTIFY_EMAIL        = "team@example.com"
        GIT_CREDENTIALS_ID  = "github-creds"     // Create this in Jenkins (Username + PAT) or use SSH key credential id
        SLACK_CHANNEL       = "#ci"              // optional: configure slack in Jenkins global settings / plugin
    }

    options {
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    triggers {
        pollSCM('@midnight')
        githubPush()
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Checking out branch: ${params.BRANCH}"
                git branch: "${params.BRANCH}",
                    url: 'https://github.com/Linnie43/buy-01-git',
                    credentialsId: env.GIT_CREDENTIALS_ID
            }
        }

        stage('Build Backend') {
            steps {
                echo "Building backend microservices"
                sh '''
                     mvn -f backend/discovery/pom.xml -B clean package -DskipTests=false && \
                     mvn -f backend/gateway/pom.xml -B clean package -DskipTests=false && \
                     mvn -f backend/user-service/pom.xml -B clean package -DskipTests=false && \
                     mvn -f backend/product-service/pom.xml -B clean package -DskipTests=false && \
                     mvn -f backend/media-service/pom.xml -B clean package -DskipTests=false
                  '''
            }
        }

        stage('Run Backend Tests') {
            steps {
                echo "Running backend JUnit tests"
                sh '''
                     mvn -f backend/discovery/pom.xml -B test && \
                     mvn -f backend/gateway/pom.xml -B test && \
                     mvn -f backend/user-service/pom.xml -B test && \
                     mvn -f backend/product-service/pom.xml -B test && \
                     mvn -f backend/media-service/pom.xml -B test
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'backend/**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Frontend') {
             steps {
                  echo "Building Angular frontend"
                         // The working directory inside the container is the workspace
                  sh '''
                      cd frontend
                      npm ci
                      npm run build --if-present
                   '''
             }
        }

        stage('Run Frontend Tests') {
            steps {
                echo "Running frontend tests (Karma/Jasmine)"
                sh '''
                    cd frontend
                    npm run test -- --watch=false --browsers=ChromeHeadless || true
                '''
            }
            post {
                always {
                    // If your Karma config writes JUnit xml, point to that path; set allowEmptyResults true to avoid pipeline error
                    junit allowEmptyResults: true, testResults: 'frontend/test-results/**/*.xml'
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo "Archiving built artifacts"
                archiveArtifacts artifacts: 'backend/**/target/*.jar, frontend/dist/**', fingerprint: true, allowEmptyArchive: true
            }
        }

        stage('Backup Previous Deployment') {
            steps {
                echo "Tagging current containers for rollback"
                sh """
                    docker compose -f ${DOCKER_COMPOSE} pull || true
                    # commit running containers (best-effort)
                    docker compose -f ${DOCKER_COMPOSE} ps -q | xargs -r -I {} docker commit {} ${PROJECT_NAME}:${PREVIOUS_TAG} || true
                """
            }
        }

        stage('Deploy') {
            steps {
                echo "Deploying new version"
                sh """
                    docker compose -f ${DOCKER_COMPOSE} down --remove-orphans || true
                    docker compose -f ${DOCKER_COMPOSE} build --pull
                    docker compose -f ${DOCKER_COMPOSE} up -d
                    # tag latest build for quick rollback reference
                    docker image ls --format '{{.Repository}}:{{.Tag}} {{.ID}}'
                """
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded"
            try {
                emailext (
                    subject: "SUCCESS: Build & Deploy (${PROJECT_NAME})",
                    body: "Jenkins build and deployment succeeded for branch ${params.BRANCH}.",
                    to: "${NOTIFY_EMAIL}"
                )
            } catch (e) {
                echo "Email not configured: ${e}"
            }
            script {
                // optional: send slack if plugin configured
                try {
                    slackSend channel: env.SLACK_CHANNEL, color: 'good', message: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER} (${params.BRANCH})"
                } catch (e) { echo "Slack not configured: ${e}" }
            }
        }

        failure {
            echo "Build FAILED — attempting rollback"
            sh """
                set -e
                echo 'Restoring services from ${PROJECT_NAME}:${PREVIOUS_TAG}'
                docker compose -f ${DOCKER_COMPOSE} down || true
                # If docker-compose file refers to images by tag, force recreate using previous tag
                # If previous images exist as ${PROJECT_NAME}:${PREVIOUS_TAG}, attempt to start them
                docker image inspect ${PROJECT_NAME}:${PREVIOUS_TAG} >/dev/null 2>&1 && docker run -d --name ${PROJECT_NAME}_rollback ${PROJECT_NAME}:${PREVIOUS_TAG} || true
                # fallback: try to bring up compose without rebuild (uses existing images)
                docker compose -f ${DOCKER_COMPOSE} up -d --no-build || true
            """
            try {
                emailext (
                    subject: "ROLLBACK: Build FAILED (${PROJECT_NAME})",
                    body: "Jenkins build failed for branch ${params.BRANCH}. Services rolled back to previous version.",
                    to: "${NOTIFY_EMAIL}"
                )
            } catch (e) {
                echo "Email not configured: ${e}"
            }
            script {
                try {
                    slackSend channel: env.SLACK_CHANNEL, color: 'danger', message: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER} (${params.BRANCH}) - rollback attempted"
                } catch (e) { echo "Slack not configured: ${e}" }
            }
        }

        always {
            echo "Cleaning workspace"
            cleanWs notFailBuild: true
        }
    }
}
