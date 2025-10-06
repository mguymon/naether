# frozen_string_literal: true

require 'src/main/ruby/naether'
require 'src/main/ruby/naether/java'
require 'src/main/ruby/naether/bootstrap'

describe Naether do
  context 'Class' do
    it 'should have bootstrap dependencies' do
      described_class.bootstrap_dependencies('jar_dependencies.yml').should(
        include 'org.apache.maven.resolver:maven-resolver-util:jar:2.0.11'
      )
    end
  end

  context 'Instance' do
    before do
      Naether::Bootstrap.bootstrap_local_repo(
        File.expand_path('../../../target/test-repo', __dir__),
        dependencies_yml: File.expand_path('../../../jar_dependencies.yml', __dir__)
      )

      @test_dir = 'target/test-rb'
      FileUtils.mkdir_p @test_dir

      @naether = described_class.create
      @naether.local_repo_path = 'target/test-repo'
    end

    it 'should have set local repo path' do
      @naether.local_repo_path.should eql('target/test-repo')
    end

    it 'should add a remote repository' do
      @naether.add_remote_repository('http://test.net:7011')
      @naether.remote_repositories[0].getId.should eql('central')
      @naether.remote_repositories[1].getId.should eql('test.net-7011')
    end

    it 'should only add a unique remote repository' do
      @naether.add_remote_repository('http://test.net:7011')
      @naether.add_remote_repository('http://test.net:7011')

      @naether.remote_repository_urls.should eql(['https://repo1.maven.org/maven2/', 'http://test.net:7011'])
    end

    it 'should set unique remote repositories from pom file' do
      @naether.dependencies = 'src/test/resources/valid_pom.xml'
      @naether.remote_repository_urls.should eql(['https://repo1.maven.org/maven2/', 'https://repository.jboss.org/nexus/content/groups/public-jboss'])
    end

    it 'should add a dependency from notation' do
      @naether.dependencies = 'junit:junit:jar:4.8.2'
      @naether.dependencies_notation.should eql ['junit:junit:jar:4.8.2']
    end

    it 'should get paths for dependencies' do
      @naether.dependencies = 'junit:junit:jar:4.8.2'
      @naether.resolve_dependencies
      @naether.dependencies_path.should(
        eql({ 'junit:junit:jar:4.8.2' => File.expand_path('target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar') })
      )
    end

    it 'should get local paths for notations' do
      paths = @naether.to_local_paths(['junit:junit:jar:4.8.2'])
      paths.first.should match(%r{test-repo/junit/junit/4.8.2/junit-4.8.2.jar})
    end

    it 'should get dependency graph' do
      @naether.dependencies = 'org.springframework:spring-orm:jar:3.0.5.RELEASE'

      @naether.resolve_dependencies(false)
      @naether.dependencies_graph.should eql({
                                               'org.springframework:spring-orm:jar:3.0.5.RELEASE' => {
                                                 'org.springframework:spring-beans:jar:3.0.5.RELEASE' => {},
                                                 'org.springframework:spring-core:jar:3.0.5.RELEASE' => {
                                                   'commons-logging:commons-logging:jar:1.1.1' => {},
                                                   'org.springframework:spring-asm:jar:3.0.5.RELEASE' => {}
                                                 },
                                                 'org.springframework:spring-jdbc:jar:3.0.5.RELEASE' => {},
                                                 'org.springframework:spring-tx:jar:3.0.5.RELEASE' => {
                                                   'aopalliance:aopalliance:jar:1.0' => {},
                                                   'org.springframework:spring-aop:jar:3.0.5.RELEASE' => {},
                                                   'org.springframework:spring-context:jar:3.0.5.RELEASE' => {
                                                     'org.springframework:spring-expression:jar:3.0.5.RELEASE' => {}
                                                   }
                                                 }
                                               }
                                             })
    end

    it 'should set build artifacts' do
      @naether.build_artifacts = { 'build_artifact:test:0.1' => 'target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar' }
      @naether.dependencies = ['ch.qos.logback:logback-classic:jar:0.9.29', 'junit:junit:jar:4.8.2']
      @naether.resolve_dependencies.should =~ [
        'ch.qos.logback:logback-core:jar:0.9.29',
        'ch.qos.logback:logback-classic:jar:0.9.29',
        'junit:junit:jar:4.8.2',
        'org.slf4j:slf4j-api:jar:1.6.1'
      ]
    end

    context 'setting mixed list of dependencies' do
      it 'should handle a list of dependencies' do
        @naether.dependencies = ['junit:junit:jar:4.8.2', 'ch.qos.logback:logback-classic:jar:0.9.29']
        @naether.dependencies_notation.should =~ ['junit:junit:jar:4.8.2', 'ch.qos.logback:logback-classic:jar:0.9.29']
      end

      it 'should handle poms in a list of dependencies' do
        @naether.dependencies = ['pom.xml', 'does.not:exist:jar:0.1']

        ['org.apache.maven.wagon:wagon-http-lightweight:jar:3.5.3',
         'org.apache.maven.resolver:maven-resolver-api:jar:2.0.11',
         'org.apache.maven.resolver:maven-resolver-transport-apache:jar:2.0.11',
         'org.apache.maven.wagon:wagon-file:jar:3.5.3',
         'does.not:exist:jar:0.1', 'org.apache.maven.resolver:maven-resolver-util:jar:2.0.11',
         'org.apache.maven.resolver:maven-resolver-impl:jar:2.0.11',
         'org.apache.maven.wagon:wagon-ssh:jar:3.5.3',
         'org.apache.maven:maven-resolver-provider:jar:3.9.9',
         'org.apache.maven.resolver:maven-resolver-transport-wagon:jar:2.0.11',
         'org.apache.maven.resolver:maven-resolver-transport-file:jar:2.0.11'].each do |notation|
          @naether.dependencies_notation.should include(notation)
        end
      end

      it 'should handle pom with scopes' do
        @naether.dependencies = [{ 'src/test/resources/valid_pom.xml' => ['test'] },
                                 { 'junit:junit:jar:4.8.2' => 'test' }, 'ch.qos.logback:logback-classic:jar:0.9.29']
        @naether.dependencies_notation.should =~ [
          'com.google.code.greaze:greaze-client:jar:test-jar:0.5.1',
          'ch.qos.logback:logback-classic:jar:0.9.29',
          'junit:junit:jar:4.8.2'
        ]

        @naether.resolve_dependencies.should =~ [
          'com.google.code.greaze:greaze-client:jar:test-jar:0.5.1',
          'com.google.code.gson:gson:jar:1.7.1',
          'ch.qos.logback:logback-core:jar:0.9.29',
          'ch.qos.logback:logback-classic:jar:0.9.29', 'junit:junit:jar:4.8.2',
          'org.slf4j:slf4j-api:jar:1.6.1',
          'com.google.code.greaze:greaze-definition:jar:0.5.1'
        ]
      end
    end

    it 'should resolve dependencies' do
      @naether.dependencies = 'ch.qos.logback:logback-classic:jar:0.9.29'
      @naether.resolve_dependencies.should =~ [
        'ch.qos.logback:logback-core:jar:0.9.29',
        'ch.qos.logback:logback-classic:jar:0.9.29',
        'org.slf4j:slf4j-api:jar:1.6.1'
      ]

      @naether.dependencies_notation.should =~ [
        'ch.qos.logback:logback-core:jar:0.9.29',
        'ch.qos.logback:logback-classic:jar:0.9.29',
        'org.slf4j:slf4j-api:jar:1.6.1'
      ]
    end

    it 'should resolve pom dependencies with properties' do
      @naether.dependencies = 'src/test/resources/pom_with_broken_dep.xml'
      @naether.resolve_dependencies(false, { 'project.basedir' => File.expand_path('src/test/resources') }).should =~ [
        'commons-beanutils:commons-beanutils:jar:1.8.3',
        'ch.qos.logback:logback-core:jar:0.9.29', 'pom:with-system-path:jar:2',
        'ch.qos.logback:logback-classic:jar:0.9.29', 'org.slf4j:slf4j-api:jar:1.6.1'
      ]
      @naether.dependencies_notation.should =~
        ['commons-beanutils:commons-beanutils:jar:1.8.3',
         'ch.qos.logback:logback-core:jar:0.9.29', 'pom:with-system-path:jar:2',
         'ch.qos.logback:logback-classic:jar:0.9.29', 'org.slf4j:slf4j-api:jar:1.6.1']
    end

    it 'should download artifacts' do
      if File.exist?('target/test-repo/junit/junit/4.9/junit-4.9.jar')
        FileUtils.rm_rf('target/test-repo/junit/junit/4.9')
      end

      if File.exist?('target/test-repo/junit/junit/4.10/junit-4.10.jar')
        FileUtils.rm_rf('target/test-repo/junit/junit/4.10')
      end

      @naether.download_artifacts(['junit:junit:4.10', 'junit:junit:4.9'])

      File.exist?('target/test-repo/junit/junit/4.9/junit-4.9.jar').should be_truthy
      File.exist?('target/test-repo/junit/junit/4.10/junit-4.10.jar').should be_truthy
    end

    it 'should deploy artifact' do
      FileUtils.rm_f('target/test-repo/test/test/22.3/test-22.3.jar')

      jar = 'target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar'

      @naether.deploy_artifact('test:test:jar:22.3', jar, 'file:target/test-repo')

      File.exist?('target/test-repo/test/test/22.3/test-22.3.jar').should be_truthy
    end
  end
end
