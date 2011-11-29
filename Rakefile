require 'rubygems'
require 'bundler'
begin
  Bundler.setup(:default, :development)
rescue Bundler::BundlerError => e
  $stderr.puts e.message
  $stderr.puts "Run `bundle install` to install missing gems"
  exit e.status_code
end
require 'rake'

require 'src/main/ruby/naether'
namespace :naether do
  require "fileutils"
  task :setup_naether_gem_build do
      unless File.exists?( "target" )
        raise "Run `mvn package` to build java first" 
      end
      
      unless File.exists?( "target/gem" )
        Dir.mkdir( "target/gem" )
      end
      
      unless File.exists?( "target/gem/lib" )
        FileUtils.mkdir_p( "target/gem/lib/naether" )
      end
      
            
      Dir.glob("src/main/ruby/**/*.rb") do |file|
        FileUtils.copy( file, file.gsub("src/main/ruby", "target/gem/lib"))
      end
      
      FileUtils.copy( 'jar_dependencies.yml', "target/gem/jar_dependencies.yml" )
      FileUtils.copy( 'LICENSE', "target/gem/LICENSE" )
      FileUtils.copy( 'README.rdoc', "target/gem/README.rdoc" )
      FileUtils.copy( 'pom.xml', "target/gem/pom.xml" )
      FileUtils.copy( 'VERSION', "target/gem/VERSION" )
      
      # Rather than loading the Java for inspecting the Maven project, just copy all naether
      # jars into the target/gem. The gemspec will know the correct jar to use from inspecting
      # the Maven project.
      Dir.glob('target/naether*.jar').each do |jar|
        FileUtils.copy( jar, "target/gem/#{File.basename(jar)}" )
      end
      
      # Change dir so relevant files have the correct paths
      Dir.chdir( "target/gem" )      
  end
  
  task :version_return_from_target do
    FileUtils.copy( 'VERSION', "../../." )
    Dir.chdir( "../../" )
  end

  desc "write jar_dependencies.yml"
  task :write_dependencies do
    Naether::Bootstrap.write_dependencies("target")
  end
  
  task :copy_gem_from_target do
    unless File.exists?( "../../pkg" )
      Dir.mkdir( "../../pkg" )
    end
    
    platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
    version = IO.read('VERSION').strip
    source = File.expand_path("pkg/naether-#{version}#{"-java" if platform =='java'}.gem")
    dest = File.expand_path( "../../pkg/." )
    puts "copying #{source} to #{dest}"
    FileUtils.copy( source, dest )   
  end
end

require 'jeweler'
Jeweler::Tasks.new do |gem|
  
  platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
  naether_jar = nil
  
  Naether::Java.load_jars_dir( ['target', 'target/lib'])
  
  if platform == 'java'
    java_import com.slackworks.naether.maven.Project
    
    maven_project = Project.new('pom.xml')
    naether_jar = "naether-#{maven_project.get_version}.jar"
  else
    mavenProjectClass = Rjb::import('com.slackworks.naether.maven.Project')
    maven_project = mavenProjectClass.new('pom.xml')
    naether_jar = "naether-#{maven_project.getVersion()}.jar"
  end
  
  puts "Naether Jar: #{naether_jar}"
  puts File.expand_path( "." )
  
  # gem is a Gem::Specification... see http://docs.rubygems.org/read/chapter/20 for more options
  gem.name = "naether"
  gem.rubyforge_project  = 'naether'
  gem.homepage = "http://github.com/mguymon/naether"
  gem.license = "Apache"
  gem.summary = %Q{Java dependency resolver using Maven's Aether}
  gem.description = %Q{Java dependency resolver using Maven's Aether}
  gem.email = "michael.guymon@gmail.com"
  gem.authors = ["Michael Guymon"]
  gem.platform       = platform
  gem.require_paths = %w[lib]
  
  ruby_files = Dir.glob("lib/**/*.rb")
  gem.files = ruby_files + ['jar_dependencies.yml', 'VERSION', 'LICENSE','README.rdoc','pom.xml', naether_jar]

  
  # Include your dependencies below. Runtime dependencies are required when using your gem,
  # and development dependencies are only needed for development (ie running rake tasks, tests, etc)
  #  gem.add_runtime_dependency 'jabber4r', '> 0.1'
  #  gem.add_development_dependency 'rspec', '> 1.2.3'
end
Jeweler::RubygemsDotOrgTasks.new

Rake::Task["build"].enhance ["naether:setup_naether_gem_build"]
Rake::Task["version_required"].enhance ["naether:setup_naether_gem_build"]
Rake::Task["gemspec:release"].enhance ["naether:setup_naether_gem_build"]

Rake::Task["version:bump:major"].enhance ['naether:version_return_from_target']
Rake::Task["version:bump:minor"].enhance ['naether:version_return_from_target']
Rake::Task["version:bump:patch"].enhance ['naether:version_return_from_target']

Rake::Task["build"].enhance do
  Rake::Task['naether:copy_gem_from_target'].invoke
end

require 'rspec/core/rake_task'

RSpec::Core::RakeTask.new(:spec) do |t|
  t.pattern = 'src/test/spec/**/*_spec.rb'
end

task :spec => :test_warning

task :test => :spec

task :default => :test

require 'rake/rdoctask'
Rake::RDocTask.new do |rdoc|
  
  version = File.exist?('VERSION') ? File.read('VERSION') : ""

  rdoc.rdoc_dir = 'rdoc'
  rdoc.title = "naether #{version}"
  rdoc.rdoc_files.include('README*')
  rdoc.rdoc_files.include('src/main/ruby/**/*.rb')
end

task :test_warning do
  puts " ###########################################################"
  puts " Make sure you run mvn package to download jars for the tests"
  puts " ###########################################################"
  puts ""
end