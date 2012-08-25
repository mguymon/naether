require  File.expand_path(File.join(File.dirname(__FILE__), 'spec_helper'))
require 'src/main/ruby/naether'
require 'src/main/ruby/naether/java'

describe Naether do
  
    before(:each) do
        @test_dir = 'target/test-rb'
        unless File.exists?( @test_dir )
          Dir.mkdir @test_dir
        end
        
        @naether = Naether.create_from_paths("target/lib", 'target')
        @naether.should_not be_nil
        @naether.local_repo_path = 'target/test-repo'
    end  
  
    it "should get version from pom file" do
      version = IO.read("VERSION")
      @naether.pom_version( 'pom.xml' ).strip.should eql version.strip
    end
    
    it "should get dependencies from pom file" do
      deps = @naether.pom_dependencies( 'src/test/resources/valid_pom.xml' )
      deps.should eql ["ch.qos.logback:logback-classic:jar:0.9.29", "junit:junit:jar:4.8.2", "com.google.code.greaze:greaze-client:jar:test-jar:0.5.1"]
                        
      deps = @naether.pom_dependencies( 'src/test/resources/valid_pom.xml', ['test'] )
      deps.should eql ["junit:junit:jar:4.8.2", "com.google.code.greaze:greaze-client:jar:test-jar:0.5.1"]
    end
    
    it "should create pom xml" do
      @naether.dependencies = [ "org.apache.maven.wagon:wagon-file:jar:1.0", {"junit:junit:jar:4.8.2" => 'test'} ]
      
      xml = @naether.build_pom( 'testGroup:testArtifact:jar:test' )
      
      pom = IO.read( "src/test/resources/generated_pom.xml" ) 
      xml.should eql( pom )
    end
    
    it "should write pom file" do
      test_file = "#{@test_dir}/naether_spec_test.xml"
      
      @naether.dependencies = [ {"junit:junit:jar:4.8.2" => 'test'}, "ch.qos.logback:logback-classic:jar:0.9.29" ]
      @naether.resolve_dependencies
      @naether.write_pom( 'test-rb:test-rb:jar:100.1', test_file)
      
      File.exists?( test_file ).should be_true
      
      xml = IO.read( test_file ) 
      xml.should match /.+junit<\/groupId>\s+<artifactId>junit<\/artifactId>\s+<version>4.8.2<\/version>\s+<scope>test.+/
      xml.should match /.+ch.qos.logback<\/groupId>\s+<artifactId>logback-classic<\/artifactId>\s+<version>0.9.29.+/
      xml.should match /.+ch.qos.logback<\/groupId>\s+<artifactId>logback-core<\/artifactId>\s+<version>0.9.29.+/
      xml.should match /.+org.slf4j<\/groupId>\s+<artifactId>slf4j-api<\/artifactId>\s+<version>1.6.1.+/
      
    end
    
end
