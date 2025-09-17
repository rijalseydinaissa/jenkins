pipeline {
    agent any

    tools {
        maven 'Maven-3.9.0'
        jdk 'JDK-21'
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
        APP_NAME = 'jenkins-demo'
        APP_PORT = '8081'
        JAVA_OPTS = '-Xmx1024m -Xms512m -Djava.awt.headless=true -Dspring.profiles.active=dev'
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

                // Verify JAR was created and show its info
                sh '''
                    echo "🔍 Vérification du JAR créé:"
                    ls -la target/*.jar
                    echo "📋 Contenu du manifest:"
                    jar tf target/${APP_NAME}.jar | grep -E "(BOOT-INF|META-INF|MANIFEST)" | head -10
                '''
            }
        }

        stage('🚀 Deploy/Run') {
            steps {
                echo '🚀 Démarrage de l\'application...'
                script {
                    // Kill any existing instances
                    sh "pkill -f ${APP_NAME}.jar || true"
                    sh "sleep 3"

                    // Check available memory and Java version
                    sh '''
                        echo "🖥️  System Info:"
                        free -h
                        java -version
                        echo "📂 Working Directory: $(pwd)"
                        echo "🗂️  JAR Location:"
                        ls -la target/${APP_NAME}.jar
                    '''

                    // Start with enhanced logging
                    sh """
                        echo "🚀 Starting application with enhanced logging..."
                        nohup java ${JAVA_OPTS} \\
                            -Dlogging.level.org.springframework=DEBUG \\
                            -Dlogging.level.org.apache.catalina=INFO \\
                            -Dserver.port=${APP_PORT} \\
                            -jar target/${APP_NAME}.jar > app.log 2>&1 &

                        echo \$! > app.pid
                        sleep 5

                        echo "📝 Initial logs:"
                        head -50 app.log || echo "No logs yet"

                        echo "🔍 Process check:"
                        ps aux | grep -v grep | grep ${APP_NAME}.jar || echo "Process not found"
                    """

                    // Enhanced health check with more detailed debugging
                    def maxRetries = 12  // Increase to 2 minutes
                    def retryCount = 0
                    def healthy = false

                    while (retryCount < maxRetries && !healthy) {
                        sleep 10
                        retryCount++

                        // Check if process is still running
                        def processCheck = sh(
                            script: "ps aux | grep -v grep | grep ${APP_NAME}.jar | wc -l",
                            returnStdout: true
                        ).trim()

                        echo "⏳ Tentative ${retryCount}/${maxRetries} - Process running: ${processCheck}"

                        if (processCheck == "0") {
                            echo "❌ Application process died! Last logs:"
                            sh 'tail -50 app.log || echo "No logs available"'
                            error("Application process terminated unexpectedly")
                        }

                        // Show recent logs
                        sh 'echo "📋 Recent logs:"; tail -20 app.log || echo "No logs yet"'

                        // Try health check
                        def curlResult = sh(
                            script: "curl -f -s -m 5 http://localhost:${APP_PORT}/actuator/health 2>/dev/null || echo 'UNHEALTHY'",
                            returnStdout: true
                        ).trim()

                        if (curlResult.contains('"status":"UP"') || curlResult.contains('{"status":"UP"}')) {
                            healthy = true
                            echo '✅ Application healthy!'
                        } else {
                            echo "🏥 Health check result: ${curlResult}"

                            // Try a simpler endpoint if actuator fails
                            def simpleCheck = sh(
                                script: "curl -f -s -m 5 http://localhost:${APP_PORT}/ 2>/dev/null | head -c 100 || echo 'NO_RESPONSE'",
                                returnStdout: true
                            ).trim()
                            echo "🌐 Root endpoint check: ${simpleCheck}"
                        }
                    }

                    if (!healthy) {
                        echo '❌ App not healthy after retries. Full diagnostic:'
                        sh '''
                            echo "🔍 Full application logs:"
                            cat app.log || echo "No log file found"

                            echo "🌐 Network connections:"
                            netstat -tlnp | grep ${APP_PORT} || echo "Port not bound"

                            echo "⚡ Process status:"
                            ps aux | grep java || echo "No Java processes"

                            echo "💾 Disk space:"
                            df -h .

                            echo "🗂️  Directory contents:"
                            ls -la target/
                        '''
                        error('App startup failed - check diagnostic info above')
                    }

                    echo '✅ Application déployée avec succès!'
                    echo "🌐 Accessible sur: http://localhost:${APP_PORT}"
                }
            }
        }

        stage('✅ Health Check') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                echo '🏥 Vérification de santé de l\'application...'
                sh """
                    echo "Testing root endpoint:"
                    curl -f -s -m 10 http://localhost:${APP_PORT}/ | head -c 200

                    echo "\\nTesting demo API:"
                    curl -f -s -m 10 http://localhost:${APP_PORT}/api/demo || echo "Demo endpoint may not exist"

                    echo "\\nTesting actuator health:"
                    curl -f -s -m 10 http://localhost:${APP_PORT}/actuator/health
                """
                echo '✅ Tous les endpoints testés!'
            }
        }
    }

    post {
        always {
            echo '📋 Pipeline terminé!'

            // Enhanced artifact collection
            script {
                sh '''
                    echo "📊 Final system state:"
                    ps aux | grep java || echo "No Java processes"
                    netstat -tlnp | grep ${APP_PORT} || echo "Port not in use"
                '''
            }

            archiveArtifacts artifacts: "target/${APP_NAME}.jar, app.log, app.pid",
                            fingerprint: true,
                            allowEmptyArchive: true
        }
        success {
            echo '🎉 SUCCESS: Le déploiement a réussi!'
            echo "🌐 Application disponible sur http://localhost:${APP_PORT}"
        }
        failure {
            echo '❌ FAILURE: Le pipeline a échoué!'
            echo '🔍 Vérifiez app.log et les diagnostics ci-dessus pour plus de détails.'
        }
        cleanup {
            script {
                sh """
                    echo "🧹 Cleanup: Stopping application..."
                    if [ -f app.pid ]; then
                        kill \$(cat app.pid) || true
                    fi
                    pkill -f ${APP_NAME}.jar || true
                    sleep 3
                """
            }
            cleanWs()
        }
    }
}