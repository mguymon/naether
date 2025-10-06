# frozen_string_literal: true

require File.expand_path(File.join(File.dirname(__FILE__), '../spec_helper'))
require 'src/main/ruby/naether'
require 'src/main/ruby/naether/bootstrap'
require 'fileutils'

Naether::Configuration.dependencies_yml = File.expand_path(File.join(File.dirname(__FILE__),
                                                                     '../../../../jar_dependencies.yml'))

describe Naether::Bootstrap do
  context 'Class' do
    it 'should write jar dependencies yml' do
      described_class.write_dependencies('target/jar_dependencies.yml')
      expect(File).to exist('target/jar_dependencies.yml')

      deps = YAML.load_file('target/jar_dependencies.yml')
      expect(deps.size).to be > 0
    end

    it 'should download dependencies' do
      FileUtils.rm_r 'target/test/bootstrap' if File.exist? 'target/test/bootstrap'

      deps = described_class.download_dependencies('target/test/bootstrap', local_repo: 'non-existant-dir')

      deps[:missing].should eql(described_class.dependencies)
      described_class.dependencies.size.should eql(deps[:missing].size)
    end

    it 'should install dependencies to local repo' do
      FileUtils.rm_r 'target/test/bootstrap-repo' if File.exist? 'target/test/bootstrap-repo'

      FileUtils.rm_r 'target/test/bootstrap' if File.exist? 'target/test/bootstrap'

      deps = described_class.download_dependencies('target/test/bootstrap',
                                                   local_repo: 'target/test/bootstrap-repo')
      jars = (deps[:exists] + deps[:downloaded]).map { |jar| jar.values.first }

      described_class.install_dependencies_to_local_repo(
        deps[:downloaded], jars,
        local_repo: 'target/test/bootstrap-repo',
        dependencies_yml: File.expand_path('../../../../jar_dependencies.yml', __dir__)
      )

      deps = described_class.check_local_repo_for_deps('target/test/bootstrap-repo')
      deps[:exists].map { |x| x.keys[0] }.should eql(described_class.dependencies)
    end

    context 'check_local_repo_for_deps' do
      it 'local repo does not contain jars' do
        deps = described_class.check_local_repo_for_deps(
          'not-existant-dir',
          dependencies_yml: File.expand_path('../../../../jar_dependencies.yml', __dir__)
        )

        result = {
          missing: described_class.dependencies,
          exists: [],
          downloaded: []
        }

        deps.should eql(result)
      end

      it 'local repo contains jars' do
        deps = described_class.check_local_repo_for_deps

        ((deps[:downloaded] + deps[:exists]).map do |x|
          x.keys[0]
        end + deps[:missing]).should =~ described_class.dependencies
      end
    end
  end
end
