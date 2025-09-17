pipeline {
    agent any

    tools {
        maven 'Maven-3.9.0' // Assurez-vous que Maven est configuré dans Jenkins
        jdk 'JDK-21'        // Java 21 configuré dans Jenkins
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
        APP_NAME = 'jenkins-demo'
        APP_PORT = '8081'   // Centralized port var for easy changes
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

        stage('📦 Package') {
            steps {
                echo '📦 Création du JAR...'
                sh 'mvn package -DskipTests'
            }
        }

        stage('🚀 Deploy/Run') {
            steps {
                echo '🚀 Démarrage de l\'application...'
                script {
                    // Arrêter l'ancienne instance si elle existe (plus précis)
                    sh "pkill -f ${APP_NAME}.jar || true"
                    sleep 2  // Petit délai pour cleanup

                    // Démarrer la nouvelle instance en arrière-plan avec plus de mémoire
                    sh "nohup java -Xmx1024m -jar target/${APP_NAME}.jar > app.log 2>&1 &"
                    echo '📝 Logs de démarrage dans app.log...'

                    // Amélioration: Boucle de retry pour attendre le démarrage (max 60s, 6 tentatives de 10s)
                    def maxRetries = 6
                    def retryCount = 0
                    def healthy = false
                    while (retryCount < maxRetries && !healthy) {
                        sleep 10
                        def curlResult = sh(script: "curl -f -s http://localhost:${APP_PORT}/actuator/health || echo 'UNHEALTHY'", returnStdout: true).trim()
                        if (curlResult == '{"status":"UP"}') {  // Assumes standard actuator JSON response
                            healthy = true
                            echo '✅ Application healthy!'
                        } else {
                            retryCount++
                            echo "⏳ Tentative ${retryCount}/${maxRetries} - Statut: ${curlResult}"
                            sh 'tail -n 10 app.log || true'  // Debug logs each retry
                        }
                    }
                    if (!healthy) {
                        echo '❌ App not healthy after retries. Full log:'
                        sh 'cat app.log'
                        error('App startup failed - check app.log for details')
                    }

                    // Debug : Afficher les dernières lignes des logs (optionnel, après succès)
                    sh 'tail -n 20 app.log || true'

                    echo '✅ Application déployée avec succès!'
                    echo "🌐 Accessible sur: http://localhost:${APP_PORT}"
                }
            }
        }

        stage('✅ Health Check') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }  // Skip si échec précédent
            }
            steps {
                echo '🏥 Vérification de santé de l\'application...'
                sh "curl -f -s http://localhost:${APP_PORT}/ || exit 1"
                sh "curl -f -s http://localhost:${APP_PORT}/api/demo || exit 1"
                echo '✅ Tous les endpoints fonctionnent!'
            }
        }
    }

    post {
        always {
            echo '📋 Pipeline terminé!'
            archiveArtifacts artifacts: "target/${APP_NAME}.jar, app.log", fingerprint: true, allowEmptyArchive: true
        }
        success {
            echo '🎉 SUCCESS: Le déploiement a réussi!'
            echo "🌐 Application disponible sur http://localhost:${APP_PORT}"
        }
        failure {
            echo '❌ FAILURE: Le pipeline a échoué!'
            echo '🔍 Vérifiez app.log dans les artifacts pour plus de détails.'
            // Optionnel: Envoyer une notification (e.g., emailext)
        }
        cleanup {
            // Arrêter l'app à la fin pour cleanup (même en cas d'échec)
            script {
                sh "pkill -f ${APP_NAME}.jar || true"
                sleep 2  // Délai pour graceful shutdown
            }
            cleanWs()
        }
    }
}