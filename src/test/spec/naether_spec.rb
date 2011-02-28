require 'src/main/ruby/naether'

describe Naether do
  
  context "Class" do
    it "should have bootstrap dependencies" do
      Naether.bootstrap_dependencies( 'jar_dependencies.yml' ).should include "org.sonatype.aether:aether-util:jar:1.11"
    end
  end
  
  context "Instance" do
    before(:each) do
      @naether = Naether.create_from_paths("target/lib", 'target')
      @naether.should_not be_nil
    end
    
    it "should add a remote repository" do
      @naether.add_remote_repository("http://test.net:7011")
      @naether.remote_repositories[0].getId().should eql( "central" )
      @naether.remote_repositories[1].getId().should eql( "test.net-7011" )
    end
    
    it "should add a dependency" do
      @naether.add_dependency( "junit:junit:jar:4.8.2" )  
      @naether.dependencies.should eql ["junit:junit:jar:4.8.2"]
    end
    
    it "should set a list of dependencies" do
      @naether.dependencies = [ "junit:junit:jar:4.8.2", "ch.qos.logback:logback-classic:jar:0.9.24" ]  
      @naether.dependencies.should eql ["junit:junit:jar:4.8.2", "ch.qos.logback:logback-classic:jar:0.9.24"]
    end
    
    it "should resolve dependencies" do
      @naether.add_dependency( "ch.qos.logback:logback-classic:jar:0.9.24" ) 
      @naether.resolve_dependencies.should eql ["ch.qos.logback:logback-classic:jar:0.9.24", "ch.qos.logback:logback-core:jar:0.9.24", "org.slf4j:slf4j-api:jar:1.6.0"]
    end
  end
end