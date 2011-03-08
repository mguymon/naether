class Naether
  # :title:Naether::DeployArtifact
  #
  # Information for deploying an Artifact
  #
  # = Authors
  # Michael Guymon
  #
  class DeployArtifact
    
    # 
    # valid optional opts:
    #   :pom_path - file path to artifact's pom
    #   :username - username for authentication with destination repository
    #   :password - password for authentication with destination repository
    #   :pub_key  - public key path for authentication with destination repository
    #   :pub_key _passphrase - public key passphrase for authentication with destination repository
    def initialize( notation, file_path, url, opts = {} )
      
      if Naether.platform == 'java'
        @deploy_artifact = com.slackworks.naether.DeployArtifact.new 
      else
        deployArtifactClass = Rjb::import('com.slackworks.naether.DeployArtifact') 
        @deploy_artifact = deployArtifactClass.new
      end
      
      @deploy_artifact.setRemoteRepo( url )
      @deploy_artifact.setNotation( notation )
      @deploy_artifact.setFilePath( file_path )
      if opts[:pom_path]
        @deploy_artifact.setPomPath( pom_path )
      end
      
      if opts[:user_name] || opts[:pub_key]
        @deploy_artifact.setAuth(opts[:user_name], opts[:password], opts[:pub_key], opts[:pub_key_passphrase] )
      end
    end
  end
  
end