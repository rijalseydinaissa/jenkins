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
                    // Arrêter l'ancienne instance si elle existe
                    sh 'pkill -f "jenkins-demo" || true'

                    // Démarrer la nouvelle instance en arrière-plan
                    sh 'nohup java -jar target/jenkins-demo.jar > app.log 2>&1 &'

                    // Attendre que l'app démarre
                    sleep 30

                    // Vérifier que l'app répond
                    sh 'curl -f http://localhost:8081/actuator/health || exit 1'

                    echo '✅ Application déployée avec succès!'
                    echo '🌐 Accessible sur: http://localhost:8081'
                }
            }
        }

        stage('✅ Health Check') {
            steps {
                echo '🏥 Vérification de santé de l\'application...'
                sh 'curl -f http://localhost:8081/'
                sh 'curl -f http://localhost:8081/api/demo'
                echo '✅ Tous les endpoints fonctionnent!'
            }
        }
    }

    post {
        always {
            echo '📋 Pipeline terminé!'
            archiveArtifacts artifacts: 'target/jenkins-demo.jar', fingerprint: true
        }
        success {
            echo '🎉 SUCCESS: Le déploiement a réussi!'
            echo '🌐 Application disponible sur http://localhost:8081'
        }
        failure {
            echo '❌ FAILURE: Le pipeline a échoué!'
        }
        cleanup {
            cleanWs()
        }
    }
}