source "http://rubygems.org"

platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
if platform != 'java'
	gem 'rjb'
end

group :development do
  gem "rspec", "~> 2.1.0"
  gem "bundler", "~> 1.0.0"
  gem "jeweler", "~> 1.5.2"
  
  if platform == 'java'
  	gem 'jruby-openssl'
  end
end
