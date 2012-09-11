$:.unshift File.expand_path('.')
$:.unshift File.expand_path(File.join(File.dirname(__FILE__),'../../../'))
$:.unshift File.expand_path(File.join(File.dirname(__FILE__),'../../../src/main/ruby'))

puts File.expand_path(File.join(File.dirname(__FILE__),'../../../'))

puts "#########################################################"
puts " Make sure you run mvn package to have an update to date "
puts " naether jar for specs "
puts "#########################################################"


require 'rubygems'
require 'naether/configuration'
require 'rspec'
require 'naether/bootstrap'

Naether::Configuration.naether_jar = "target/naether-#{Naether::Configuration.version}.jar"
Naether::Bootstrap.bootstrap_local_repo(  
  File.expand_path( '../../../../target/test-repo', __FILE__ ), 
  :dep_yaml => File.expand_path( '../../../../jar_dependencies.yml', __FILE__ ) )
  
RSpec.configure do |config|
  
end

