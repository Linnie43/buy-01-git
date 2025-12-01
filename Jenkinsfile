pipeline {
    agent any
    parameters {
        string(name: 'BRANCH', defaultValue: 'maris', description: 'Branch to build')
    }

    tools {
        maven 'maven'
        nodejs 'node'
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

       stage('Build Docker Images') {
            steps {
                echo "Building Docker images"
                sh 'docker compose -f docker-compose.dev.yml build'
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
