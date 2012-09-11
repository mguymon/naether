
class Naether
  class Configurator
    def initialize(data={})
      gem_dir = File.expand_path("#{File.dirname(__FILE__)}/../../")
      
      version = nil
      
      # Load VERSION file from gem to VERSION var
      if File.exists?( File.expand_path("#{File.dirname(__FILE__)}/../../VERSION") )
        version = IO.read(File.expand_path("#{File.dirname(__FILE__)}/../../VERSION")).strip
          
      # VERSION file not found in gem dir, assume in current path, e.g.running from checkout
      else
        version = IO.read(File.expand_path("VERSION")).strip
      end
      
      @data = {
        :gem_dir =>     gem_dir,
        :naether_jar => File.join( gem_dir, "naether-#{version}.jar"),
        :platform =>    ($platform || RUBY_PLATFORM[/java/] || 'ruby'),
        :version =>     version
      }
      
      update!(data)
    end
  
    def update!(data)
      data.each do |key, value|
        self[key] = value
      end
    end
  
    def [](key)
      @data[key.to_sym]
    end
  
    def []=(key, value)
      if value.class == Hash
        @data[key.to_sym] = Config.new(value)
      else
        @data[key.to_sym] = value
      end
    end
  
    def method_missing(sym, *args)
      if sym.to_s =~ /(.+)=$/
        self[$1] = args.first
      else
        self[sym]
      end
    end
  end
  
  unless defined?(Naether::Configuration)
    Naether::Configuration = Naether::Configurator.new
  end
end