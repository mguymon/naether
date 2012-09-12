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

module Naether
  
  # Helper for interacting with a Maven POM
  #
  # @author Michael Guymon
  #
  class Maven
    
    #
    # Create new instance
    #
    # @param [String] notation_or_file_path
    def initialize( notation_or_file_path )
      if notation_or_file_path.count(":") >= 3
        @project_instance = Naether::Java.create("com.tobedevoured.naether.maven.Project" )
        @project_instance.setProjectNotation( notation_or_file_path )
      else
        @project_instance = Naether::Java.create("com.tobedevoured.naether.maven.Project", notation_or_file_path )
      end      
    end
    
    # Get dependences for Project as notations
    # 
    # @param [Array] scopes valid options are compile,test,runtime
    # @see {https://github.com/mguymon/naether/wiki/Notations}
    def dependencies( scopes = nil)
  
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
    
    # Set dependencies
    #
    # @param [Array] dependencies
    def dependencies=(dependencies)
      @project_instance.setDependencies( dependencies )
    end
    
    # Set repositories
    #
    # @param [Array] repositories of urls
    def repositories=( repositories )
      @project_instance.setRepositories( repositories )
    end
  
    # Get the version
    #
    # return [String] version
    def version()
      return @project_instance.getVersion()
    end
    
    # Create the XML for a Maven Pom
    #
    # @return [String] pom xml
    def build_pom()
      @project_instance.toXml()
    end
    
  
    # Write Maven Pom
    #
    # @param [String] file_path
    def write_pom( file_path )
      @project_instance.writePom( file_path )
    end
  end
end