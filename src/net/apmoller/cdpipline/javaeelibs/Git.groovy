package net.apmoller.cdpipline.javaeelibs

class Git implements Serializable {
    private static final String GIT_TOOL_NAME="git_2_x"
    public static final String BITBUCKET_HTTPS_URL ="https://git.maerskdev.net/scm"
    def pipeline, username, email, credentialId

    Git(jenkins,name="rest_user",email="service-account@maerskdev.net",credentialId="mdev_bb_token"){
        this.pipeline=jenkins
        this.username=name
        this.email=email
        this.credentialId=credentialId
    }

    /**
     *
     * @param stage
     * @param _args.project
     * @param _args.repository
     * @param _args.branches
     * @param _args.credentialsId
     * @return
     */
    def gitCheckout(_args=[:]) {
        String project=_args.project
        String repository=_args.repository
        String branches=_args.branches
        String url="${BITBUCKET_HTTPS_URL}/${project}/${repository}.git"
        String credentialsId=_args.credentialsId
        return pipeline.checkout([$class           : 'GitSCM',
                                  branches         : [[name: branches]],
                                  userRemoteConfigs: [[ credentialsId:credentialsId,
                                                     url: url]]])
    }

    def checkoutSCM() {
        def _scm,git
        git=getGitCommandLine()
        pipeline.sh 'git config --global credential.helper store'
        _scm=pipeline.checkout(pipeline.scm)
        pipeline.sh """
            $git config --local user.email '$email'
            $git config --local user.name '$username'
            
        """
        return _scm
    }

    private def getGitCommandLine(){
        def git, gitTool
        try{
            gitTool="${pipeline.tool name:'Git_2_x'}"
        }catch(Exception r){
            // didn't want to fail the build due to missing tool, Hence Handling it and defaulting it
            pipeline.echo "WARNING: ${r}"
        }
        if(gitTool) {
            git=gitTool
        }else{
            git="git"
        }
        return git
    }

    def tag(branchName,tagName,_args="") {
        def git= getGitCommandLine()
        pipeline.sh """
          $git tag -a $branchName/$tagName -m 'Release $branchName/$tagName'
          $git push --tags
        """
    }

    def commitChangedPOM(branchName,buildVersion,_args="") {
        def git= getGitCommandLine()
        pipeline.sh """
          $git add pom.xml
          $git commit -m 'pom file version $buildVersion change from pipeline'
          $git push origin HEAD:$branchName
        """
    }

    def commitChanges(branchName,files=".",commitMessage="Changes are from pipeline") {
        def git= getGitCommandLine()
        pipeline.sh """
          $git add ${files}
          $git commit -m \'${commitMessage}\'
          $git push origin HEAD:$branchName
        """
    }
}
