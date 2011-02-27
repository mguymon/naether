require 'singleton'

class Naether
  class Classpath
    
    def self.load_jars(jar_paths)
      if Naether.platform == 'java'
        Naether::Classpath::JRuby.instance.load_jars(jar_paths)
      else
        Naether::Classpath::Ruby.instance.load_jars(jar_paths)
      end
    end
    
    class JRuby
      include Singleton

      def initialize
        require 'java'
        
        @loaded_jars = []
      end

      def load_jars(jar_paths)
        
        unless jar_paths.is_a? Array
          jar_paths = [jar_paths]
        end
        
        jar_paths.each do |jar_path|
          Dir.glob("#{File.expand_path(jar_path)}/*.jar") do |jar|
            if !@loaded_jars.include? jar
              require jar
              @loaded_jars << jar
            end
          end
        end
      end
    end
    
    class Ruby
      include Singleton
      
      def initialize()
        require 'rjb' 
        java_opts = (ENV['JAVA_OPTS'] || ENV['JAVA_OPTIONS']).to_s.split
        Rjb::load([], java_opts)
        
        @loaded_jars = []
      end
      
      def load_jars(jar_paths)
        unless jar_paths.is_a?( Array )
          jar_paths = [jar_paths]
        end
        
        jar_paths.each do |jar_path|
          Dir.glob("#{File.expand_path(jar_path)}/*.jar").each do |jar|
            if !@loaded_jars.include? jar
              Rjb::add_jar( jar )
              @loaded_jars << jar
            end
          end
        end
      end
    end
  end
end