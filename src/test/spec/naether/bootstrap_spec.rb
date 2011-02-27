require 'src/test/spec/spec_helper'
require 'src/main/ruby/naether'

describe Naether::Bootstrap do
  context "Class" do
    it "should write jar dependencies yml" do
      Naether::Bootstrap.write_dependencies("target")
    end
  end
end