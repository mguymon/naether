require 'singleton'
require "#{File.dirname(__FILE__)}/configuration"

#
# Handles loading jars using the correct platform, Naether::Java::JRuby
# or Naether::Java::Ruby
#
# @author Michael Guymon
#
class Naether
  class Java
    include Singleton
    
    attr_reader :java
    
    #
    # Creates new instance by loading the Naether jar to the parent ClassLoader
    # and creating the internal Naether ClassLoader
    #
    def initialize()
      naether_jar = Naether::Configuration.naether_jar
      
      unless File.exists? naether_jar
        raise "Cannot create Naether::Java: #{naether_jar} does not exist"
      end
      
      if Naether::Configuration.platform == 'java'
        require "#{File.dirname(__FILE__)}/java/jruby"
        @java = Naether::Java::JRuby.new
      else
        require "#{File.dirname(__FILE__)}/java/ruby"
        @java = Naether::Java::Ruby.new
      end
    end
    
    #
    # Paths loaded
    #
    # @result [Array] of String paths
    #
    def self.loaded_paths
      instance.java.loaded_paths
    end
    
    #
    # Load a path onto the parent ClassLoader
    #
    # @param [Array] paths as an Array of String paths or a String path
    #
    def self.load_paths(paths)
      instance.java.load_paths(paths)
    end
    
    #
    # Load a path into the internal Naether ClassLoader
    #
    # @param [Array] paths as an Array of String paths or a String path
    #
    def self.internal_load_paths(paths)
      instance.java.internal_load_paths(paths)
    end
 
    #
    # Create a Java Object from the Naether Class Loader
    #
    # @param [String] target_class to create
    # @param [Array] args Array of constructor arguments   
    def self.create( target_class, *args )
      instance.java.create( target_class, *args )
    end
    
    #
    # Execute a Staic method on a Java class from the Naether Class Loader
    #
    # @param [String] target_class
    # @param [String] target_method
    # @param [Array] params Array of method parameters
    # @param [Array] types if defined, a Array of String classes of params type that lines up with params one to one.
    # @result [Object]
    def self.exec_static_method( target_class, target_method, params, types = nil ) 
      instance.java.exec_static_method( target_class, target_method, params, types )
    end
    
    #
    # Convert a Ruby Array to a java.util.ArrayList
    # 
    # @param [Array] to convert to Java.util.ArrayList
    # @return [java.util.ArrayList]
    #    
    def self.convert_to_java_list( ruby_array )
      instance.java.convert_to_java_list( ruby_array )
    end
    
    #
    # Convert a java,util.List to a Ruby Array
    #
    # @param [java.util.ArrayList] java_array
    # @param [Boolean] to_string platform dependent helper
    # @return [Array]
    #
    def self.convert_to_ruby_array( java_array, to_string = false )
      instance.java.convert_to_ruby_array( java_array, to_string )
    end

    #
    # Convert a java.util.Map to a Ruby Hash
    #
    # @param [java.util.Map] java_hash
    # @param [Boolean] to_string platform dependent helper
    # @return [Hash]
    #
    def self.convert_to_ruby_hash( java_hash, to_string = false )
      instance.java.convert_to_ruby_hash( java_hash, to_string )
    end
    
  end
end