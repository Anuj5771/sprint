
def call(String parentDir, String playbookToRun) {
    stage("Run playbook") {
        script {
            def dirPath = parentDir ?: '.'

            dir(dirPath) {
                sh """
                    ansible-playbook -i aws_ec2.yml ${playbookToRun}
                """
            }
        }
    }
}

