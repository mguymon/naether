source "http://rubygems.org"

platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
 if platform != 'java'
  gem 'rjb', '> 1.3.2'
end

group :development do
  gem 'rake',  '~> 0.9.2'
  gem "rspec", "> 2.9.0"
  gem "bundler", "> 1.0.0"
  gem "jeweler", "> 1.5.2"
  gem "yard"
  gem "kramdown"
  
   if platform == 'java'
  	gem 'jruby-openssl'
  end
end
