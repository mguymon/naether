require  File.expand_path(File.join(File.dirname(__FILE__), '../spec_helper'))
require 'src/main/ruby/naether'
require 'src/main/ruby/naether/java'

describe Naether::Java do
  context "Class" do
    it "should load jars" do
      loaded_jars = Naether::Java.load_paths( 'src/test/resources/commons-beanutils-1.8.3.jar' )
      loaded_jars.should include(File.expand_path('src/test/resources/commons-beanutils-1.8.3.jar'))
      
    end
    
    it "should create an instance of com.tobedevoured.naether.maven.Project" do
      @naether = Naether.create
      instance = Naether::Java.create("com.tobedevoured.naether.maven.Project")
      instance.getClass().getName().should eql "com.tobedevoured.naether.maven.Project" 
    end
    
    it "should load paths" do
      loaded_paths = Naether::Java.load_paths( 'src/test/classes/commons-io' )
      loaded_paths.should include(File.expand_path('src/test/classes/commons-io'))
      
      file = Naether::Java.create( "java.io.File", "VERSION" )
      version = Naether::Java.exec_static_method( 'org.apache.commons.io.FileUtils', 'readFileToString',  file ).strip
      version.should eql IO.read("VERSION").strip
    end
    
  end
end
