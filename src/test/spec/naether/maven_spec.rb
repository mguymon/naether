require  File.expand_path(File.join(File.dirname(__FILE__), '../spec_helper'))
require 'src/main/ruby/naether'
require 'src/main/ruby/naether/java'
require 'src/main/ruby/naether/maven'

describe Naether::Maven do
  
    before(:each) do
        @naether = Naether.create
        @naether.should_not be_nil
        @naether.local_repo_path = 'target/test-repo'
        
        @maven = Naether::Maven.new( 'src/test/resources/valid_pom.xml')
    end  
  
    it "should get version from pom file" do
      @maven.version().should eql "3"
    end
    
    it "should get runtime & compile dependencies for the project" do
      @maven.dependencies.should =~ ["ch.qos.logback:logback-classic:jar:0.9.29"]
    end

    it "should get dependencies by scope" do
        @maven.dependencies(['test']).should =~ [
          "com.google.code.greaze:greaze-client:jar:test-jar:0.5.1", "junit:junit:jar:4.8.2"]
    end
  
    it "should add a dependency" do
      @maven.add_dependency("org.apache.maven.wagon:wagon-file:jar:1.0")
      @maven.dependencies.should include "org.apache.maven.wagon:wagon-file:jar:1.0"
    end
    
    it "should add a dependency by scope" do
       @maven.add_dependency("org.apache.maven.wagon:wagon-file:jar:1.0", "runtime")
       @maven.dependencies('runtime').should =~ ["org.apache.maven.wagon:wagon-file:jar:1.0"]
    end
    
    it "should create pom xml" do
      @naether.dependencies = [ "org.apache.maven.wagon:wagon-file:jar:1.0", {"junit:junit:jar:4.8.2" => 'test'} ]
      @naether.add_remote_repository( 'http://repository.jboss.org/nexus/content/groups/public-jboss' )
      maven = Naether::Maven.create_from_notation('testGroup:testArtifact:jar:test')
      maven.load_naether( @naether )
      xml = maven.build_pom()
      
      pom = IO.read( "src/test/resources/generated_pom.xml" ) 
      xml.should eql(pom)
    end
    
    it "should write pom file" do
      test_file = "target/naether_spec_test.xml"
      
      @naether.dependencies = [ {"junit:junit:jar:4.8.2" => 'test'}, "ch.qos.logback:logback-classic:jar:0.9.29" ]
      @naether.resolve_dependencies
      
      maven = Naether::Maven.create_from_notation('test-rb:test-rb:jar:100.1')
      maven.load_naether( @naether )
      maven.write_pom(test_file)
      
      File.exists?( test_file ).should be_true
      
      xml = IO.read( test_file ) 
      xml.should match /.+junit<\/groupId>\s+<artifactId>junit<\/artifactId>\s+<version>4.8.2<\/version>\s+<scope>test.+/
      xml.should match /.+ch.qos.logback<\/groupId>\s+<artifactId>logback-classic<\/artifactId>\s+<version>0.9.29.+/
      xml.should match /.+ch.qos.logback<\/groupId>\s+<artifactId>logback-core<\/artifactId>\s+<version>0.9.29.+/
      xml.should match /.+org.slf4j<\/groupId>\s+<artifactId>slf4j-api<\/artifactId>\s+<version>1.6.1.+/
      
    end

    it "should compile project" do
      maven = Naether::Maven.create_from_pom('pom.xml')
      result = maven.invoke('compile')
      result.getExitCode().should eql 0
    end

    it "should get final name" do
      maven = Naether::Maven.create_from_pom('pom.xml')
      expect( maven.final_name).to eql "core-#{maven.version}"
    end
end
