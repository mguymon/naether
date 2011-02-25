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

require 'java'
$CLASSPATH << "target/classes"
Dir.glob('target/lib/*.jar').each do |jar|
	require jar
end
java_import com.slackworks.MavenProject

maven_project = MavenProject.new('pom.xml')


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
  spec.platform       = $platform || RUBY_PLATFORM[/java/] || 'ruby'

  spec.files          = Dir['pom.xml', '*.{gemspec}'] +
                        ['LICENSE','README.rdoc']

end
