require 'src/main/ruby/naether'

describe Naether::Bootstrap do
  context "Class" do
    it "should write jar dependencies yml" do
      Naether::Bootstrap.write_dependencies("target", "target/jar_dependencies.yml")
      File.exists?( "target/jar_dependencies.yml" ).should be_true
      
      deps = YAML.load_file( "target/jar_dependencies.yml" ) 
      deps.size.should > 0
    end
  end
end