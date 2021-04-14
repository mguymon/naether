source "http://rubygems.org"

gem 'httpclient'
gem 'rake', '>= 12.3.3'

platform = $platform || RUBY_PLATFORM[/java/] || 'ruby'
 if platform != 'java'
  gem 'rjb', '> 1.6'
end

group :development do
  gem "rspec", "> 2.9"
  gem "jeweler", "~> 2.1"
  gem "yard"
  gem "kramdown"
end
