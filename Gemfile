source "http://rubygems.org"

gem 'httpclient'
gem 'rake', '< 11.0'

platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
 if platform != 'java'
  gem 'rjb', '> 1.4.0', '< 1.6.0'
end

group :development do
  gem "rspec", "> 2.9"
  gem "jeweler", "~> 2.1"
  gem "yard"
  gem "kramdown"
end
