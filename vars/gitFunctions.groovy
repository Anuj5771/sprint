def getVersionAndTag() {
    def VERSION = '1.0.0'  
    def BUILD_NUM = env.BUILD_NUMBER ?: 'local'  
    def TAG_NAME = "v${VERSION}-build${BUILD_NUM}" 
    return [VERSION: VERSION, TAG_NAME: TAG_NAME]
}

def commitAndPushChanges(String branch) {
    def result = getVersionAndTag()  
    def VERSION = result.VERSION
    def TAG_NAME = result.TAG_NAME

    sh """
        git add .
        git commit -m "Automated commit - ${TAG_NAME}"
        git tag ${TAG_NAME}  # Tagging with version
        git push origin ${branch}  # Branch push
        git push origin ${TAG_NAME}  # Tag push
        echo "Version: ${VERSION}, Tag: ${TAG_NAME}"  # Version aur Tag dikhana
    """
}

// Call function with branch name
commitAndPushChanges('main')
