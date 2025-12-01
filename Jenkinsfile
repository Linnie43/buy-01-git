pipeline {
    agent none

    parameters {
        string(name: 'BRANCH', defaultValue: 'maris', description: 'Branch to build')
    }

    stages {
        stage('Check Tools') {
        agent { docker { image 'infra-jenkins:latest' args '-u root -v /var/run/docker.sock:/var/run/docker.sock' } }
            steps {
                sh 'docker --version'
                sh 'docker ps'
                sh 'mvn -v'
                sh 'node -v'
            }
        }

        stage('Checkout') {
            agent any
            steps {
                echo "Checking out branch: ${params.BRANCH}"
                git branch: "${params.BRANCH}",
                    url: 'https://github.com/Linnie43/buy-01-git'
            }
        }

        stage('Build Backend') {
            agent { docker { image 'infra-jenkins:latest' args '-u root -v /var/run/docker.sock:/var/run/docker.sock' } }
            steps {
                echo "Building backend microservices"
                sh 'mvn -f backend/pom.xml clean package -DskipTests'
            }
        }
    }

    post {
        always {
            echo "Cleaning workspace"
            cleanWs notFailBuild: true
        }
    }
}
