require "#{File.dirname(__FILE__)}/../configuration"

# :title:Naether::Java::Ruby
#
# Sngleton that handles Java interactions for Ruby using RJB. Providers helpers for
# nomalizing accesss.
#
# = Authors
# Michael Guymon
#
class Naether
  class Java

    class Ruby
      
      attr_reader :loaded_paths, :class_loader
      
      def initialize
        require 'rjb' 
        
        naether_jar = Naether::Configuration.naether_jar
              
        # Call Rjb::load with an empty classpath, incase Rjb::load has already been called
        java_opts = (ENV['JAVA_OPTS'] || ENV['JAVA_OPTIONS']).to_s.split
        Rjb::load("", java_opts)
        
        @loaded_paths = []
        load_paths( naether_jar )
        
        class_loader_class = Rjb::import( "com.tobedevoured.naether.PathClassLoader" )
        @class_loader = class_loader_class.new()
          
        internal_load_paths( naether_jar )
        
      end
      
      def create( target_class, *args )
        #@class_loader.newInstance(target_class, *args )
        if args.size > 0
          @class_loader._invoke('newInstance', 'Ljava.lang.String;[Ljava.lang.Object;', target_class, args )
        else
          @class_loader._invoke('newInstance', 'Ljava.lang.String;', target_class )
        end
      end
      
      def exec_static_method( target_class, target_method, params, types = nil ) 
        unless params.is_a? Array
          params = [params]
        end
        
        if types
          unless types.is_a? Array
             types = [types]
          end
        end
        result = nil
        if params.nil?
          result = @class_loader._invoke('execStaticMethod','Ljava.lang.String;Ljava.lang.String;', target_class, target_method )
        elsif types.nil?
          result = @class_loader._invoke('execStaticMethod','Ljava.lang.String;Ljava.lang.String;Ljava.util.List;', target_class, target_method, convert_to_java_list(params) )
        else
          result = @class_loader._invoke('execStaticMethod','Ljava.lang.String;Ljava.lang.String;Ljava.util.List;Ljava.util.List;', target_class, target_method, convert_to_java_list(params), convert_to_java_list(types) )
        end
        
        unless result.nil?
          # Force toString on java.lang.String otherwise the result will be a Rjb::Proxy
          if result.getClass().getName() == 'java.lang.String'
            result.toString()
          else
            result
          end
        end
      end
      
      def internal_load_paths(paths)
        loadable_paths = []
        unless paths.is_a? Array
          paths = [paths]
        end
        
        paths.each do |path|
          expanded_path = File.expand_path(path)
          if File.exists?( expanded_path )
            @class_loader.addPath( path )
            loadable_paths << expanded_path
          end
        end
        
        loadable_paths
      end
      
      def load_paths(paths)
        loadable_paths = []
        unless paths.is_a? Array
          paths = [paths]
        end
        
        paths.each do |path|
          expanded_path = File.expand_path(path)
          if !@loaded_paths.include?(expanded_path) && File.exists?( expanded_path )
            @loaded_paths << expanded_path
            Rjb::add_jar( expanded_path )
            loadable_paths << expanded_path
          end
        end
        
        loadable_paths
      end
      
      def convert_to_java_list( ruby_array ) 
        list = Rjb::import("java.util.ArrayList").new
        ruby_array.each do |item|
          list.add( item )
        end
        
        list
      end
      
      def convert_to_ruby_array( java_array, to_string = false )
        ruby_array = java_array.toArray()
        
        if to_string
          ruby_array = ruby_array.map { |x| x.toString()}
        end
        
        ruby_array
      end
      
      def convert_to_ruby_hash( java_hash, to_string = false )
        
        hash = {}
        keys = java_hash.keySet()
        iterator = keys.iterator()
        if to_string
          while iterator.hasNext()
            key = iterator.next().toString()
            hash[key] = java_hash.get( key ).toString()
          end
        else
          while iterator.hasNext()
            key = iterator.next()
            hash[key] = java_hash.get( key )              
          end
        end
        
        hash
        
      end
    end
    
  end
end