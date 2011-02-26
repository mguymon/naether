require 'java'
Dir.glob("#{File.dirname(__FILE__)}/../*.jar") do |jar|
  require jar
end

class Naether
  
  def self.bootstrap_jars
    Dir.glob("#{File.dirname(__FILE__)}/../*.jar") 
  end
  
  def self.bootstrap_dependencies
    include_class com.slackworks.Bootstrap
    
    Bootstrap.dependencies
  end
end