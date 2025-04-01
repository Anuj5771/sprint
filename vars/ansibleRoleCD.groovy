
def call(String url, String branch, String creds, String parentDir, String playbookToRun) {
    gitCheckout(url, branch, creds)
    deployApproval()
    runPlaybook(parentDir, playbookToRun)
}

