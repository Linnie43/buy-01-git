pipeline {
    agent any
    parameters {
        string(name: 'BRANCH', defaultValue: 'maris', description: 'Branch to build')
    }

    tools {
        maven 'maven'
        nodejs 'NodeJS-20'
    }

    stages {
        stage('Check Tools') {
            steps {
                sh 'docker --version || true'
                sh 'docker ps || true'
                sh 'mvn -v || true'
                sh 'node -v || true'
            }
        }

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
                sh 'mvn -f backend/pom.xml clean package -DskipTests -Dspring-boot.repackage.skip=true'
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

       stage('Test User Service and Controller') {
           steps {
               echo "Running user service and controller tests"
               sh 'mvn -f backend/pom.xml test -Dtest=com.buy01.user..*Test'
           }
       }

       stage('Test Product Service') {
            steps {
                echo "Running product service tests"
                sh 'mvn -f backend/pom.xml test -Dtest=com.buy01.product..*Test'
            }
       }


       stage('Build Docker Images') {
            steps {
                //echo "Building Docker images"
                // sh 'docker compose -f docker-compose.dev.yml build'
                echo 'Skipping Docker image build because Dockerfiles are not present in this branch.'

              }
           }

    }

    post {
        always {
            script {
            if (env.WORKSPACE) {
                cleanWs notFailBuild: true
            } else {
                echo "No workspace available; skipping cleanWs"
            }
          }
       }
    }
}
