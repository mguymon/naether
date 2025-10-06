# frozen_string_literal: true

require 'ostruct'
require_relative 'version'

module Naether
  #
  # Naether runtime configuration
  #
  Configuration = begin
    gem_dir = File.expand_path("#{File.dirname(__FILE__)}/../../")

    data = {
      version: Naether::VERSION,
      gem_dir: gem_dir,
      naether_jar: File.join(gem_dir, "core-#{Naether::VERSION}.jar"),
      platform: RUBY_PLATFORM[/java/] || 'ruby',
      dependencies_yml: File.expand_path("#{File.dirname(__FILE__)}/../../jar_dependencies.yml")
    }
    OpenStruct.new(data)
  end
end
