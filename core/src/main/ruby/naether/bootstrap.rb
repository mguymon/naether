require "#{File.dirname(__FILE__)}/../naether"
require "#{File.dirname(__FILE__)}/java"
require 'yaml'
require 'open-uri'
require 'fileutils'
        
class Naether
  # :title:Naether::Bootstrap
  #
  # Helper for bootstrapping Naether
  #
  # = Authors
  # Michael Guymon
  #
  class Bootstrap
    
    @@dependencies = nil
    
    class << self
      
      def default_local_repo
        ENV['M2_REPO'] || File.expand_path('~/.m2/repository')
      end
      
      # write bootstrap dependencies to yaml file
      def write_dependencies( dest = 'jar_dependencies.yml' )
        deps = {};
        if Naether.platform == 'java'
          deps[:dependencies] = com.tobedevoured.naether.Bootstrap.dependencies.to_a
        else
          bootstrap = Rjb::import('com.tobedevoured.naether.Bootstrap')
          deps[:dependencies] = bootstrap.dependencies.toArray().map{ |dep| dep.toString() }
        end  
        
        File.open( dest, 'w' ) do |out|
          YAML.dump( deps, out )
        end
      end
      
      # List of Java dependencies for Naether
      def dependencies( dep_file=nil )
        
        if @@dependencies
          return @@dependencies
        end
        
        if dep_file.nil?
          dep_file = "#{File.dirname( __FILE__ )}/../../jar_dependencies.yml"
        end
        
        dep = YAML.load_file( dep_file )  
        @@dependencies = dep[:dependencies]
      end
      

      def bootstrap_local_repo(local_repo = nil, opts = {} )
        local_repo = local_repo || default_local_repo
        
        opts[:local_repo] = local_repo
        
        temp_naether_dir = File.join( local_repo, ".naether" )
        
        
        deps = download_dependencies( temp_naether_dir, opts )
        
        jars = (deps[:exists] + deps[:downloaded]).map {|jar| jar.values.first }
          
        if ( deps[:downloaded].size > 0)
          install_dependencies_to_local_repo( deps[:downloaded], opts )
        else
          Naether::Java.internal_load_paths( jars )
        end
        
        #raise Naether::Java.instance.java.class_loader.getLoadedPaths().map { |x| x.toString() }.sort.inspect
        
      end
      
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
          puts "Downloading jars for Naether"  
            
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
              deps[:exists] << { dep => maven_path }
            end
          end
        end
        
        deps
      end
      
      def check_local_repo_for_deps(local_repo = nil, opts = {} )
        
        local_repo = local_repo || default_local_repo
        local_repo = File.expand_path(local_repo)
        
        #puts "Checking #{local_repo} for jars to bootstrap Naether"
        
        deps = {:exists => [], :missing => [] }
        
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
      
      def install_dependencies_to_local_repo( jars_or_dir, opts = {}  )
          
        @naether = nil
        jars = []  
        unless jars_or_dir.is_a? Array
          jars = Dir.glob( "#{jars_or_dir}#{File::SEPARATOR}*.jar" )
          
        else
          jars = jars_or_dir
        end
        
        @naether = Naether.create_from_jars( jars )
        
        if opts[:local_repo]
          @naether.local_repo_path = opts[:local_repo]
        end
        
        dependencies.each do |dep|
          notation = dep.split(":")
          group = notation[0].gsub("\.", File::SEPARATOR)
          artifact = notation[1].gsub("\.", File::SEPARATOR)
          type = notation[2]
          version = notation[3]
          
          name = "#{artifact}-#{version}.#{type}"
          
          jar = jars.select { |x| x =~ /#{name}/ }
          if jar.size > 0
            jar = jar[0]
            @naether.install( dep, nil, jar )
          end
          
        end
        
        @naether
      end
    end
  end
end