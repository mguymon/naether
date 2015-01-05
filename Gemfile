source "http://rubygems.org"

platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
 if platform != 'java'
  gem 'rjb', '~> 1.4.0'
end

group :development do
  gem "rspec", "> 2.9.0"
  gem "jeweler", "~> 2.0.0"
  gem "yard"
  gem "kramdown"

   if platform == 'java'
  	gem 'jruby-openssl'
  end
end
