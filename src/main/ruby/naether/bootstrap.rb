require "#{File.dirname(__FILE__)}/java"
require 'yaml'

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
      
      # Find naether jar relative to the gem install
      def naether_jar
        Dir.glob(File.expand_path("#{File.dirname(__FILE__)}/../../naether*.jar")).first
      end
      
      def write_dependencies( jar_path = nil, dest = 'jar_dependencies.yml' )
        Naether::Java.load_jars_dir( jar_path || Naether::JAR_LIB )
        deps = {};
        if Naether.platform == 'java'
          deps[:dependencies] = com.slackworks.Bootstrap.dependencies.to_a
        else
          bootstrap = Rjb::import('com.slackworks.Bootstrap')
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
          dep_file = "#{File.dirname( __FILE__ )}../../jar_dependencies.yml"
        end
        
        dep = YAML.load_file( dep_file )  
        @@dependencies = dep[:dependencies]
      end
    end
  end
end