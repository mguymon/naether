# frozen_string_literal: true

require_relative "src/main/ruby/naether/version"

Gem::Specification.new do |spec|
  spec.name = "naether"
  spec.version = Naether::VERSION
  spec.authors = ["Michael Guymon"]
  spec.email = ["michael@tobedevoured.com"]

  spec.summary = "Java dependency resolver using Maven's Aether"
  spec.description = "Java dependency resolver using Maven's Aether. Naether provides resolution, artifact downloading, and more from Maven repositories."
  spec.homepage = "http://github.com/mguymon/naether"
  spec.license = "Apache-2.0"
  spec.required_ruby_version = ">= 2.3.0"

  spec.metadata["homepage_uri"] = spec.homepage
  spec.metadata["source_code_uri"] = "http://github.com/mguymon/naether"

  # Specify which files should be added to the gem when it is released.
  spec.files = Dir.glob("**/*").select { |path| !(path =~ /^pkg/) }
  spec.require_paths = ["src/main/ruby"]
  spec.extensions = ["Rakefile"]
  spec.platform = RUBY_PLATFORM[/java/] ? "java" : "ruby"

  # Runtime dependencies
  spec.add_dependency "httpclient", "~> 2.8"
  spec.add_dependency "mutex_m", "~> 0.2.0"

  # ruby java bridge for MRI
  spec.add_dependency "rjb", '~> 1.7.3' unless RUBY_PLATFORM[/java/]

  # Development dependencies
  spec.add_development_dependency "bundler", "~> 2.0"
  spec.add_development_dependency "rake", ">= 12.3.3", "< 14.0"
  spec.add_development_dependency "rspec", "~> 3.0"
  spec.add_development_dependency "yard", "~> 0.9"
  spec.add_development_dependency "kramdown", "~> 2.0"
end
