require "#{File.dirname(__FILE__)}/naether/bootstrap"
require "#{File.dirname(__FILE__)}/naether/java"

# :title:Naether
#
# Java dependency resolver using Maven's Aether. 
#
# = Authors
# Michael Guymon
#
class Naether
  
  # Naether jar path will default to packaged in the gem
  JAR_LIB = "#{File.dirname(__FILE__)}/.."
  
  # Helper to determine the platform
  def self.platform
    platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
  end
  
  # Java dependencies needed to bootstrap Naether
  def self.bootstrap_dependencies( jar_path = nil )
    Naether::Bootstrap.dependencies( jar_path )
  end
  
  #
  # Create new instance
  #
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
  
  #
  # Add a dependency in the notation: groupId:artifactId:type:version
  #
  def add_dependency( notation )
    @resolver.addDependency( notation )
  end
  
  #
  # Resolve dependencies, finding related additional dependencies
  #
  def resolve_dependencies
    @resolver.resolveDependencies();
    dependencies
  end
  
  #
  # Set array of dependencies in the notation: groupId:artifactId:type:version
  #
  def dependencies=(dependencies)
    dependencies.each do |dependent|
      add_ependency( dependent )  
    end
  end
  
  # Get dependencies
  def dependencies
    if Naether.platform == 'java'
      return @resolver.getDependenciesNotation().to_a
    else
      bootstrap = Rjb::import('com.slackworks.Bootstrap')
      return @resolver.getDependenciesNotation().toArray().map{ |dep| dep.toString() }
    end 
  end
end