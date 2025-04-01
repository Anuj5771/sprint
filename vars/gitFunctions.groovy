
def getVersionAndTag() {
    def VERSION = '1.0.0'  // Define the version here
    def TAG_NAME = "v${VERSION}-build${BUILD_NUMBER}" 
    return [VERSION: VERSION, TAG_NAME: TAG_NAME]
}

def commitAndPushChanges(String branch) {
    def result = getVersionAndTag()  
    def TAG_NAME = result.TAG_NAME

    sh """
        git add .
        git commit -m "Automated commit - ${BUILD_NUMBER}"
        git tag ${TAG_NAME}
        git push origin ${branch}
        git push origin ${TAG_NAME}
    """
}


commitAndPushChanges('main') 

