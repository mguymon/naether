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
  JAR_PATH = "#{JAR_LIB}/naether-0.5.2.jar" #XXX: hardcoded, should be based on VERSION file
  
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
    Naether::Java.convert_to_ruby_array(@resolver.getRemoteRepositories())
  end
  
  # Path to local maven repo
  def local_repo_path
    @resolver.getLocalRepoPath()
  end
  
  # Set path to local maven repo
  def local_repo_path=( path )
    @resolver.setLocalRepoPath( path )
  end
  
  # Add a dependency in the notation: groupId:artifactId:type:version
  def add_notation_dependency( notation, scope='compile' )
    @resolver.addDependency( notation, scope )
  end
  
  # Add dependencies from a Maven POM
  def add_pom_dependencies( pom_path, scopes=['compile'] )
    if Naether.platform == 'java'
      @resolver.addDependencies( pom_path, scopes )
    else
      list = Rjb::import("java.util.ArrayList").new
      scopes.each do |scope|
        list.add( scope )
      end
      @resolver._invoke( 'addDependencies', 'Ljava.lang.String;Ljava.util.List;', pom_path, list )
    end
  end
  
  # Add a dependency of org.sonatype.aether.graph.Dependency Java object
  def add_dependency( dependency )
    #@resolver.addDependency( dependency )
    if Naether.platform == 'java'
      @resolver.addDependency( dependency )
    else
      @resolver._invoke('addDependency', 'Lorg.sonatype.aether.graph.Dependency;', dependency)
    end
  end
  
  # Array of mixed dependencies
  def dependencies=(dependencies)
    @resolver.clearDependencies()
    
    unless dependencies.is_a? Array
      dependencies = [dependencies]
    end
    
    dependencies.each do |dependent|
      # Hash of notation => scope
      if dependent.is_a? Hash
        key = dependent.keys.first
        
        # Add pom dependencies with scopes
        if key =~ /\.xml$/i
          scopes = nil
          if dependent[key].is_a? Array
            scopes = dependent[key]
          else
            scopes = [dependent[key]]
          end
          
          add_pom_dependencies( key, scopes )
          
        # Add a dependency notation with scopes
        else
          add_notation_dependency( key, dependent[key] )
        end
        
      elsif dependent.is_a? String
        
        # Add pom dependencies with default scopes
        if dependent =~ /\.xml$/i
          add_pom_dependencies( dependent )
          
        # Add a dependency notation with compile scope
        else
          add_notation_dependency( dependent, 'compile' )
        end
      
      # Add an Aether dependency
      else
        add_dependency( dependent )
      end
    end
  end
  
  # Get array of dependencies
  def dependencies()
    Naether::Java.convert_to_ruby_array( @resolver.getDependencies() )
  end
  
  # Get array of dependencies as notation
  def dependenciesNotation()
    Naether::Java.convert_to_ruby_array(@resolver.getDependenciesNotation(), true)
  end
  
  def dependencies_classpath()
    @resolver.getResolvedClassPath()
  end
  
  def load_dependencies_to_classpath
    jars = dependencies_classpath.split(":")
    Naether::Java.load_jars(jars)
    
    jars
  end
  
  # Resolve dependencies, finding related additional dependencies
  def resolve_dependencies( download_artifacts = true )
    @resolver.resolveDependencies( download_artifacts );
    dependenciesNotation
  end
  
  # Deploy artifact to remote repo url
  def deploy_artifact( notation, file_path, url, opts = {} )
    if Naether.platform == 'java'
      @instance = com.slackworks.naether.deploy.DeployArtifact.new 
    else
      deployArtifactClass = Rjb::import('com.slackworks.naether.deploy.DeployArtifact') 
      @instance = deployArtifactClass.new
    end
    
    @instance.setRemoteRepo( url )
    @instance.setNotation( notation )
    @instance.setFilePath( file_path )
    if opts[:pom_path]
      @instance.setPomPath( opts[:pom_path] )
    end
    
    if opts[:username] || opts[:pub_key]
      @instance.setAuth(opts[:username], opts[:password], opts[:pub_key], opts[:pub_key_passphrase] )
    end
    
    @resolver.deployArtifact(@instance)
  end
  
  # Install artifact to local repo
  def install_artifact( notation, file_path, opts = {} )
    if Naether.platform == 'java'
      @instance = com.slackworks.naether.deploy.DeployArtifact.new 
    else
      deployArtifactClass = Rjb::import('com.slackworks.naether.deploy.DeployArtifact') 
      @instance = deployArtifactClass.new
    end
    
    @instance.setNotation( notation )
    @instance.setFilePath( file_path )
    if opts[:pom_path]
      @instance.setPomPath( opts[:pom_path] )
    end
    
    @resolver.installArtifact(@instance)
  end
  
  def load_pom( file_path )
    @project_instance = Naether::Java.create("com.slackworks.naether.maven.Project", file_path )
  end
  
  def pom_dependencies( file_path=nil, scopes = nil)
    if file_path
      load_pom( file_path )
    end

    if Naether.platform == 'java'
      deps = @project_instance.getDependenciesNotation( scopes, true )
    else
      if scopes
        list = Rjb::import("java.util.ArrayList").new
        scopes.each do |scope|
          list.add( scope )
        end
      end
      deps = @project_instance._invoke('getDependenciesNotation', 'Ljava.util.List;Z', list, true)
    end
    
    Naether::Java.convert_to_ruby_array( deps, true )
  end
  
  # Get the POM version
  def pom_version( file_path=nil )
    if file_path
      load_pom( file_path )
    end
    
    return @project_instance.getVersion()
  end
  
  # filePath to write the pom 
  # notation of the pom, groupId:artifactId:type:version
  #
  # loads all resolved dependencies into pom
  def write_pom( notation, file_path )
    @project_instance = Naether::Java.create("com.slackworks.naether.maven.Project")
    @project_instance.setProjectNotation( notation )
    
    dependencies().each do |notation|
      @project_instance.addDependency( notation )
    end
    
    @project_instance.writePom( file_path )
    
  end
end
