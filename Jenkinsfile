pipeline {
    agent any

    tools {
        maven 'Maven-3.9.0'
        jdk 'JDK-21'
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
        RENDER_SERVICE_ID = 'srv-d3620j0dl3ps739cl4l0'  // Remplacez par l'ID réel (ex. srv-abc123)
        RENDER_APP_URL = 'https://jenkins-demo-2wrc.onrender.com'
    }

    stages {
        stage('🔄 Checkout') {
            steps {
                echo '📦 Récupération du code source...'
                checkout scm
            }
        }

        stage('🧹 Clean') {
            steps {
                echo '🧹 Nettoyage du projet...'
                sh 'mvn clean'
            }
        }

        stage('📋 Compile') {
            steps {
                echo '⚙️ Compilation du projet...'
                sh 'mvn compile'
            }
        }

        stage('🧪 Tests') {
            steps {
                echo '🧪 Exécution des tests...'
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    echo '📊 Résultats des tests publiés!'
                }
            }
        }

        stage('🚀 Deploy to Render') {
            when {
                branch 'main'
            }
            steps {
                echo '🚀 Déclenchement du déploiement sur Render...'
                withCredentials([string(credentialsId: 'RENDER_API_KEY', variable: 'API_KEY')]) {
                    sh """
                        # Trigger le deploy
                        curl -X POST \\
                             -H "Accept: application/json" \\
                             -H "Authorization: Bearer \${API_KEY}" \\
                             -H "Content-Type: application/json" \\
                             --data '{}' \\
                             https://api.render.com/v1/services/\${RENDER_SERVICE_ID}/deploys
                    """
                }
                echo '✅ Déploiement déclenché sur Render ! Vérifiez le statut dans le dashboard Render.'

                // Optionnel : Attendre et checker le statut du deploy (ajout pour robustesse)
                script {
                    sleep 30  // Attendre un peu
                    def status = sh(
                        script: """
                            curl -s -H "Authorization: Bearer \${API_KEY}" \\
                                 https://api.render.com/v1/services/\${RENDER_SERVICE_ID}/deploys | \\
                            jq -r '.[0].status'  # Nécessite jq installé sur l'agent Jenkins
                        """,
                        returnStdout: true
                    ).trim()
                    echo "📊 Statut du dernier deploy: ${status}"
                    if (status != 'live') {
                        error("Deploy échoué: ${status}")
                    }
                }
            }
        }
    }

    post {
        always {
            echo '📋 Pipeline terminé!'
        }
        success {
            echo '🎉 SUCCESS: Le déploiement a réussi!'
            echo "🌐 App accessible sur: https://votre-app.onrender.com"  // Remplacez par votre URL Render
        }
        failure {
            echo '❌ FAILURE: Le pipeline a échoué!'
        }
        cleanup {
            cleanWs()
        }
    }
}