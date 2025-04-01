def commitAndPushChanges(String branch) {
    def result = getVersionAndTag()  
    def VERSION = result.VERSION
    def TAG_NAME = result.TAG_NAME

    
    def changes = sh(script: "git status --porcelain", returnStdout: true).trim()

    if (changes) {
        // If there are changes, commit and push
        sh """
            git add .
            git commit -m "Automated commit - ${TAG_NAME}"
            git tag ${TAG_NAME}  # Tagging with version
            git push origin ${branch}  # Branch push
            git push origin ${TAG_NAME}  # Tag push
            echo "Version: ${VERSION}, Tag: ${TAG_NAME}"  # Version and Tag info
        """
    } else {
        
        echo "No changes to commit"
    }
}


commitAndPushChanges('main')
