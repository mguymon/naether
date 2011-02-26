require 'java'
Dir.glob("#{File.dirname(__FILE__)}/../*.jar") do |jar|
  require jar
end

class Naether
  
  def self.bootstrap_jars
    Dir.glob("#{File.dirname(__FILE__)}/../*.jar") 
  end
  
  def self.bootstrap_dependencies
    com.slackworks.Bootstrap.dependencies
  end
  
  def initialize
    @resolver = com.slackworks.Naether.new  
  end
  
  def add_dependency( notation )
    @resolver.add_dependency( notation )
  end
  
  def resolve_dependencies
    @resolver.resolve_dependencies();
    dependencies
  end
  
  def dependencies=(dependencies)
    dependencies.each do |dependent|
      add_dependency( dependent )  
    end
  end
  
  def dependencies
    @resolver.get_dependencies_notation.to_a
  end
end