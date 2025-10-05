require 'rubygems'
require 'bundler'
require "fileutils"
require 'rake'
require File.expand_path(File.join(File.dirname(__FILE__), 'src/main/ruby/naether'))

namespace :naether do
  task :setup_naether_gem_build do
      unless File.exist?( "target" )
        raise "Run `mvn package` to build java first"
      end

      unless File.exist?( "target/gem" )
        Dir.mkdir( "target/gem" )
      end

      unless File.exist?( "target/gem/lib" )
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
      FileUtils.copy( 'naether.gemspec', "target/gem/naether.gemspec" )

      version = IO.read('VERSION').strip
      if !File.exist? "target/core-#{version}.jar"
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
end

require 'bundler/gem_tasks'

# Bundler::GemHelper provides build, install, and release tasks
Bundler::GemHelper.install_tasks

Rake::Task["build"].enhance ["naether:setup_naether_gem_build"]

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
#Rake::Task["build"].enhance ['yard']
