// Aline Financial Jenkins Pipelines

pipeline {

    agent any

    environment {
        COMMIT_HASH = "${sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()}"
        AWS_ID = credentials("AWS_ID")
        // IMG_NAME = "**docker-img-name**"
        // GIT_ORGANIZATION_NAME = "**git-organization**"
        // GIT_REPO_NAME = "**git-repo-name**"
        // STACK_NAME = "**stack-name**"
        // PORT = "**port**"
        // ECR_REPOSITORY = "**ecr-repository**"
        // LISTENER_ARN = "**listener-arn**"
        // ENVIRONMENT = "**dev/staging/master**"
    }

    stages {

        stage("Clean and Test") {
            steps {
                sh "mvn clean test"
            }
        }

        stage("Test and Package") {
            steps {
                sh "mvn package"
            }
        }

        stage("Code Analysis: SonarQube") {
            steps {
                withSonarQubeEnv("SonarQube") {
                    sh "mvn sonar:sonar"
                }
            }
        }

        stage("Await Quality Gate") {
            steps {
                waitForQualityGate abortPipeline: true
            }
        }

        stage("Docker Build") {
            steps {
                echo "Docker Build..."
                sh "aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin ${AWS_ID}.dkr.ecr.us-east-2.amazonaws.com"
                sh "docker build --tag ${IMG_NAME}:${COMMIT_HASH} ."
                sh "docker tag ${IMG_NAME}:${COMMIT_HASH} ${AWS_ID}.dkr.ecr.us-east-2.amazonaws.com/${IMG_NAME}:${COMMIT_HASH}"
                echo "Docker Push..."
                sh "docker push ${AWS_ID}.dkr.ecr.us-east-2.amazonaws.com/${IMG_NAME}:${COMMIT_HASH}"
            }
        }

        stage("Deploy") {
            steps {
                echo "Fetching CloudFormation template..."
                sh "wget https://raw.githubusercontent.com/${ORGANIZATION_NAME}/${GIT_REPO_NAME}/${ENVIRONMENT}/esc-aws.yaml"
                echo "Deploying CloudFormation..."
                sh "aws cloudformation deploy --stack-name ${STACK_NAME} --template-file ./esc-aws.yaml --parameter-overrides PortNumber=${PORT} ListenerArn=${LISTENER_ARN} ApplicationName=${IMG_NAME} CommitHash=${COMMIT_HASH} ApplicationEnvironment=${ENVIRONMENT} ECRRepositoryUri=${ECR_REPOSITORY} OrganizationName=${ORGANIZATION_NAME} --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM --region us-east-2"
            }
        }
    }

    post {
        always {
            sh "mvn clean"
        }
    }
}
