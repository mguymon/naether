$:.unshift File.expand_path('.')
$:.unshift File.expand_path(File.join('../../../', File.dirname(__FILE__)))
$:.unshift File.expand_path(File.join('../../../src/main/ruby', File.dirname(__FILE__),))

require 'rubygems'
require 'rspec'

RSpec.configure do |config|
  
end