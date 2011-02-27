require "#{File.dirname(__FILE__)}/naether/bootstrap"
require "#{File.dirname(__FILE__)}/naether/classpath"

class Naether
  JAR_LIB = "#{File.dirname(__FILE__)}/.."
  def self.platform
    platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
  end
  
  def self.bootstrap_dependencies( jar_path = nil )
    Naether::Bootstrap.dependencies( jar_path )
  end
  
  def initialize( jar_path )
    Naether::Classpath.load_jars(jar_path)
    
    if Naether.platform == 'java'
      @resolver = com.slackworks.Naether.new 
    else
      @resolver = Rjb::Import('com.slackworks.Naether.new') 
    end
  end
  
  def add_dependency( notation )
    @resolver.addDependency( notation )
  end
  
  def resolve_dependencies
    @resolver.resolveDependencies();
    dependencies
  end
  
  def dependencies=(dependencies)
    dependencies.each do |dependent|
      add_ependency( dependent )  
    end
  end
  
  def dependencies
    @resolver.get_dependencies_notation.to_a
  end
end