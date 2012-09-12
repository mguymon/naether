# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with this
# work for additional information regarding copyright ownership. The ASF
# licenses this file to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

require "#{File.dirname(__FILE__)}/naether/configuration"
require "#{File.dirname(__FILE__)}/naether/bootstrap"
require "#{File.dirname(__FILE__)}/naether/java"


# Java dependency resolver.
#
# @author Michael Guymon
#
class Naether
  
  attr_reader :resolver
  
  class << self
    
    # Helper for Java dependencies needed to bootstrap Naether
    # 
    # @param [String] dep_file path
    # @see {Naether::Bootstrap#dependencies}
    def bootstrap_dependencies( dep_file=nil )
      Naether::Bootstrap.dependencies( dep_file )
    end
    
    #
    # Helper for platform detection
    #
    def platform
      Naether::Configuration.platform
    end
    
    #
    # Loads all jars creates a new instance of Naether
    #
    # @param [Array<String>] jars of paths
    # @return [Naether]
    def create_from_jars( jars )
      Naether::Java.internal_load_paths( jars )
      Naether.new
    end
    
  end
  
  # Create new instance.
  def initialize
    @resolver = Naether::Java.create('com.tobedevoured.naether.Naether')
  end
  
  # Clear all remote repositories
  def clear_remote_repositories
    @resolver.clearRemoteRepositories()
  end
  
  # Add remote repository
  #
  # @param [String] url of remote repo
  # @param [String] username optional
  # @param [String] password optioanl 
  def add_remote_repository( url, username = nil, password = nil )
    if username
      @resolver.addRemoteRepositoryByUrl( url, username, password )
    else
      @resolver.addRemoteRepositoryByUrl( url )
    end
  end
  
  # Get remote repositories
  #
  # @return [Array] of remote repos
  def remote_repositories
    Naether::Java.convert_to_ruby_array(@resolver.getRemoteRepositories())
  end
  
  # Get remote repositories as urls
  #
  # @return [Array<String>] of String urls
  def remote_repository_urls
    Naether::Java.convert_to_ruby_array(@resolver.getRemoteRepositoryUrls(), true)
  end
  
  # Path to local maven repo
  #
  # @return [String] path to local repo
  def local_repo_path
    @resolver.getLocalRepoPath()
  end
  
  # Set path to local maven repo
  #
  # @param [String] path local repo
  def local_repo_path=( path )
    @resolver.setLocalRepoPath( path )
  end
  
  #
  # Add a local Build Artifact, that will be used in the Dependency Resolution
  #
  # @param [String] notation 
  # @param [String] path to artifact
  # @param [String] pom optional path to pom.xml
  #
  def add_build_artifact( notation, path, pom = nil )
    @resolver.addBuildArtifact(notation, path, pom )
  end
  
  #
  # Set local Build Artifacts, that will be used in the Dependency Resolution
  #
  # @param [Array<Hash>] artifacts of Hashs with { notation => path } or { notation => { :path => path, :pom => pom_path }
  #
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
  
  #
  # Get Build Artifacts
  #
  # @return [Array] of build artifacts
  #
  def build_artifacts
    Naether::Java.convert_to_ruby_array( @resolver.getBuildArtifacts() )
  end
  
  # Add a dependency 
  #
  # @param [String] notation in the format of groupId:artifactId:version
  # @param [String] scope valid options are compile,test,runtime
  # @see https://github.com/mguymon/naether/wiki/Notations
  def add_notation_dependency( notation, scope='compile' )
    @resolver.addDependency( notation, scope )
  end
  
  # Add dependencies from a Maven POM
  #
  # @param [String] pom_path
  # @param [Array<String>] scopes valid options are compile,test,runtime
  def add_pom_dependencies( pom_path, scopes=['compile'] )
    if Naether.platform == 'java'
      @resolver.addDependencies( pom_path, scopes )
    else
      list = Naether::Java.convert_to_java_list( scopes )
      @resolver._invoke( 'addDependencies', 'Ljava.lang.String;Ljava.util.List;', pom_path, list )
    end
  end
  
  # Add a dependency of org.sonatype.aether.graph.Dependency
  #
  # @param [org.sonatype.aether.graph.Dependency] dependency
  def add_dependency( dependency )
    #@resolver.addDependency( dependency )
    if Naether.platform == 'java'
      @resolver.addDependency( dependency )
    else
      @resolver._invoke('addDependency', 'Lorg.sonatype.aether.graph.Dependency;', dependency)
    end
  end
  
  # Set the dependencies
  # 
  # The dependencies param takes an [Array] of mixed dependencies:
  #  * [String] Artifact notation, such as groupId:artifactId:version, e.g. 'junit:junit:4.7' 
  #  * [Hash] of a single artifaction notation => scope - { 'junit:junit:4.7' => 'test' }
  #  * [String] path to a local pom - 'lib/pom.xml'
  #  * [Hash] of a single path to a local pom => scope - { 'lib/pom.xml' => ['compile','test'] }
  #
  # @param [Array] dependencies
  # @see https://github.com/mguymon/naether/wiki/Notations
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
  #
  # @return [Array]
  def dependencies
    Naether::Java.convert_to_ruby_array( @resolver.getDependencies() )
  end
  
  # Get array of dependencies as notation
  #
  # @return [Array<String>] of notations
  # @see https://github.com/mguymon/naether/wiki/Notations
  def dependencies_notation
    Naether::Java.convert_to_ruby_array(@resolver.getDependenciesNotation(), true)
  end
  
  # Hash of dependency paths
  #
  # @return [Array<Hash>] of { notation => path }
  def dependencies_path
    Naether::Java.convert_to_ruby_hash( @resolver.getDependenciesPath(), true )
  end
  
  # Convert dependencies to Classpath friendly string
  #
  # @return [String]
  def dependencies_classpath()
    @resolver.getResolvedClassPath()
  end
  
  # Load dependencies to Classpath
  def load_dependencies_to_classpath
    jars = dependencies_classpath.split(":")
    Naether::Java.load_jars(jars)
    
    jars
  end
  
  # Resolve dependencies
  #
  # @return [Array<String>] of notations
  # @see https://github.com/mguymon/naether/wiki/Notations
  def resolve_dependencies( download_artifacts = true, properties = nil )
    
    if properties
       # Convert to HashMap
       map = Naether::Java.create( "java.util.HashMap" )
       properties.each do |k,v|
         map.put( k, v )
       end
    end
    
    @resolver.resolveDependencies( download_artifacts, map );
    dependencies_notation
  end

  # Convert notations to local paths of artifacts
  # 
  # @param [Array<String>] notations
  # @return [Array<String>] of paths to artifacts
  # @see https://github.com/mguymon/naether/wiki/Notations
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
  
  # Download artifacts
  # 
  # @param [Array<String>] notations
  # @return [Array<String>] of paths of downloaded artifacts
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
  #
  # @param [String] notation
  # @param [String] file_path to artifact to deploy
  # @param [String] url to deploy to
  # @opts [Hash] opts
  # @option opts [String] :pom_path path to pom.xml
  # @option opts [String] :username for optional auth
  # @option opts [String] :password for optional auth
  # @option opts [String] :pub_key for optional auth
  # @option opts [String] :pub_key_passphrase for optional auth
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
  #
  # @param [String] notation
  # @param [String] pom_path
  # @param [String] jar_path
  def install( notation, pom_path =nil, jar_path = nil )
    @resolver.install(notation, pom_path, jar_path)
  end
  
  # Set Log level for Naether Java logging
  #
  # @param [String] level to debug, info, warn, or error
  def set_log_level( level )
    Naether::Java.exec_static_method('com.tobedevoured.naether.util.LogUtil', 'changeLevel', ['com.tobedevoured', level] )
  end
end
