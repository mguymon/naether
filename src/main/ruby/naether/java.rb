require 'singleton'
require "#{File.dirname(__FILE__)}/configuration"

# :title:Naether::Java
#
# Handles loading jars. Determines correct platform to use, Naether::Java::JRuby
# or Naether::Java::Ruby
#
# = Authors
# Michael Guymon
#
class Naether
  class Java
    include Singleton
    
    attr_reader :java
    
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
    
    # Paths loaded
    def self.loaded_paths
      instance.java.loaded_paths
    end
    
    # Load paths for the runtime platform
    def self.load_paths(paths)
      instance.java.load_paths(paths)
    end
    
    # Load paths for the runtime platform
    def self.internal_load_paths(paths)
      instance.java.internal_load_paths(paths)
    end
    
    def self.create( target_class, *args )
      instance.java.create( target_class, *args )
    end
    
    def self.exec_static_method( target_class, target_method, *args ) 
      instance.java.exec_static_method( target_class, target_method, *args )
    end
    
    def self.convert_to_java_list( ruby_array )
      instance.java.convert_to_java_list( ruby_array )
    end
    
    def self.convert_to_ruby_array( java_array, to_string = false )
      instance.java.convert_to_ruby_array( java_array, to_string )
    end
    
    def self.convert_to_ruby_hash( java_hash, to_string = false )
      instance.java.convert_to_ruby_hash( java_hash, to_string )
    end
    
  end
end