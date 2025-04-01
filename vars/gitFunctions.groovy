def commitAndPushChanges(String branch) {
    // Get version and tag details
    def result = getVersionAndTag()
    def VERSION = result.VERSION
    def TAG_NAME = result.TAG_NAME

    // Check if there are any changes in the repository
    def changes = sh(script: "git status --porcelain", returnStdout: true).trim()

    if (changes) {
        // If there are changes, commit and push
        sh """
            git add .
            git commit -m "Automated commit - ${TAG_NAME}"
            git tag ${TAG_NAME}  # Tagging with the version
            git push origin ${branch}  # Pushing the branch
            git push origin ${TAG_NAME}  # Pushing the tag
            echo "Version: ${VERSION}, Tag: ${TAG_NAME}"  # Output version and tag info
        """
    } else {
        // If no changes, print a message
        echo "No changes to commit"
    }
}

// Call the function for the 'main' branch
commitAndPushChanges('main')
