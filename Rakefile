require 'rubygems'
require 'bundler'
require "fileutils"

begin
  Bundler.setup(:default, :development)
rescue Bundler::BundlerError => e
  $stderr.puts e.message
  $stderr.puts "Run `bundle install` to install missing gems"
  exit e.status_code
end
require 'rake'


require File.expand_path(File.join(File.dirname(__FILE__), 'src/main/ruby/naether'))

namespace :naether do
  task :setup_naether_gem_build do
      unless File.exists?( "target" )
        raise "Run `mvn package` to build java first" 
      end
      
      unless File.exists?( "target/gem" )
        Dir.mkdir( "target/gem" )
      end
      
      unless File.exists?( "target/gem/lib" )
        FileUtils.mkdir_p( "target/gem/lib/naether/java" )
      end
      
            
      Dir.glob("src/main/ruby/**/*.rb") do |file|
        FileUtils.copy( file, file.gsub("src/main/ruby", "target/gem/lib"))
      end
      
      FileUtils.copy( 'jar_dependencies.yml', "target/gem/jar_dependencies.yml" )
      FileUtils.copy( 'LICENSE', "target/gem/LICENSE" )
      FileUtils.copy( 'README.md', "target/gem/README.md" )
      FileUtils.copy( 'pom.xml', "target/gem/pom.xml" )
      FileUtils.copy( 'VERSION', "target/gem/VERSION" )
      FileUtils.copy( 'PostInstallRakefile', "target/gem/Rakefile" )
      FileUtils.copy( 'Gemfile', "target/gem/Gemfile" )
      FileUtils.copy( 'Gemfile.lock', "target/gem/Gemfile.lock" )
      
      version = IO.read('VERSION').strip
      if !File.exists? "target/core-#{version}.jar"
        raise "target/core-#{version}.jar does not exist"
      end
      
      FileUtils.copy( "target/core-#{version}.jar", "target/gem/core-#{version}.jar" )
      
      # Change dir so relevant files have the correct paths
      Dir.chdir( "target/gem" )      
  end
  
  task :version_return_from_target do
    FileUtils.copy( 'VERSION', "../../." )
    Dir.chdir( "../../" )
  end

  
  # XXX: bit of chicken and the egg, need a valid jar_dependencies to properly
  #      bootstrap Naether
  desc "write jar_dependencies.yml"
  task :write_dependencies do
    Naether::Configuration[:naether_jar] = "target/core-#{Naether::Configuration[:version]}.jar"
    require "#{File.dirname(__FILE__)}/src/main/ruby/naether/java"
    Naether::Java.load_paths( "target/classes" )
    require "#{File.dirname(__FILE__)}/src/main/ruby/naether/bootstrap"
    Naether::Bootstrap.bootstrap_local_repo("target/test-repo", :dep_yaml => File.expand_path( '../jar_dependencies.yml', __FILE__ ) )
    Naether::Bootstrap.write_dependencies()
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
  # gem is a Gem::Specification... see http://docs.rubygems.org/read/chapter/20 for more options
  gem.name = "naether"
  gem.rubyforge_project  = 'naether'
  gem.homepage = "http://github.com/mguymon/naether"
  gem.license = "Apache"
  gem.summary = %Q{Java dependency resolver using Maven's Aether}
  gem.description = %Q{Java dependency resolver using Maven's Aether}
  gem.email = "michael@tobedevoured.com"
  gem.authors = ["Michael Guymon"]
  gem.platform      = $platform || RUBY_PLATFORM[/java/] || 'ruby'
  gem.require_paths = %w[lib]
  gem.extensions = ["Rakefile"]
  
  # all files in target/gem should be included, expect for pkg
  gem.files = Dir.glob("**/*").select{ |path| !(path =~ /^pkg/) }
  
  # Jeweler stopped loading Bundler deps automatically ><
  bundler = Bundler.load
  bundler.dependencies_for(:default, :runtime).each do |dependency|
    gem.add_dependency dependency.name, *dependency.requirement.as_list
  end
  bundler.dependencies_for(:development).each do |dependency|
    gem.add_development_dependency dependency.name, *dependency.requirement.as_list
  end
    
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

task :test => :spec

task :default => :test

require 'yard'
YARD::Rake::YardocTask.new do |t|
  t.files   = ['lib/**/*.rb', 'LICENSE', 'pom.xml', 'jar_dependencies.yml'] 
end
Rake::Task["build"].enhance ['yard']
