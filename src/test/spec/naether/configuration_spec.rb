require  File.expand_path(File.join(File.dirname(__FILE__), '../spec_helper'))
require 'src/main/ruby/naether/configuration'

describe Naether::Configuration do
    it "should have naether jar" do
      Naether::Configuration.naether_jar.should match /core-#{Naether::Configuration.version}.jar/
    end
    
    it "should have jar_dependencies.yml" do
      Naether::Configuration.dependencies_yml.should match /jar_dependencies\.yml/
    end
end