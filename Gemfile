source "http://rubygems.org"

platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
 if platform != 'java'
  gem 'rjb', '~> 1.4.0'
end

group :development do
  gem "rspec", "> 2.9.0"
  gem "jeweler", "~> 1.8.0"
  gem "yard", "~> 0.8.7.3"
  gem "kramdown", "~> 1.3.1"

   if platform == 'java'
  	gem 'jruby-openssl'
  end
end
