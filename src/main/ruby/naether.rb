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
  
  class << self
    # Helper to determine the platform
    def platform
      $platform || RUBY_PLATFORM[/java/] || 'ruby'
    end
    
    # Java dependencies needed to bootstrap Naether
    def bootstrap_dependencies( dep_file=nil )
      Naether::Bootstrap.dependencies( dep_file )
    end
    
    def create_from_paths( deps_jar_dir, naether_jar_dir = nil )
      naether_jar_dir = naether_jar_dir || JAR_LIB
      Naether::Java.load_jars_dir( [deps_jar_dir, naether_jar_dir] )
      
      Naether.new
    end
    
    def create_from_jars( jars )
      Naether::Java.load_jars( jars )
      
      Naether.new
    end
  end
  
  #
  # Create new instance
  #
  def initialize()
    
    if Naether.platform == 'java'
      @resolver = com.slackworks.Naether.new 
    else
      naetherClass = Rjb::import('com.slackworks.Naether') 
      @resolver = naetherClass.new
    end
  end
  
  def clear_remote_repositories
    @resolver.clearRemoteRepositories()
  end
  
  # Add remote repository
  def add_remote_repository( url )
    @resolver.addRemoteRepository( url )
  end
  
  def remote_repositories
    @resolver.getRemoteRepositories()
  end
  
  # Add a dependency in the notation: groupId:artifactId:type:version
  def add_dependency( notation )
    @resolver.addDependency( notation )
  end
  
  # Set array of dependencies in the notation: groupId:artifactId:type:version
  def dependencies=(dependencies)
    dependencies.each do |dependent|
      add_dependency( dependent )  
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
  
  # Resolve dependencies, finding related additional dependencies
  def resolve_dependencies
    @resolver.resolveDependencies();
    dependencies
  end
end