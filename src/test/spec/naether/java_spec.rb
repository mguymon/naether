# frozen_string_literal: true

require 'src/main/ruby/naether'
require 'src/main/ruby/naether/java'

describe Naether::Java do
  context 'Class' do
    it 'should load jars' do
      loaded_jars = described_class.load_paths('src/test/resources/commons-beanutils-1.8.3.jar')
      loaded_jars.should include(File.expand_path('src/test/resources/commons-beanutils-1.8.3.jar'))
    end

    it 'should create an instance of com.tobedevoured.naether.maven.Project' do
      @naether = Naether.create
      instance = described_class.create('com.tobedevoured.naether.maven.Project')
      instance.getClass.getName.should eql 'com.tobedevoured.naether.maven.Project'
    end

    it 'should load paths' do
      loaded_paths = described_class.load_paths('src/test/classes/commons-io')
      loaded_paths.should include(File.expand_path('src/test/classes/commons-io'))

      # Load to internal class loader as well since exec_static_method uses the internal PathClassLoader
      described_class.internal_load_paths('src/test/classes/commons-io')

      file = described_class.create('java.io.File', 'VERSION')
      version = described_class.exec_static_method('org.apache.commons.io.FileUtils', 'readFileToString', file).strip
      version.should eql File.read('VERSION').strip
    end
  end
end
