
def call() {
    def userInput = input(
        message: "Do you approve the deployment of the postgres role?",
        parameters: [
            choice(name: 'Approval', choices: ['Approve', 'Reject'], description: 'Approve or Reject the deployment')
        ]
    )

    if (userInput == 'Approve') {
        echo "Deployment can be done"
    } else {
        echo "Deployment is rejected"
        error("Deployment has been rejected. Stopping pipeline.")
    }
}

