require  File.expand_path(File.join(File.dirname(__FILE__), 'spec_helper'))
require 'src/main/ruby/naether'
require 'src/main/ruby/naether/java'
require 'src/main/ruby/naether/bootstrap'

describe Naether do

  context "Class" do

    it "should have bootstrap dependencies" do
      Naether.bootstrap_dependencies( 'jar_dependencies.yml' ).should include "org.sonatype.aether:aether-util:jar:1.13.1"
    end

  end

  context "Instance" do

    before(:each) do
      # XXX: in 1.8.7, the classpath is not being set in the spec_helper
      Naether::Configuration.naether_jar = "target/core-#{Naether::Configuration.version}.jar"
      Naether::Bootstrap.bootstrap_local_repo(
        File.expand_path( '../../../../target/test-repo', __FILE__ ),
        :dep_yaml => File.expand_path( '../../../../jar_dependencies.yml', __FILE__ ) )


      @test_dir = 'target/test-rb'
      unless File.exists?( @test_dir )
        Dir.mkdir @test_dir
      end

      @naether = Naether.create
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

    it "should only add a unique remote repository" do
      @naether.add_remote_repository("http://test.net:7011")
      @naether.add_remote_repository("http://test.net:7011")

      @naether.remote_repository_urls.should eql(["http://repo1.maven.org/maven2/", "http://test.net:7011"])
    end

    it "should set unique remote repositories from pom file" do
      @naether.dependencies = 'src/test/resources/valid_pom.xml'
      @naether.remote_repository_urls.should eql( ["http://repo1.maven.org/maven2/", "http://repository.jboss.org/nexus/content/groups/public-jboss"] )
    end

    it "should add a dependency from notation" do
      @naether.dependencies = "junit:junit:jar:4.8.2"
      @naether.dependencies_notation.should eql ["junit:junit:jar:4.8.2"]
    end

    it "should get paths for dependencies" do
      @naether.dependencies = "junit:junit:jar:4.8.2"
      @naether.resolve_dependencies
      @naether.dependencies_path.should eql({"junit:junit:jar:4.8.2" => File.expand_path("target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar")})
    end

    it "should get local paths for notations" do
      paths = @naether.to_local_paths( ["junit:junit:jar:4.8.2"] )
      paths.first.should match /test-repo\/junit\/junit\/4.8.2\/junit-4.8.2.jar/
    end

    it "should get dependency graph" do
      @naether.dependencies = "org.springframework:org.springframework.orm:3.0.5.RELEASE"
      @naether.add_remote_repository( "http://repository.springsource.com/maven/bundles/release" );
      @naether.add_remote_repository( "http://repository.springsource.com/maven/bundles/external" );

      @naether.resolve_dependencies(false)
      @naether.dependencies_graph.should eql({
        "org.springframework:org.springframework.orm:jar:3.0.5.RELEASE"=>{
          "org.springframework:org.springframework.jdbc:jar:3.0.5.RELEASE"=>{},
          "org.springframework:org.springframework.transaction:jar:3.0.5.RELEASE"=>{
            "org.springframework:org.springframework.context:jar:3.0.5.RELEASE"=>{
              "org.springframework:org.springframework.expression:jar:3.0.5.RELEASE"=>{}
            },
            "org.springframework:org.springframework.aop:jar:3.0.5.RELEASE"=>{},
            "org.aopalliance:com.springsource.org.aopalliance:jar:1.0.0"=>{}
          },
          "org.springframework:org.springframework.core:jar:3.0.5.RELEASE"=>{},
          "org.springframework:org.springframework.beans:jar:3.0.5.RELEASE"=>{
            "org.springframework:org.springframework.asm:jar:3.0.5.RELEASE"=>{}
          }
        }
      })
    end

    it "should set build artifacts" do
      @naether.build_artifacts = { "build_artifact:test:0.1" => 'target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar' }
      @naether.dependencies = [ "ch.qos.logback:logback-classic:jar:0.9.29", "junit:junit:jar:4.8.2" ]
      @naether.resolve_dependencies().should =~ [
        "ch.qos.logback:logback-core:jar:0.9.29",
        "ch.qos.logback:logback-classic:jar:0.9.29",
        "junit:junit:jar:4.8.2",
        "org.slf4j:slf4j-api:jar:1.6.1" ]
    end

    context "setting mixed list of dependencies" do
      it "should handle a list of dependencies" do
         @naether.dependencies = [ "junit:junit:jar:4.8.2", "ch.qos.logback:logback-classic:jar:0.9.29" ]
         @naether.dependencies_notation.should eql ["junit:junit:jar:4.8.2", "ch.qos.logback:logback-classic:jar:0.9.29"]
      end

      it "should handle poms in a list of dependencies" do
         @naether.dependencies = [  "pom.xml", "does.not:exist:jar:0.1" ]

         [ "org.apache.maven.wagon:wagon-http-lightweight:jar:1.0",
           "org.sonatype.aether:aether-api:jar:1.13.1",
           "org.sonatype.aether:aether-connector-asynchttpclient:jar:1.13.1",
           "org.apache.maven.wagon:wagon-file:jar:1.0",
           "does.not:exist:jar:0.1", "org.sonatype.aether:aether-util:jar:1.13.1",
           "org.sonatype.aether:aether-impl:jar:1.13.1",
           "org.apache.maven.wagon:wagon-ssh:jar:1.0",
           "org.apache.maven:maven-aether-provider:jar:3.0.3",
           "org.sonatype.aether:aether-connector-wagon:jar:1.13.1",
           "org.sonatype.aether:aether-connector-file:jar:1.13.1" ].each do |notation|

            @naether.dependencies_notation.should include(notation)
         end
      end

      it "should handle pom with scopes" do
        @naether.dependencies = [ {"src/test/resources/valid_pom.xml" => ["test"]}, {"junit:junit:jar:4.8.2" => "test"}, "ch.qos.logback:logback-classic:jar:0.9.29" ]
        @naether.dependencies_notation.should eql [
          "com.google.code.greaze:greaze-client:jar:test-jar:0.5.1",
          "ch.qos.logback:logback-classic:jar:0.9.29", "junit:junit:jar:4.8.2"]
        @naether.resolve_dependencies().should =~ [
          "com.google.code.greaze:greaze-client:jar:test-jar:0.5.1",
          "com.google.code.gson:gson:jar:1.7.1",
          "ch.qos.logback:logback-core:jar:0.9.29",
          "ch.qos.logback:logback-classic:jar:0.9.29", "junit:junit:jar:4.8.2",
          "org.slf4j:slf4j-api:jar:1.6.1",
          "com.google.code.greaze:greaze-definition:jar:0.5.1"]
      end
    end

    it "should resolve dependencies" do
      @naether.dependencies = "ch.qos.logback:logback-classic:jar:0.9.29"
      @naether.resolve_dependencies().should eql [
        "ch.qos.logback:logback-core:jar:0.9.29",
        "ch.qos.logback:logback-classic:jar:0.9.29",
        "org.slf4j:slf4j-api:jar:1.6.1"]

      @naether.dependencies_notation.should =~ [
        "ch.qos.logback:logback-core:jar:0.9.29",
        "ch.qos.logback:logback-classic:jar:0.9.29",
        "org.slf4j:slf4j-api:jar:1.6.1"]
    end


    it "should resolve pom dependencies with properties" do
      @naether.dependencies  = 'src/test/resources/pom_with_broken_dep.xml'
      @naether.resolve_dependencies(false, { 'project.basedir' => File.expand_path( 'src/test/resources' ) } ).should eql(
        ["commons-beanutils:commons-beanutils:jar:1.8.3",
         "ch.qos.logback:logback-core:jar:0.9.29", "pom:with-system-path:jar:2",
         "ch.qos.logback:logback-classic:jar:0.9.29", "org.slf4j:slf4j-api:jar:1.6.1"] )
      @naether.dependencies_notation.should =~
        ["commons-beanutils:commons-beanutils:jar:1.8.3",
         "ch.qos.logback:logback-core:jar:0.9.29", "pom:with-system-path:jar:2",
         "ch.qos.logback:logback-classic:jar:0.9.29", "org.slf4j:slf4j-api:jar:1.6.1"]
    end

    it "should download artifacts" do
      if File.exists?("target/test-repo/junit/junit/4.9/junit-4.9.jar")
        FileUtils.rm_rf( "target/test-repo/junit/junit/4.9" );
      end

      if File.exists?("target/test-repo/junit/junit/4.10/junit-4.10.jar")
        FileUtils.rm_rf( "target/test-repo/junit/junit/4.10" );
      end

      paths = @naether.download_artifacts( ["junit:junit:4.10", "junit:junit:4.9"])

      File.exists?("target/test-repo/junit/junit/4.9/junit-4.9.jar").should be_true
      File.exists?("target/test-repo/junit/junit/4.10/junit-4.10.jar").should be_true
    end

    it "should deploy artifact" do
      if File.exists?( 'target/test-repo/test/test/22.3/test-22.3.jar' )
        File.delete( 'target/test-repo/test/test/22.3/test-22.3.jar' )
      end
      jar = "target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar"

      @naether.deploy_artifact( "test:test:jar:22.3", jar, "file:target/test-repo" )

      File.exists?( 'target/test-repo/test/test/22.3/test-22.3.jar' ).should be_true
    end

  end
end
