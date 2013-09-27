require "#{File.dirname(__FILE__)}/../configuration"


# Handles Java interactions for JRuby. Providers helpers for 
# nomalizing accesss.

# @author Michael Guymon
#
module Naether
  class Java

    class JRuby
      
      attr_reader :loaded_paths, :class_loader
      
      #
      # Creates new instance by loading the Naether jar to the parent ClassLoader
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
      # @param [String] target_class to create
      # @param [Array] args Array of constructor arguments
      def create( target_class, *args )
        @class_loader.newInstance(target_class, *args )
      end
      
      #
      # Execute a Staic method on a Java class from the Naether Class Loader
      #
      # @param [String] target_class
      # @param [String] target_method
      # @param [Array] params Array of method parameters
      # @param [Array] types if defined, a Array of String classes of params type that lines up with params one to one.
      # @return [Object]
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
      # @param [Array] paths as an Array of String paths or a String path
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
      # Load a path onto the parent ClassLoader
      #
      # @param [Array] paths as an Array of String paths or a String path
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
      # Convert a Ruby Array to a java.util.ArrayList
      # 
      # @param [Array] ruby_array Array to convert to Java.util.ArrayList
      # @return [java.util.ArrayList]
      #
      def convert_to_java_list( ruby_array ) 
        list = java.util.ArrayList.new
        ruby_array.each do |item|
          list.add( item )
        end
        
        list
      end
      
      #
      # Convert a java,util.List to a Ruby Array
      #
      # @param [java.util.ArrayList] java_array
      # @param [Boolean] to_string has no affect on conversion.
      # @return [Array]
      #
      def convert_to_ruby_array( java_array, to_string = false )
        java_array.to_a
      end
      
      #
      # Convert a java.util.Map to a Ruby Hash
      #
      # @param [java.util.Map] java_hash
      # @param [Boolean] to_string has no affect on conversion
      # @return [Hash]
      #
      def convert_to_ruby_hash( java_hash, to_string = false )
        hash = java_hash.to_hash
        
        hash.each do |k,v|
          if v.is_a? java.util.Map
            hash[k] = convert_to_ruby_hash(v, to_string )
          end
        end
      end
    
    end    
  end
end