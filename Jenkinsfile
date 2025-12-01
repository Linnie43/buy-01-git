pipeline {
    agent {
        docker {
            image 'infra-jenkins:latest'
            args '-u root -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    parameters {
        string(name: 'BRANCH', defaultValue: 'maris', description: 'Branch to build')
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
                sh 'ls backend || true'
                sh 'mvn -f backend/pom.xml clean package -DskipTests'
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
