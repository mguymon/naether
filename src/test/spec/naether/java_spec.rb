require 'src/main/ruby/naether'

describe Naether::Java do
  context "Class" do
    it "should load jars" do
      Naether::Java.load_jars_dir( 'target/lib' )
      loaded_jars = Naether::Java.loaded_jars
      
      missing = []
      Dir.glob( File.expand_path( 'target/lib/*.jar' ) ).each do |jar|
        if !loaded_jars.include? jar
          missing << jar
        end
      end
      
      if missing.size > 0
        fail( "Jars not loaded: #{missing}" )
      end
      
    end
    
    it "should create an instance of com.slackworks.naether.maven.Project" do
      Naether::Java.create_maven_project.getClass().getName().should eql "com.slackworks.naether.maven.Project" 
    end
    
  end
end