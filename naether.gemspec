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

Gem::Specification.new do |spec|
  spec.name           = 'naether'
  spec.version        = 0.1
  spec.author         = 'Michael Guymon'
  spec.email          = "michael.guymon@gmail.com"
  spec.homepage       = "https://github.com/mguymon/buildr-naether"
  spec.summary        = "Java Dependency Resolver for Buildr"
  spec.description    = <<-TEXT
Java Dependency Resolver for Buildr using Maven's Aether
  TEXT
  spec.rubyforge_project  = 'buildr-naether'

  # Rakefile needs to create spec for both platforms (ruby and java), using the
  # $platform global variable.  In all other cases, we figure it out from RUBY_PLATFORM.
  spec.platform       = $platform || RUBY_PLATFORM[/java/] || 'ruby'

  spec.files          = Dir['{lib,spec}/**/*', '*.{gemspec}'] +
                        ['LICENSE','README.rdoc']
  spec.require_paths  = 'lib'
  
  #spec.has_rdoc         = true
  #spec.extra_rdoc_files = 'README.rdoc', 'LICENSE'
  #spec.rdoc_options     = '--title', 'Buildr', '--main', 'README.rdoc',
  #                        '--webcvs', 'http://svn.apache.org/repos/asf/buildr/trunk/'
  #spec.post_install_message = "To get started run buildr --help"

  # Tested against these dependencies.
  spec.add_dependency 'buildr',               '1.4.4'
  spec.add_development_dependency 'rspec',    '~> 2.1.0'
end
