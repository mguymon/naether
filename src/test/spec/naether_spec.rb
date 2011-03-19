require 'src/main/ruby/naether'

describe Naether do
  
  context "Class" do
    it "should have bootstrap dependencies" do
      Naether.bootstrap_dependencies( 'jar_dependencies.yml' ).should include "org.sonatype.aether:aether-util:jar:1.11"
    end
  end
  
  context "Instance" do
    before(:each) do
      @test_dir = 'target/test-rb'
      unless File.exists?( @test_dir )
        Dir.mkdir @test_dir
      end
      
      @naether = Naether.create_from_paths("target/lib", 'target')
      @naether.should_not be_nil
      @naether.local_repo_path = 'target/test-repo'
    end
    
    it "should have set local repo path" do
      @naether.local_repo_path.should eql( 'target/test-repo' )
    end
    
    it "should add a remote repository" do
      @naether.add_remote_repository("http://test.net:7011")
      @naether.remote_repositories[0].getId().should eql( "central" )
      @naether.remote_repositories[1].getId().should eql( "test.net-7011" )
    end
    
    it "should add a dependency" do
      @naether.dependencies = "junit:junit:jar:4.8.2" 
      @naether.dependenciesNotation.should eql ["junit:junit:jar:4.8.2"]
    end
    
    it "should set a list of dependencies" do
      @naether.dependencies = [ {"junit:junit:jar:4.8.2" => "test"}, "ch.qos.logback:logback-classic:jar:0.9.24" ]  
      @naether.dependenciesNotation.should eql ["junit:junit:jar:4.8.2", "ch.qos.logback:logback-classic:jar:0.9.24"]
    end
    
    it "should resolve dependencies" do
      @naether.dependencies = "ch.qos.logback:logback-classic:jar:0.9.24" 
      @naether.resolve_dependencies.should eql ["ch.qos.logback:logback-classic:jar:0.9.24", "ch.qos.logback:logback-core:jar:0.9.24", "org.slf4j:slf4j-api:jar:1.6.0"]
    end
    
    it "should deploy artifact" do
      if File.exists?( 'target/test-repo/test/test/22.3/test-22.3.jar' )
        File.delete( 'target/test-repo/test/test/22.3/test-22.3.jar' )
      end
      jar = "target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar"
      
      @naether.deploy_artifact( "test:test:jar:22.3", jar, "file:target/test-repo" )
      
      File.exists?( 'target/test-repo/test/test/22.3/test-22.3.jar' ).should be_true
    end
    
    it "should write pom file" do
      test_file = "#{@test_dir}/naether_spec_test.xml"
      
      @naether.dependencies = [ {"junit:junit:jar:4.8.2" => 'test'}, "ch.qos.logback:logback-classic:jar:0.9.24" ]
      @naether.resolve_dependencies
      @naether.write_pom( 'test-rb:test-rb:jar:100.1', test_file)
      
      File.exists?( test_file ).should be_true
      
      xml = IO.read( test_file ) 
      xml.should match /.+junit<\/groupId>\s+<artifactId>junit<\/artifactId>\s+<version>4.8.2<\/version>\s+<scope>test.+/
      xml.should match /.+ch.qos.logback<\/groupId>\s+<artifactId>logback-classic<\/artifactId>\s+<version>0.9.24.+/
      xml.should match /.+ch.qos.logback<\/groupId>\s+<artifactId>logback-core<\/artifactId>\s+<version>0.9.24.+/
      xml.should match /.+org.slf4j<\/groupId>\s+<artifactId>slf4j-api<\/artifactId>\s+<version>1.6.0.+/
      
    end
  end
end