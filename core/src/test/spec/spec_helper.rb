$:.unshift File.expand_path('.')
$:.unshift File.expand_path(File.join(File.dirname(__FILE__),'../../../'))
$:.unshift File.expand_path(File.join(File.dirname(__FILE__),'../../../src/main/ruby'))

require 'rubygems'
require 'rspec'

RSpec.configure do |config|
  
end