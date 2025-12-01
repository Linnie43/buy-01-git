pipeline {
   agent {
           docker {
               image 'infra-jenkins:latest'
               args '-u root -v /var/run/docker.sock:/var/run/docker.sock'
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
        //PREVIOUS_TAG        = "previous_build"
        //CURRENT_TAG         = "latest_build"
        NOTIFY_EMAIL        = "team@example.com"
        //GIT_CREDENTIALS_ID  = "github-creds"     // Create this in Jenkins (Username + PAT) or use SSH key credential id
        SLACK_CHANNEL       = "#ci"              // optional: configure slack in Jenkins global settings / plugin
    }

    /*options {
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }*/

    /*triggers {
        pollSCM('@midnight')
        githubPush()
    }*/

    stages {

        stage('Check Tools') {
                    steps {
                        sh 'docker --version'
                        sh 'docker ps'
                        sh 'mvn -v'
                        sh 'node -v'
                    }
                }

        stage('Checkout') {
            steps {
                echo "Checking out branch: ${params.BRANCH}"
                git branch: "${params.BRANCH}",
                    url: 'https://github.com/Linnie43/buy-01-git',
                    //credentialsId: env.GIT_CREDENTIALS_ID
            }
        }

        stage('Build Backend') {
            steps {
                echo "Building backend microservices"
                sh '''
                     mvn -f backend/pom.xml clean package -DskipTests
                  '''
            }
        }

        stage('Run Backend Tests') {
            steps {
                echo "Running backend JUnit tests"
                sh '''
                     mvn -f backend/pom.xml test
                '''
            }
            post {
                always {
                      junit 'backend/**/target/surefire-reports/*.xml'
                    //junit allowEmptyResults: true, testResults: 'backend/**/target/surefire-reports/*.xml'
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
                      npm run build
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
                      junit 'frontend/test-results/**/*.xml'
                    // If your Karma config writes JUnit xml, point to that path; set allowEmptyResults true to avoid pipeline error
                    //junit allowEmptyResults: true, testResults: 'frontend/test-results/**/*.xml'
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo "Archiving built artifacts"
                archiveArtifacts artifacts: 'backend/**/target/*.jar, frontend/dist/**', fingerprint: true, allowEmptyArchive: true
            }
        }

        stage('Deploy') {
            steps {
                echo "Deploying new version"
                sh """
                     docker compose -f ${DOCKER_COMPOSE} build --pull
                     docker compose -f ${DOCKER_COMPOSE} up -d
                """
            }
        }
    }

        post {
                success {
                    echo "Pipeline succeeded"
                }
                failure {
                    echo "Pipeline failed — consider rollback"
                }
                always {
                    echo "Cleaning workspace"
                    cleanWs notFailBuild: true
                }
            }
        }