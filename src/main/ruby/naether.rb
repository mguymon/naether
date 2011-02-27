require "#{File.dirname(__FILE__)}/naether/bootstrap"
require "#{File.dirname(__FILE__)}/naether/java"

class Naether
  JAR_LIB = "#{File.dirname(__FILE__)}/.."
  
  def self.platform
    platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
  end
  
  def self.bootstrap_dependencies( jar_path = nil )
    Naether::Bootstrap.dependencies( jar_path )
  end
  
  def initialize( deps_jar_path, naether_jar_path = nil )
    naether_jar_path = naether_jar_path || JAR_LIB
    Naether::Java.load_jar_dirs( [deps_jar_path,naether_jar_path] )
    
    if Naether.platform == 'java'
      @resolver = com.slackworks.Naether.new 
    else
      naetherClass = Rjb::import('com.slackworks.Naether') 
      @resolver = naetherClass.new
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
    if Naether.platform == 'java'
      return @resolver.getDependenciesNotation().to_a
    else
      bootstrap = Rjb::import('com.slackworks.Bootstrap')
      return @resolver.getDependenciesNotation().toArray().map{ |dep| dep.toString() }
    end 
  end
end