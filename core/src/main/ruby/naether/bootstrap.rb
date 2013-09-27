require "#{File.dirname(__FILE__)}/java"
require "#{File.dirname(__FILE__)}/configuration"
require 'yaml'
require 'open-uri'
require 'fileutils'

module Naether
  
  #
  # Helper for bootstrapping Naether
  #
  # @author Michael Guymon
  #
  class Bootstrap
    
    @@dependencies = nil
    
    class << self
      
      # Default local repo of ENV['M2_REPO'] or ~/.m2/repository
      #
      # @return [String]
      def default_local_repo
        ENV['M2_REPO'] || File.expand_path('~/.m2/repository')
      end
      
      # Write bootstrap dependencies to yaml file
      def write_dependencies( dest = 'jar_dependencies.yml' )
        deps = {};
        if Naether::Configuration.platform == 'java'
          deps[:dependencies] = com.tobedevoured.naether.Bootstrap.dependencies.to_a
        else
          bootstrap = Rjb::import('com.tobedevoured.naether.Bootstrap')
          deps[:dependencies] = bootstrap.dependencies.toArray().map{ |dep| dep.toString() }
        end  
        
        File.open( dest, 'w' ) do |out|
          YAML.dump( deps, out )
        end
      end
      
      # List of Java dependencies for Naether from yaml dependency file. Caches
      # result after first run.
      # 
      # @param [String] dep_file path, defaults to Naether::Configuration.dependencies_yml
      # @return [List]
      def dependencies( dep_file=nil )
        
        if @@dependencies
          return @@dependencies
        end
        
        if dep_file.nil?
          dep_file = Naether::Configuration.dependencies_yml
        end
        
        dep = YAML.load_file( dep_file )  
        @@dependencies = dep[:dependencies]
      end
      

      #
      # Bootstrap the local repo by downloading Naether's dependencies
      # @param [String] local_repo defaults to #default_local_repo
      # @param [hash] opts
      #
      def bootstrap_local_repo(local_repo = nil, opts = {} )
        local_repo = local_repo || default_local_repo
        
        opts[:local_repo] = local_repo
        
        temp_naether_dir = File.join( local_repo, ".naether" )
        
        
        deps = download_dependencies( temp_naether_dir, opts )
        
        jars = (deps[:exists] + deps[:downloaded]).map {|jar| jar.values.first }
          
        jars = Naether::Java.internal_load_paths( jars )
        
        if ( deps[:downloaded].size > 0)
          install_dependencies_to_local_repo( deps[:downloaded], jars, opts )
        end
        
        #raise Naether::Java.internal_loaded_paths.inspect
        
      end
      
      #
      # Download Naether dependencies
      #
      # @param [String] dest to download dependencies t
      # @param [Hash] opts
      # @return [Hash] with status of missing, downloaded, exists dependencies
      def download_dependencies( dest, opts = {} )
         
        if !File.exists? dest
          FileUtils.mkdir_p( dest )
        end
        
        deps = {}
          
        if opts[:deps]
          deps[:missing] = opts[:deps] 
        else
          deps = check_local_repo_for_deps( opts[:local_repo], opts )
        end
        
        deps[:downloaded] = []
          
        if deps[:missing].size > 0
            
          deps[:missing].each do |dep|
            notation = dep.split(":")
            group = notation[0].gsub("\.", File::SEPARATOR)
            artifact = notation[1]
            type = notation[2]
            version = notation[3]
            
            jar = "#{artifact}-#{version}.#{type}"
            
            maven_path = "#{dest}#{File::SEPARATOR}#{jar}"
            
            if !File.exists? maven_path
              maven_mirror = "http://repo1.maven.org/maven2/#{group}/#{artifact}/#{version}/#{jar}"
              
              open(maven_path, 'wb') do |io|
                io.print open(maven_mirror).read
              end
              
              deps[:downloaded] << { dep => maven_path }
            else
              deps[:downloaded] << { dep => maven_path }
            end
          end
        end
        
        deps
      end
      
      #
      # Check local_repo for Naether dependencies
      # 
      # @param [String] local_repo
      # @param [Hash] opts
      # @return [Hash] with status of missing, downloaded, exists dependencies
      def check_local_repo_for_deps(local_repo = nil, opts = {} )
        
        local_repo = local_repo || default_local_repo
        local_repo = File.expand_path(local_repo)
        
        #puts "Checking #{local_repo} for jars to bootstrap Naether"
        
        deps = {:exists => [], :missing => [], :downloaded => [] }
        
        dependencies( opts[:dep_yaml] ).each do |dep|
          notation = dep.split(":")
          group = notation[0].gsub("\.", File::SEPARATOR)
          artifact = notation[1].gsub("\.", File::SEPARATOR)
          type = notation[2]
          version = notation[3]
          
          jar = "#{artifact}-#{version}.#{type}"
          
          maven_path = "#{local_repo}#{File::SEPARATOR}#{group}#{File::SEPARATOR}#{artifact}#{File::SEPARATOR}#{version}#{File::SEPARATOR}#{jar}"
          
          if File.exists? maven_path
            deps[:exists] << { dep => maven_path }
          else
            deps[:missing] << dep
          end
          
        end  
        deps
      end
      
      #
      # Install Naether Dependencies to local_repo
      # 
      # @param [Array<String>] install_jars 
      # @param [Array<String>] naether_jars to bootstrap Naether. These may overlap with install_jars.
      # @param [Hash] opts
      # @return [Naether]
      #
      def install_dependencies_to_local_repo( install_jars, naether_jars, opts = {}  )
          
        require "#{File.dirname(__FILE__)}/../naether"
        
        @naether = Naether.create_from_jars( naether_jars )
        
        if opts[:local_repo]
          @naether.local_repo_path = opts[:local_repo]
        end
        
        install_jars.each do |dep|
          notation, path = dep.to_a.first
          @naether.install( notation, nil, path )
        end
        
        @naether
      end
    end
  end
end