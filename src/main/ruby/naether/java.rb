require 'singleton'

class Naether
  class Java
    
    def self.loaded_jars
      if Naether.platform == 'java'
        Naether::Java::JRuby.instance.loaded_jars
      else
        Naether::Java::Ruby.instance.loaded_jars
      end
    end
    
    def self.load_jar_dirs(jar_paths)
      if Naether.platform == 'java'
        Naether::Java::JRuby.instance.load_jar_dirs(jar_paths)
      else
        Naether::Java::Ruby.instance.load_jar_dirs(jar_paths)
      end
    end
    
    class JRuby
      include Singleton

      attr_reader :loaded_jars

      def initialize
        require 'java'
        
        @loaded_jars = []
      end

      def load_jar_dirs(jar_paths)
        loaded_jars = []
        unless jar_paths.is_a? Array
          jar_paths = [jar_paths]
        end
        
        jar_paths.each do |jar_path|
          Dir.glob("#{File.expand_path(jar_path)}/*.jar") do |jar|
            if !@loaded_jars.include? jar
              require jar
              loaded_jars << jar
              @loaded_jars << jar
            end
          end
        end
        
        loaded_jars
      end
    end
    
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
      
      def load_jar_dirs(jar_paths)
        loaded_jars = []
        unless jar_paths.is_a?( Array )
          jar_paths = [jar_paths]
        end
        
        jar_paths.each do |jar_path|
          Dir.glob("#{File.expand_path(jar_path)}/*.jar").each do |jar|
            if !@loaded_jars.include? jar
              loaded_jars << jar
              @loaded_jars << jar
            end
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