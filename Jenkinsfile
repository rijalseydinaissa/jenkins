pipeline {
    agent any

    tools {
        maven 'Maven-3.9.0' // Assurez-vous que Maven est configuré dans Jenkins
        jdk 'JDK-21'        // Java 21 configuré dans Jenkins
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
        APP_NAME = 'jenkins-demo'
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
                    sh 'pkill -f jenkins-demo.jar || true'
                    sleep 2  // Petit délai pour cleanup

                    // Démarrer la nouvelle instance en arrière-plan avec plus de mémoire
                    sh 'nohup java -Xmx1024m -jar target/jenkins-demo.jar > app.log 2>&1 &'
                    echo '📝 Logs de démarrage dans app.log...'

                    // Attendre que l'app démarre (augmenté à 30s)
                    sleep 30

                    // Debug : Afficher les dernières lignes des logs (optionnel)
                    sh 'tail -n 20 app.log || true'

                    // Vérifier que l'app répond (avec timeout pour éviter les hangs)
                    timeout(time: 30, unit: 'SECONDS') {
                        sh 'curl -f -s http://localhost:8081/actuator/health || exit 1'
                    }

                    echo '✅ Application déployée avec succès!'
                    echo '🌐 Accessible sur: http://localhost:8081'
                }
            }
        }

        stage('✅ Health Check') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }  // Skip si échec précédent
            }
            steps {
                echo '🏥 Vérification de santé de l\'application...'
                sh 'curl -f -s http://localhost:8081/ || exit 1'
                sh 'curl -f -s http://localhost:8081/api/demo || exit 1'
                echo '✅ Tous les endpoints fonctionnent!'
            }
        }
    }

    post {
        always {
            echo '📋 Pipeline terminé!'
            archiveArtifacts artifacts: 'target/jenkins-demo.jar, app.log', fingerprint: true, allowEmptyArchive: true
        }
        success {
            echo '🎉 SUCCESS: Le déploiement a réussi!'
            echo '🌐 Application disponible sur http://localhost:8081'
        }
        failure {
            echo '❌ FAILURE: Le pipeline a échoué!'
            echo '🔍 Vérifiez app.log dans les artifacts pour plus de détails.'
        }
        cleanup {
            // Arrêter l'app à la fin pour cleanup (même en cas d'échec)
            script {
                sh 'pkill -f jenkins-demo.jar || true'
            }
            cleanWs()
        }
    }
}