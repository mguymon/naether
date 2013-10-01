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

require "#{File.dirname(__FILE__)}/java"

module Naether
  
  # Helper for interacting with a Maven POM
  #
  # @author Michael Guymon
  #
  class Maven
    
    class << self
      
      # Create an instance from a POM
      #
      # @param [String] pom_path
      # @return [Naether::Maven]
      def create_from_pom( pom_path )
        maven = Maven.new( pom_path )
      end
      
      # Create an instance based on notation
      # 
      # @param [String] notation
      # @return [Naether::Maven]
      # @see https://github.com/mguymon/naether/wiki/Notations
      def create_from_notation( notation )
        maven = Maven.new
        maven.notation = notation
        maven
      end
    end
    
    #
    # Create new instance
    #
    def initialize(pom_path = nil)
      if pom_path
        @project = Naether::Java.create("com.tobedevoured.naether.maven.Project", pom_path )
      else
        @project = Naether::Java.create("com.tobedevoured.naether.maven.Project" )
      end
      
    end
    
    # Set the Notation for this Project
    #
    # @param [String] notation
    # @see https://github.com/mguymon/naether/wiki/Notations
    def notation=(notation)
      @project.setProjectNotation( notation )
    end
    
    # Get dependences for Project as notations
    # 
    # @param [Array<String>] scopes valid options are compile,test,runtime
    def dependencies( scopes = nil)
      if scopes
        unless scopes.is_a? Array
           scopes = [scopes]
        end
      end
          
      if Naether.platform == 'java'
        if scopes.nil?
          deps = @project.getDependenciesNotation()
        else
          deps = @project.getDependenciesNotation( scopes )
        end
        
      elsif scopes
        list = Naether::Java.convert_to_java_list( scopes )

        deps = @project._invoke('getDependenciesNotation', 'Ljava.util.List;', list)
      else
        deps = @project.getDependenciesNotation()
      end

      
      Naether::Java.convert_to_ruby_array( deps, true )
    end
    
    # Add dependency by scope
    #
    # @param [String] notation
    # @param [String] scope
    #
    def add_dependency( notation, scope = nil )
      @project.addDependency( notation, scope )
    end
      
    
    # Set dependencies
    #
    # @param [Array] dependencies
    def dependencies=(dependencies)
      @project.setDependencies( dependencies )
    end
    
    # Set repositories
    #
    # @param [Array] repositories of urls
    def repositories=( repositories )
      @project.setRepositories( repositories )
    end
  
    # Get the version
    #
    # return [String] version
    def version()
      return @project.getVersion()
    end
    
    # Load dependencies and remote repo from a {Naether} instance
    def load_naether( naether )
      self.dependencies= naether.resolver.currentDependencies()
      self.repositories= naether.resolver.getRemoteRepositoryUrls()
    end
    
    # Create the XML for a Maven Pom
    #
    # @return [String] pom xml
    def build_pom()
      @project.toXml()
    end
    
  
    # Write Maven Pom
    #
    # @param [String] file_path
    def write_pom( file_path )
      @project.writePom( file_path )
    end

    def invoke( *opts )
      #defaults
      config = {
        :maven_home => ENV['maven.home'] || '/usr/share/maven',
        :local_repo => File.expand_path('~/.m2/repository')
      }

      if opts.last.is_a? Hash
        config = defaults.merge( opts.pop )
      end
      goals = opts

      pom = @project.getPomFile().getAbsolutePath()

      invoker = Naether::Java.create("com.tobedevoured.naether.maven.Invoker", config[:local_repo], config[:maven_home] )
      invoker.execute( pom, Naether::Java.convert_to_java_list(goals) )
    end
  end
end