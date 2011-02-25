# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with this
# work for additional information regarding copyright ownership.  The ASF
# licenses this file to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#
require "ftools"
require 'java'
$CLASSPATH << "target/classes"
Dir.glob('target/lib/*.jar').each do |jar|
	require jar
end
java_import com.slackworks.MavenProject

maven_project = MavenProject.new('pom.xml')
naether_jar = "naether-#{maven_project.get_version}.jar"

unless File.exsts?( "target" )
	
end

unless File.exists?( "target/gem" )
	Dir.mkdir( "target/gem" )
end

File.copy( 'naether.gemspec', "target/gem/naether.gemspec" )
File.copy( 'LICENSE', "target/gem/LICENSE" )
File.copy( 'README.rdoc', "target/gem/README.rdoc" )
File.copy( 'pom.xml', "target/gem/pom.xml" )
File.copy( "target/#{naether_jar}", "target/gem/#{naether_jar}" )

Dir.chdir( "target/gem" )

puts "Creating gem in target/gem"

Gem::Specification.new do |spec|
  spec.name           = 'naether'
  spec.version        = maven_project.get_version
  spec.author         = 'Michael Guymon'
  spec.email          = "michael.guymon@gmail.com"
  spec.homepage       = "https://github.com/mguymon/naether"
  spec.summary        = "Java Dependency Resolver"
  spec.description    = <<-TEXT
Java Dependency Resolver for using Maven's Aether
  TEXT
  spec.rubyforge_project  = 'naether'

  # Rakefile needs to create spec for both platforms (ruby and java), using the
  # $platform global variable.  In all other cases, we figure it out from RUBY_PLATFORM.
  spec.platform       = 'java'

  spec.files          = ['naether.gemspec', 'LICENSE','README.rdoc','pom.xml', naether_jar]

end
