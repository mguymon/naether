# frozen_string_literal: true

$LOAD_PATH.unshift File.expand_path('.')
$LOAD_PATH.unshift File.expand_path(File.join(File.dirname(__FILE__), '../../../'))
$LOAD_PATH.unshift File.expand_path(File.join(File.dirname(__FILE__), '../../../src/main/ruby'))

puts File.expand_path(File.join(File.dirname(__FILE__), '../../../'))

puts '#########################################################'
puts ' Make sure you run mvn package to have an update to date '
puts ' naether jar for specs '
puts '#########################################################'

require 'rspec'
require 'pry'

require 'naether/configuration'
require 'naether/bootstrap'

Naether::Configuration.naether_jar = "target/core-#{Naether::Configuration.version}.jar"
Naether::Bootstrap.bootstrap_local_repo(
  File.expand_path('../../../target/test-repo', __dir__),
  dependencies_yml: File.expand_path('../../../jar_dependencies.yml', __dir__)
)

RSpec.configure do |config|
  config.expect_with :rspec do |c|
    c.syntax = %i[should expect]
  end

  config.backtrace_exclusion_patterns << %r{org/jruby}
end
