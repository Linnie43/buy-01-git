pipeline {
    agent any

    environment {
        DOCKER_COMPOSE = "docker-compose.dev.yml"
        PROJECT_NAME   = "buy-01"
        PREVIOUS_TAG   = "previous_build"
        CURRENT_TAG    = "latest_build"
        NOTIFY_EMAIL   = "team@example.com"
    }

    options {
        timestamps()
    }

    triggers {
        // Auto-trigger pipeline when new commit is pushed
        pollSCM('* * * * *')   // every 1 minute
    }

    stages {

        stage('Checkout') {
            steps {
                echo "📥 Pulling code from GitHub..."
                git branch: 'dev',
                    url: 'https://github.com/teieorg/teieprojekt.git',
                    credentialsId: 'github-creds'
            }
        }

        stage('Build Backend') {
            steps {
                echo "🔨 Building backend microservices..."
                sh '''
                    find backend -name "mvnw" -exec chmod +x {} \\;
                    ./backend/user-service/mvnw -f backend/user-service/pom.xml clean package -DskipTests=false
                    ./backend/product-service/mvnw -f backend/product-service/pom.xml clean package -DskipTests=false
                    ./backend/media-service/mvnw -f backend/media-service/pom.xml clean package -DskipTests=false
                '''
            }
        }

        stage('Build Frontend') {
            steps {
                echo "🌐 Building Angular frontend..."
                sh '''
                    cd frontend
                    npm install
                    npm run build
                '''
            }
        }

        stage('Run Backend Tests') {
            steps {
                echo "🧪 Running backend JUnit tests..."
                sh '''
                    ./backend/user-service/mvnw -f backend/user-service/pom.xml test
                    ./backend/product-service/mvnw -f backend/product-service/pom.xml test
                    ./backend/media-service/mvnw -f backend/media-service/pom.xml test
                '''
            }
        }

        stage('Run Frontend Tests') {
            steps {
                echo "🧪 Running Angular tests..."
                sh '''
                    cd frontend
                    npm run test -- --watch=false --browsers=ChromeHeadless
                '''
            }
        }

        stage('Backup Previous Deployment') {
            steps {
                echo "📦 Tagging current images for rollback..."
                sh """
                    docker compose -f ${DOCKER_COMPOSE} pull || true
                    docker compose -f ${DOCKER_COMPOSE} ps -q | xargs -I {} docker commit {} ${PROJECT_NAME}:${PREVIOUS_TAG} || true
                """
            }
        }

        stage('Deploy') {
            steps {
                echo "🚀 Deploying new version..."
                sh """
                    docker compose -f ${DOCKER_COMPOSE} down
                    docker compose -f ${DOCKER_COMPOSE} build
                    docker compose -f ${DOCKER_COMPOSE} up -d
                """
            }
        }
    }

    post {

        success {
            echo "✅ Pipeline completed successfully!"
            emailext (
                subject: "SUCCESS: Build OK (${PROJECT_NAME})",
                body: "The Jenkins build & deployment succeeded.",
                to: "${NOTIFY_EMAIL}"
            )
        }

        failure {
            echo "❌ Build FAILED — triggering rollback!"

            // Rollback block
            sh """
                echo 'Restoring previous working version...'
                docker compose -f ${DOCKER_COMPOSE} down
                docker compose -f ${DOCKER_COMPOSE} up -d
            """

            emailext (
                subject: "FAILURE: Build FAILED (${PROJECT_NAME})",
                body: "The Jenkins build failed and rollback was executed.",
                to: "${NOTIFY_EMAIL}"
            )
        }
    }
}
