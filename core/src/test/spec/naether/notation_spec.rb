require  File.expand_path(File.join(File.dirname(__FILE__), '../spec_helper'))
require 'src/main/ruby/naether/notation'

describe Naether::Notation do
  it "should handle groupId:artifactId:version" do
    notation = Naether::Notation.new('groupId:artifactId:version')
    notation.group.should eql 'groupId'
    notation.artifact.should eql 'artifactId'
    notation.version.should eql 'version'
    notation.classifier.should be_nil
    notation.type.should eql 'jar'
  end
  
  it "should handle groupId:artifactId:type:version" do
    notation = Naether::Notation.new('groupId:artifactId:type:version')
    notation.group.should eql 'groupId'
    notation.artifact.should eql 'artifactId'
    notation.version.should eql 'version'
    notation.classifier.should be_nil
    notation.type.should eql 'type'
  end
   
  it "should handle groupId:artifactId:type:classifier:version" do
    notation = Naether::Notation.new('groupId:artifactId:type:classifier:version')
    notation.group.should eql 'groupId'
    notation.artifact.should eql 'artifactId'
    notation.version.should eql 'version'
    notation.classifier.should eql 'classifier'
    notation.type.should eql 'type'
   end
end