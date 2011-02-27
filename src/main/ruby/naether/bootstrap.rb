require "#{File.dirname(__FILE__)}/classpath"

class Naether
  class Bootstrap
    class << self
      attr_accessor :naether_jar_lib
      
      def dependencies( jar_path = nil )
        Naether::Classpath.load_jars( jar_path || Naether::JAR_LIB )
        deps = nil
        
        if Naether.platform == 'java'
          deps = com.slackworks.Bootstrap.dependencies
        else
          bootstrap = Rjb::Import('com.slackworks.Bootstrap')
          deps = bootstrap.dependencies
        end   
        
        deps.to_a
      end
    end
  end
end