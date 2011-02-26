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

require 'jeweler'
Jeweler::Tasks.new do |gem|
  
  platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
  naether_jar = nil
  
  if platform == 'java'
    require 'java'
    $CLASSPATH << "target/classes"
    Dir.glob('target/lib/*.jar').each do |jar|
      require jar
    end
    java_import com.slackworks.MavenProject
    
    maven_project = MavenProject.new('pom.xml')
    naether_jar = "naether-#{maven_project.get_version}.jar"
  else
    require 'rjb'
    classpath = ["target/classes"]
    Dir.glob('target/lib/*.jar').each do |jar|
      classpath << jar
    end
    java_opts = (ENV['JAVA_OPTS'] || ENV['JAVA_OPTIONS']).to_s.split
    Rjb::load(classpath.join(File::PATH_SEPARATOR), java_opts)
    MavenProject = Rjb::import('com.slackworks.MavenProject')
    maven_project = MavenProject.new
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
  
  gem.files = ['VERSION', 'naether.gemspec', 'LICENSE','README.rdoc','pom.xml', 'lib/naether.rb', naether_jar]

  
  # Include your dependencies below. Runtime dependencies are required when using your gem,
  # and development dependencies are only needed for development (ie running rake tasks, tests, etc)
  #  gem.add_runtime_dependency 'jabber4r', '> 0.1'
  #  gem.add_development_dependency 'rspec', '> 1.2.3'
end
Jeweler::RubygemsDotOrgTasks.new

require "ftools"
task :setup_naether_gem_build do
    unless File.exists?( "target" )
      raise "Run `mvn package` to build java first" 
    end
    
    unless File.exists?( "target/gem" )
      Dir.mkdir( "target/gem" )
    end
    
    unless File.exists?( "target/gem/lib" )
      Dir.mkdir( "target/gem/lib" )
    end
    
    File.copy( 'src/main/ruby/naether.rb', "target/gem/lib/naether.rb" )
    File.copy( 'LICENSE', "target/gem/LICENSE" )
    File.copy( 'README.rdoc', "target/gem/README.rdoc" )
    File.copy( 'pom.xml', "target/gem/pom.xml" )
    File.copy( 'VERSION', "target/gem/VERSION" )
    
    # Rather than loading the Java for inspecting the Maven project, just copy all naether
    # jars into the target/gem. The gemspec will know the correct jar to use from inspecting
    # the Maven project.
    Dir.glob('target/naether*.jar').each do |jar|
      File.copy( jar, "target/gem/#{File.basename(jar)}" )
    end
    
    # Change dir so relevant files have the correct paths
    Dir.chdir( "target/gem" )
    
end

Rake::Task["build"].enhance ["setup_naether_gem_build"]

# XXX: Hack to copy new gem from target/gem/pkg/ to pkg/
Rake::Task["build"].enhance do
  unless File.exists?( "../../pkg" )
    Dir.mkdir( "../../pkg" )
  end
  
  version = IO.read('VERSION').strip
  File.copy( "pkg/naether-#{version}.gem", "../../pkg/." )  
end

require 'rake/testtask'
Rake::TestTask.new(:test) do |test|
  test.libs << 'lib' << 'test'
  test.pattern = 'test/**/test_*.rb'
  test.verbose = true
end

require 'rcov/rcovtask'
Rcov::RcovTask.new do |test|
  test.libs << 'test'
  test.pattern = 'test/**/test_*.rb'
  test.verbose = true
end

task :default => :test

require 'rake/rdoctask'
Rake::RDocTask.new do |rdoc|
  version = File.exist?('VERSION') ? File.read('VERSION') : ""

  rdoc.rdoc_dir = 'rdoc'
  rdoc.title = "naether #{version}"
  rdoc.rdoc_files.include('README*')
  rdoc.rdoc_files.include('lib/**/*.rb')
end