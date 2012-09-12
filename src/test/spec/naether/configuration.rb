require  File.expand_path(File.join(File.dirname(__FILE__), 'spec_helper'))
require 'src/main/ruby/naether/configuration'
require 'src/main/ruby/naether/version'

describe Naether::Configuration do
    it "should have naether jar" do
      Naether::Config.naether_jar.should match /naether-#{Naether::VERSION}.jar/
    end
    
    it "should have jar_dependencies.yml" do
      Naether::Config.dependencies_yml.should match /jar_dependencies\.yml/
    end
end