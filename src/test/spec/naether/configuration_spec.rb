# frozen_string_literal: true

require File.expand_path(File.join(File.dirname(__FILE__), '../spec_helper'))
require 'src/main/ruby/naether/configuration'

describe Naether::Configuration do
  it 'should have naether jar' do
    described_class.naether_jar.should match(/core-#{described_class.version}.jar/)
  end

  it 'should have jar_dependencies.yml' do
    described_class.dependencies_yml.should match(/jar_dependencies\.yml/)
  end
end
