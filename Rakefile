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
  task :write_dependencies do
    Naether::Bootstrap.write_dependencies("target")
  end
end

require 'jeweler'
Jeweler::Tasks.new do |gem|
  
  platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
  naether_jar = nil
  
  Naether::Java.load_jars_dir( ['target', 'target/lib'])
  
  if platform == 'java'
    java_import com.slackworks.MavenProject
    
    maven_project = MavenProject.new('pom.xml')
    naether_jar = "naether-#{maven_project.get_version}.jar"
  else
    mavenProjectClass = Rjb::import('com.slackworks.MavenProject')
    maven_project = mavenProjectClass.new
    maven_project.loadPOM('pom.xml')
    naether_jar = "naether-#{maven_project.getVersion()}.jar"
  end
  
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
  gem.files = ruby_files + ['jar_dependencies.yml', 'VERSION', 'naether.gemspec', 'LICENSE','README.rdoc','pom.xml', naether_jar]

  
  # Include your dependencies below. Runtime dependencies are required when using your gem,
  # and development dependencies are only needed for development (ie running rake tasks, tests, etc)
  #  gem.add_runtime_dependency 'jabber4r', '> 0.1'
  #  gem.add_development_dependency 'rspec', '> 1.2.3'
end
Jeweler::RubygemsDotOrgTasks.new

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

Rake::Task["build"].enhance ["setup_naether_gem_build"]
Rake::Task["release"].enhance ["build"]

Rake::Task["release"].enhance do
  unless File.exists?( "../../pkg" )
    Dir.mkdir( "../../pkg" )
  end
  
  platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
  version = IO.read('VERSION').strip
  FileUtils.copy( "pkg/naether-#{version}#{"-java" if platform =='java'}.gem", "../../pkg/." ) 
end

require 'spec/rake/spectask'
Spec::Rake::SpecTask.new(:spec) do |t|
  t.spec_files = FileList['src/test/spec/**/*_spec.rb']
end

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