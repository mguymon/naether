require  File.expand_path(File.join(File.dirname(__FILE__), '../spec_helper'))
require 'src/main/ruby/naether'
require 'fileutils'

describe Naether::Bootstrap do
  context "Class" do
    it "should write jar dependencies yml" do
      Naether::Bootstrap.write_dependencies("target", "target/jar_dependencies.yml")
      File.exists?( "target/jar_dependencies.yml" ).should be_true
      
      deps = YAML.load_file( "target/jar_dependencies.yml" ) 
      deps.size.should > 0
    end
    
    it "should download dependencies" do
      if File.exists? 'target/test/bootstrap'
        FileUtils.rm_r 'target/test/bootstrap'
      end
      
      deps = Naether::Bootstrap.download_dependencies( "target/test/bootstrap", :local_repo => 'non-existant-dir' )
      
      deps[:missing].should eql( Naether::Bootstrap.dependencies )
      Naether::Bootstrap.dependencies.size.should eql(deps[:missing].size)
      
    end
    
    it "should install dependencies to local repo" do
      if File.exists? 'target/test/bootstrap-repo'
        FileUtils.rm_r 'target/test/bootstrap-repo'
      end
            
      deps = Naether::Bootstrap.check_local_repo_for_deps('target/test/bootstrap-repo')
      deps[:missing].should eql( Naether::Bootstrap.dependencies )
      Naether::Bootstrap.install_dependencies_to_local_repo( 
        'target/test/bootstrap', :local_repo => 'target/test/bootstrap-repo' )
      
      deps = Naether::Bootstrap.check_local_repo_for_deps('target/test/bootstrap-repo')
      deps[:exists].map {|x| x.keys[0] }.should eql( Naether::Bootstrap.dependencies )            
    end
    
    context "check_local_repo_for_deps" do
      it "local repo does not contain jars" do
        deps = Naether::Bootstrap.check_local_repo_for_deps( 'not-existant-dir' )
        
        result = {
          :missing => Naether::Bootstrap.dependencies,
          :exists => []
        }
        
        deps.should eql( result )
      end
      
      it "local repo contains jars" do
        deps = Naether::Bootstrap.check_local_repo_for_deps
        
        deps[:exists].map {|x| x.keys[0] }.should eql( Naether::Bootstrap.dependencies )
      end
    end
  end
end