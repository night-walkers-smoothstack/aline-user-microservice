// User Micrservice Pipeline

pipeline {

    agent any
    environment {
        COMMIT_HASH = "${sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()}"
        AWS_ID = credentials('AWS_ID')
        SERVICE_NAME = 'user'
        SERVICE_PORT = 8070
        REGION = 'us-east-2'
        APP_NAME = 'alinefinancial'
        APP_ENV = 'dev'
        ORGANIZATION = 'Aline-Financial'
        PROJECT_NAME = 'aline-user-microservice'
    }

    stages {
    
        stage("Clean and Test") {
        
            steps {

                sh "mvn clean test"

            }

        }

        stage("Package Jar") {
        
            steps {

                sh "mvn package -DskipTests"

            }
            
        }

        stage("Setup Image Stack") {

            steps {
                echo "Fetching CloudFormation template 'setup-stack.yml'..."
                sh "wget https://raw.githubusercontent.com/${ORGANIZATION}/${PROJECT_NAME}/${APP_ENV}/setup-stack.yml"
                echo "Deploying 'Setup Stack'..."
                sh '''
                    aws cloudformation deploy \
                        --stack-name ${SERVICE_NAME}-setup-stack \
                        --template-file setup-stack.yml \
                        --parameter-overrides \
                            AppEnv=${APP_ENV} \
                            AppName=${APP_NAME} \
                            ServiceName=${SERVICE_NAME} \
                        --capabilities CAPABILITY_NAMED_IAM \
                        --no-fail-on-empty-changeset
                '''            
            }

        }

        stage("Docker Build") {
        
            steps {
            
                echo "Docker build ${SERVICE_NAME}..."
                sh "aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin --password-stdin ${AWS_ID}.dkr.ecr.${REGION}.amazonaws.com"
                echo "Building image ${SERVICE_NAME}..."
                sh "docker build --tag ${APP_NAME}/${APP_ENV}/${SERVICE_NAME}:${COMMIT_HASH} ."
                echo "Tagging image ${SERVICE_NAME}..."
                sh "docker tag ${APP_NAME}/${APP_ENV}/${SERVICE_NAME}:${COMMIT_HASH} ${AWS_ID}.dkr.ecr.${REGION}.amazonaws.com/${APP_NAME}/${APP_ENV}/${SERVICE_NAME}:${COMMIT_HASH}"
                echo "Docker push ${SERVICE_NAME}..."
                sh "docker push ${AWS_ID}.dkr.ecr.${REGION}.amazonaws.com/${APP_NAME}/${APP_ENV}/${SERVICE_NAME}:${COMMIT_HASH}"
            }
        
        }

        stage("Deploy") {

            steps {

                echo "Fetching CloudFormation template 'deploy-stack.yml'..."
                sh "wget https://raw.githubusercontent.com/${ORGANIZATION}/${PROJECT_NAME}/${APP_ENV}/deploy-stack.yml"
                echo "Deploying ${SERVICE_NAME}..."

                sh '''
                    aws cloudformation deploy \
                    --stack-name ${SERVICE_NAME}-stack \
                    --template-file deploy-stack.yml \
                    --parameter-overrides \
                        AppEnv=${APP_ENV} \
                        AppName=${APP_NAME} \
                        ServiceName=${SERVICE_NAME} \
                        ServicePort=${SERVICE_PORT} \
                        CommitHash=${COMMIT_HASH} \
                    --capabilities CAPABILITY_NAMED_IAM \
                    --no-fail-on-empty-changeset
                '''
            }

        }

    }

}
