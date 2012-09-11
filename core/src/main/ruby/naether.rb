require "#{File.dirname(__FILE__)}/naether/configuration"
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
  
  class << self
    
    # Java dependencies needed to bootstrap Naether
    def bootstrap_dependencies( dep_file=nil )
      Naether::Bootstrap.dependencies( dep_file )
    end
    
    def platform
      Naether::Configuration.platform
    end
    

    # Loads all jars creates a new instance of Naether
    def create_from_jars( jars )
      Naether::Java.internal_load_paths( jars )
      Naether.new
    end
    
  end
  
  # Create new instance. Naether.create_from_paths and Naether.create_from_jars should be
  # used instead of Naether.new to ensure the dependencies for Naether are set into the classpath
  def initialize
    @resolver = Naether::Java.create('com.tobedevoured.naether.Naether')
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
  
  def remote_repository_urls
    Naether::Java.convert_to_ruby_array(@resolver.getRemoteRepositoryUrls(), true)
  end
  
  # Path to local maven repo
  def local_repo_path
    @resolver.getLocalRepoPath()
  end
  
  # Set path to local maven repo
  def local_repo_path=( path )
    @resolver.setLocalRepoPath( path )
  end
  
  def add_build_artifact( notation, path, pom = nil )
    @resolver.addBuildArtifact(notation, path, pom )
  end
  
  def build_artifacts=( artifacts )
    @resolver.clearBuildArtifacts()
    
    unless artifacts.is_a? Array
      artifacts = [artifacts]
    end
    
    artifacts.each do |artifact|
      # Hash of notation => path or notation => { :path =>, :pom => }
      if artifact.is_a? Hash
        
        notation, opts = artifact.shift
        
        if opts.is_a? Hash
          @resolver.add_build_artifact( notation, opts[:path], opts[:pom] )
        else
          @resolver.add_build_artifact( notation, opts )
        end
        
      else
        raise "invalid build_artifacts format" 
      end
    end
  end
  
  def build_artifacts
    Naether::Java.convert_to_ruby_array( @resolver.getBuildArtifacts() )
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
      list = Naether::Java.convert_to_java_list( scopes )
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
  
  # Array of mixed dependencies.
  #  * Artifact notation in the format of groupId:artifactId:version or groupId:artifactId:type:version - 'junit:junit:4.7' 
  #  * Hash of a single artifaction notation => scope - { 'junit:junit:4.7' => 'test' }
  #  * Path to a local pom - 'lib/pom.xml'
  #  * Hash of a single path to a local pom => scope - { 'lib/pom.xml' => ['compile','test'] }
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
  def dependencies
    Naether::Java.convert_to_ruby_array( @resolver.getDependencies() )
  end
  
  # Get array of dependencies as notation
  def dependencies_notation
    Naether::Java.convert_to_ruby_array(@resolver.getDependenciesNotation(), true)
  end
  alias_method :dependenciesNotation, :dependencies_notation # some javaism snuck in
  
  # Hash of dependency paths
  def dependencies_path
    Naether::Java.convert_to_ruby_hash( @resolver.getDependenciesPath(), true )
  end
  
  # Convert dependencies to Classpath friendly string
  def dependencies_classpath()
    @resolver.getResolvedClassPath()
  end
  
  # Load dependencies to Classpath
  def load_dependencies_to_classpath
    jars = dependencies_classpath.split(":")
    Naether::Java.load_jars(jars)
    
    jars
  end
  
  # Resolve dependencies, finding related additional dependencies
  def resolve_dependencies( download_artifacts = true, properties = nil )
    
    if properties
       # Convert to HashMap
       map = Naether::Java.create( "java.util.HashMap" )
       properties.each do |k,v|
         map.put( k, v )
       end
    end
    
    @resolver.resolveDependencies( download_artifacts, map );
    dependenciesNotation
  end

  def to_local_paths( notations ) 
    if Naether.platform == 'java'
      Naether::Java.convert_to_ruby_array( 
        Naether::Java.exec_static_method( 
          'com.tobedevoured.naether.util.Notation', 
          'getLocalPaths', 
          [local_repo_path, notations ], 
          ['java.lang.String', 'java.util.List'] ) )
    else
      paths =  Naether::Java.exec_static_method( 
        'com.tobedevoured.naether.util.Notation', 
        'getLocalPaths', 
        [local_repo_path, Naether::Java.convert_to_java_list(notations) ], 
        ['java.lang.String', 'java.util.List'] ) 
      Naether::Java.convert_to_ruby_array( paths, true )
    end
    
  end

  def download_artifacts( notations )
    if( notations.is_a? String )
      notations = [notations]
    end
    
    files = nil
    if Naether.platform == 'java'
      files = @resolver.downloadArtifacts( notations )
    else
      list = Naether::Java.convert_to_java_list( notations )
      files = @resolver._invoke('downloadArtifacts', 'Ljava.util.List;', list)
    end
    
    paths = []
    Naether::Java.convert_to_ruby_array(files).each do |file|
      paths << file.getAbsolutePath();
    end
    
    paths
  end
  
  
  # Deploy artifact to remote repo url
  def deploy_artifact( notation, file_path, url, opts = {} )
    artifact = Naether::Java.create( "com.tobedevoured.naether.deploy.DeployArtifact" )
    
    artifact.setRemoteRepo( url )
    artifact.setNotation( notation )
    artifact.setFilePath( file_path )
    if opts[:pom_path]
      artifact.setPomPath( opts[:pom_path] )
    end
    
    if opts[:username] || opts[:pub_key]
      artifact.setAuth(opts[:username], opts[:password], opts[:pub_key], opts[:pub_key_passphrase] )
    end
    if Naether.platform == 'java'
      @resolver.deployArtifact(artifact)
    else
      @resolver._invoke( 'deployArtifact', 'Lcom.tobedevoured.naether.deploy.DeployArtifact;', artifact )
    end
  end
  
  # Install artifact or pom to local repo, must specify pom_path and/or jar_path
  def install( notation, pom_path =nil, jar_path = nil )
    @resolver.install(notation, pom_path, jar_path)
  end
  
  def load_pom( file_path )
    @project_instance = Naether::Java.create("com.tobedevoured.naether.maven.Project", file_path )
  end
  
  def pom_dependencies( file_path=nil, scopes = nil)
    if file_path
      load_pom( file_path )
    end

    if Naether.platform == 'java'
      if scopes.nil?
        deps = @project_instance.getDependenciesNotation()
      else
        deps = @project_instance.getDependenciesNotation( scopes )
      end
      
    else
      list = nil
      if scopes
        list = Naether::Java.convert_to_java_list( scopes )
        
        deps = @project_instance._invoke('getDependenciesNotation', 'Ljava.util.List;', list)
      else
        deps = @project_instance.getDependenciesNotation()
      end
      
    end
    
    Naether::Java.convert_to_ruby_array(@project_instance.getRepositoryUrls()).each do |url|
      add_remote_repository( url )
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
  
  # Create the XML for a Maven Pom for the notation, groupId:artifactId:type:version
  #
  # loads all resolved dependencies into pom
  def build_pom( notation )
    @project_instance = Naether::Java.create("com.tobedevoured.naether.maven.Project")
    @project_instance.setProjectNotation( notation )
    
    @project_instance.setRepositories( @resolver.getRemoteRepositoryUrls() )
      
    @project_instance.setDependencies( @resolver.getDependencies() )
    
    @project_instance.toXml()
    
  end
  
  # notation of the pom, groupId:artifactId:type:version
  # filePath to write the pom 
  #
  # loads all resolved dependencies into pom
  def write_pom( notation, file_path )
    @project_instance = Naether::Java.create("com.tobedevoured.naether.maven.Project")
    @project_instance.setProjectNotation( notation )
    
    @project_instance.setDependencies( @resolver.getDependencies() )
    
    @project_instance.writePom( file_path )
    
  end

  def set_log_level( level )
    Naether::Java.exec_static_method('com.tobedevoured.naether.util.LogUtil', 'changeLevel', ['com.tobedevoured', level] )
  end
end
