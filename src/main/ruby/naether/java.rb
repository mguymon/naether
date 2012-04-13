require 'singleton'

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
    
    def initialize
      if Naether.platform == 'java'
        @java = Naether::Java::JRuby.instance
      else
        @java = Naether::Java::Ruby.instance
      end
    end
    
    # Jars loaded
    def self.loaded_jars
      instance.java.loaded_jars
    end
    
  
    # Loads all jars from the array of paths
    def self.load_jars_dir(paths)
      unless paths.is_a? Array
        paths = [paths]
      end
      
      jars = []
      paths.each do |path|
        jars = jars + Dir.glob( "#{File.expand_path(path)}/*.jar", File::FNM_CASEFOLD)
      end
      
      Naether::Java.load_jars(jars)
      
    end
    
    # Load jars for the runtime platform
    def self.load_jars(jars)
      instance.java.load_jars(jars)
    end
    
    def self.set_log_level( level )
      instance.java.set_log_level( level )
    end
    
    def self.create( java_class, *args )
      instance.java.create( java_class, *args )
    end
    
    def self.convert_to_ruby_array( java_array, to_string = false )
      instance.java.convert_to_ruby_array( java_array, to_string )
    end
    
    def self.convert_to_ruby_hash( java_hash, to_string = false )
      instance.java.convert_to_ruby_hash( java_hash, to_string )
    end
    
    #
    # Handle loading jars for JRuby
    #
    class JRuby
      include Singleton
      
      attr_reader :loaded_jars
      
      def initialize
        require 'java'
        
        @loaded_jars = []
      end
      
      def create( java_class, *args )
        java_class = eval(java_class)
        java_class.new( *args )
      end
      
      def set_log_level( level )
        com.slackworks.naether.LogUtil.changeLevel( 'com.slackworks', level )
      end
      
      def load_jars(jars)
        loaded_jars = []
        unless jars.is_a? Array
          jars = [jars]
        end
        
        jars.each do |jar|
          expanded_jar = File.expand_path(jar)
          if !@loaded_jars.include? expanded_jar
            require expanded_jar
            loaded_jars << expanded_jar
            @loaded_jars << expanded_jar
          end
        end
        
        loaded_jars
      end
      
      def convert_to_ruby_array( java_array, to_string = false )
        java_array.to_a
      end
      
      def convert_to_ruby_hash( java_hash, to_string = false )
        java_hash.to_hash
      end
    
    end
    
    #
    # Handle loading jars for Ruby via Rjb
    #
    class Ruby
      include Singleton
      
      attr_reader :loaded_jars
      
      def initialize()
        require 'rjb' 
        
        # Call Rjb::load with an empty classpath, incase Rjb::load has already been called
        java_opts = (ENV['JAVA_OPTS'] || ENV['JAVA_OPTIONS']).to_s.split
        Rjb::load("", java_opts)
        
        @loaded_jars = []
      end
      
      def create( java_class, *args )
        java_class = Rjb::import(java_class) 
        java_class.new( *args )
      end
      
      def set_log_level( level )
        Rjb::import('com.slackworks.naether.LogUtil').changeLevel( 'com.slackworks', level )
      end
      
      def load_jars(jars)
        loadable_jars = []
        unless jars.is_a?( Array )
          jars = [jars]
        end
        
        jars.each do |jar|
          expanded_jar = File.expand_path(jar)
          if !@loaded_jars.include? expanded_jar
            loadable_jars << expanded_jar
            @loaded_jars << expanded_jar
          end
        end
        
        unless loadable_jars.empty?
          Rjb::add_jar( loadable_jars )
        end
        
        loadable_jars
      end
      
      def convert_to_ruby_array( java_array, to_string = false )
        ruby_array = java_array.toArray()
        
        if to_string
          ruby_array = ruby_array.map { |x| x.toString()}
        end
        
        ruby_array
      end
      
      def convert_to_ruby_hash( java_hash, to_string = false )
        
        hash = {}
        keys = java_hash.keySet()
        iterator = keys.iterator()
        if to_string
          while iterator.hasNext()
            key = iterator.next().toString()
            hash[key] = java_hash.get( key ).toString()
          end
        else
          while iterator.hasNext()
            key = iterator.next()
            hash[key] = java_hash.get( key )              
          end
        end
        
        hash
        
      end
    end
  end
end