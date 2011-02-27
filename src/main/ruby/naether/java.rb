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
    
    # Jars loaded
    def self.loaded_jars
      if Naether.platform == 'java'
        Naether::Java::JRuby.instance.loaded_jars
      else
        Naether::Java::Ruby.instance.loaded_jars
      end
    end
    
    #
    #
    #
    def self.load_jars_dir(paths)
      unless paths.is_a? Array
        paths = [paths]
      end
      
      jars = []
      paths.each do |path|
        jars = jars + Dir.glob( "#{File.expand_path(path)}/*.jar")
      end
      
      Naether::Java.load_jars(jars)
      
    end
    
    #
    # Load jars
    #
    def self.load_jars(jars)
      if Naether.platform == 'java'
        Naether::Java::JRuby.instance.load_jars(jars)
      else
        Naether::Java::Ruby.instance.load_jars(jars)
      end
    end
    
    #
    # Handle loading for JRuby
    #
    class JRuby
      include Singleton
      
      attr_reader :loaded_jars
      
      def initialize
        require 'java'
        
        @loaded_jars = []
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
      
      def load_jars(jars)
        loaded_jars = []
        unless jars.is_a?( Array )
          jars = [jars]
        end
        
        jars.each do |jar|
          expanded_jar = File.expand_path(jar)
          if !@loaded_jars.include? expanded_jar
            loaded_jars << expanded_jar
            @loaded_jars << expanded_jar
          end
        end
        
        unless loaded_jars.empty?
          Rjb::add_jar( loaded_jars )
        end
        
        loaded_jars
      end
    end
  end
end