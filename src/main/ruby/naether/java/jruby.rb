require "#{File.dirname(__FILE__)}/../configuration"

# :title:Naether::Java::JRuby
#
# Singletn that handles Java interactions for JRuby. Providers helpers for 
# nomalizing accesss.

# = Authors
# Michael Guymon
#
class Naether
  class Java

    class JRuby
      
      attr_reader :loaded_paths, :class_loader
      
      #
      # Creates new instance by loading the Naether jar to the global ClassLoader
      # and creating the internal Naether ClassLoader
      #
      def initialize
        require 'java'
        
        naether_jar = Naether::Configuration.naether_jar
              
        @loaded_paths = []
        
        load_paths(naether_jar)  
        @class_loader = com.tobedevoured.naether.PathClassLoader.new()
        internal_load_paths(naether_jar)  
      end
      
      #
      # Create a Java Object from the Naether Class Loader
      #
      def create( target_class, *args )
        @class_loader.newInstance(target_class, *args )
      end
      
      #
      # Execute a Staic method on a Java class from the Naether Class Loader
      #
      def exec_static_method( target_class, target_method, params, types = nil ) 
        unless params.is_a? Array
          params = [params]
        end
        
        if types
          unless types.is_a? Array
             types = [types]
          end
        end
        
        @class_loader.execStaticMethod( target_class, target_method, params, types )
      end
      
      #
      # Load a path into the internal Naether ClassLoader
      #
      def internal_load_paths(paths)
        load_paths = []
        unless paths.is_a? Array
          paths = [paths]
        end
        
        paths.each do |path|
          expanded_path = File.expand_path(path)
          if File.exists?( expanded_path )
            @class_loader.addPath( expanded_path )
            
            load_paths << expanded_path
          end
        end
        
        load_paths
      end
      
      #
      # Load a path onto the Global ClassLoader
      #
      def load_paths(paths)
        load_paths = []
        unless paths.is_a? Array
          paths = [paths]
        end
        
        paths.each do |path|
          expanded_path = File.expand_path(path)
          if !@loaded_paths.include?( expanded_path ) && File.exists?( expanded_path )
            $CLASSPATH << expanded_path
            load_paths << expanded_path
            @loaded_paths << expanded_path
          end
        end
        
        load_paths
      end
      
      #
      # Convert a Ruby Array to a Java ArrayList
      #
      def convert_to_java_list( ruby_array ) 
        list = java.util.ArrayList.new
        ruby_array.each do |item|
          list.add( item )
        end
        
        list
      end
      
      #
      # Convert a Java List to a Ruby Array
      #
      def convert_to_ruby_array( java_array, to_string = false )
        java_array.to_a
      end
      
      #
      # Convert a Java Map to a Ruby Hash
      #
      def convert_to_ruby_hash( java_hash, to_string = false )
        java_hash.to_hash
      end
    
    end    
  end
end