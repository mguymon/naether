module Naether
  
  #
  # Helper for handling Maven notations, supports notations:
  #  * artifactId:groupId:version
  #  * artifactId:groupId:type:version 
  #  * artifactId:groupId:type:classifier:version 
  #
  class Notation
    attr_reader :group, :artifact, :version, :classifier, :type
    
    PATTERN = Regexp.compile( '^(.+?):(.+?):(.+?)(:(.+?)(:(.+))?)?$' )
    
    def initialize(notation)
      if notation =~ PATTERN
        @group = Regexp.last_match(1) 
        @artifact = Regexp.last_match(2)
        
        # artifactId:groupId:type:classifier:version 
        if Regexp.last_match(7)
          @type = Regexp.last_match(3)
          @classifier = Regexp.last_match(5)
          @version = Regexp.last_match(7)
          
        # artifactId:groupId:type:version 
        elsif Regexp.last_match(5)
          @type = Regexp.last_match(3)
          @version = Regexp.last_match(5)
        # artifactId:groupId:version -
        else
          @type = 'jar'
          @version = Regexp.last_match(3)
        end
          
      end
    end

    def to_notation
      "#{group}:#{artifact}:#{type}#{":#{classifier}" if classifier}:#{version}"
    end
  end
end