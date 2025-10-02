source "http://rubygems.org"

gem 'httpclient'
gem 'rake', '>= 12.3.3'
gem 'mutex_m', '~> 0.2.0'

platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
 if platform != 'java'
  gem 'rjb', '= 1.7.3'
end

group :development do
  gem "rspec", "> 2.9"
  gem "yard"
  gem "kramdown"
end
