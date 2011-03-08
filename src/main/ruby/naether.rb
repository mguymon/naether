require "#{File.dirname(__FILE__)}/naether/bootstrap"
require "#{File.dirname(__FILE__)}/naether/java"

# :title:Naether
#
# Java dependency resolver using Maven's Aether. 
#
# = Authors
# Michael Guymon
#
class Naether
  
  # Naether jar path will default to packaged in the gem
  JAR_LIB = "#{File.dirname(__FILE__)}/.."
  
  class << self
    
    # Helper to determine the platform
    def platform
      $platform || RUBY_PLATFORM[/java/] || 'ruby'
    end
    
    # Java dependencies needed to bootstrap Naether
    def bootstrap_dependencies( dep_file=nil )
      Naether::Bootstrap.dependencies( dep_file )
    end
    
    # Loads all jars from paths and creates a new instance of Naether
    def create_from_paths( deps_jar_dir, naether_jar_dir = nil )
      naether_jar_dir = naether_jar_dir || JAR_LIB
      Naether::Java.load_jars_dir( [deps_jar_dir, naether_jar_dir] )
      
      Naether.new
    end
    
    # Loads all jars creates a new instance of Naether
    def create_from_jars( jars )
      Naether::Java.load_jars( jars )
      
      Naether.new
    end
  end
  
  # Create new instance. Naether.create_from_paths and Naether.create_from_jars should be
  # used instead of Naether.new to ensure the dependencies for Naether are set into the classpath
  def initialize()
    
    if Naether.platform == 'java'
      @resolver = com.slackworks.naether.Naether.new 
    else
      naetherClass = Rjb::import('com.slackworks.naether.Naether') 
      @resolver = naetherClass.new
    end
  end
  
  # Clear all remote repositories
  def clear_remote_repositories
    @resolver.clearRemoteRepositories()
  end
  
  # Add remote repository
  def add_remote_repository( url, username = nil, password = nil )
    if username
      @resolver.addRemoteRepositoryByUrl( url, username, password )
    else
      @resolver.addRemoteRepositoryByUrl( url )
    end
  end
  
  # Array of remote repositories
  def remote_repositories
    if Naether.platform == 'java'
      return @resolver.getRemoteRepositories()
    else
      return @resolver.getRemoteRepositories().toArray()
    end 
  end
  
  def local_repo_path
    @resolver.getLocalRepoPath()
  end
  
  def local_repo_path=( path )
    @resolver.setLocalRepoPath( path )
  end
  
  # Add a dependency in the notation: groupId:artifactId:type:version
  def add_dependency( notation )
    @resolver.addDependency( notation )
  end
  
  # Set array of dependencies in the notation: groupId:artifactId:type:version
  def dependencies=(dependencies)
    dependencies.each do |dependent|
      add_dependency( dependent )  
    end
  end
  
  # Get dependencies
  def dependencies()
    if Naether.platform == 'java'
      return @resolver.getDependenciesNotation().to_a
    else
      return @resolver.getDependenciesNotation().toArray().map{ |dep| dep.toString() }
    end 
  end
  
  # Resolve dependencies, finding related additional dependencies
  def resolve_dependencies( download_artifacts = true )
    @resolver.resolveDependencies( download_artifacts );
    dependencies
  end
  
  def deploy_artifact( notation, file_path, url, opts = {} )
    if Naether.platform == 'java'
      @instance = com.slackworks.naether.DeployArtifact.new 
    else
      deployArtifactClass = Rjb::import('com.slackworks.naether.DeployArtifact') 
      @instance = deployArtifactClass.new
    end
    
    @instance.setRemoteRepo( url )
    @instance.setNotation( notation )
    @instance.setFilePath( file_path )
    if opts[:pom_path]
      @instance.setPomPath( pom_path )
    end
    
    if opts[:user_name] || opts[:pub_key]
      @instance.setAuth(opts[:user_name], opts[:password], opts[:pub_key], opts[:pub_key_passphrase] )
    end
    
    @resolver.deployArtifact(@instance)
  end
  
  # filePath to write the pom 
  # notation of the pom, groupId:artifactId:type:version
  #
  # loads all resolved dependencies into pom
  def write_pom( filePath, notation )
    if Naether.platform == 'java'
      @project_instance = com.slackworks.naether.MavenProject.new 
    else
      projectClass = Rjb::import('com.slackworks.naether.MavenProject') 
      @project_instance = projectClass.new
    end
    
    @project_instance.setNotation( notation )
    
    dependencies().each do |notation|
      @project_instance.addDependency( notation )
    end
    
    @project_instance.writePom( filePath )
    
  end
end