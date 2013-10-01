require "#{File.dirname(__FILE__)}/../configuration"

# Handles Java interactions for Ruby using RJB. Providers helpers for
# nomalizing accesss.
#
# @author Michael Guymon
#
module Naether
  class Java

    class Ruby
      
      attr_reader :loaded_paths, :class_loader
      
      #
      # Creates new instance by loading the Naether jar to the parent ClassLoader
      # and creating the internal Naether ClassLoader
      #
      def initialize
        require 'rjb' 
        
        naether_jar = Naether::Configuration.naether_jar
              
        # Call Rjb::load with an empty classpath, incase Rjb::load has already been called
        java_opts = (ENV['JAVA_OPTS'] || ENV['JAVA_OPTIONS']).to_s.split

        begin
          Rjb::load("", java_opts)
        rescue StandardError => e
          if e.message == "can't create Java VM"
            puts "RJB was unable to find JVM, try setting JAVA_HOME env"
          end
          raise e
        end
        
        @loaded_paths = []
        load_paths( naether_jar )
        
        class_loader_class = Rjb::import( "com.tobedevoured.naether.PathClassLoader" )
        @class_loader = class_loader_class.new()
          
        internal_load_paths( naether_jar )
        
      end
      
      #
      # Create a Java Object from the Naether Class Loader
      #
      # @param [String] target_class to create
      # @param [Array] args Array of constructor arguments
      def create( target_class, *args )
        #@class_loader.newInstance(target_class, *args )
        if args.size > 0
          @class_loader._invoke('newInstance', 'Ljava.lang.String;[Ljava.lang.Object;', target_class, args )
        else
          @class_loader._invoke('newInstance', 'Ljava.lang.String;', target_class )
        end
      end
      
      #
      # Execute a Staic method on a Java class from the Naether Class Loader
      #
      # @param [String] target_class
      # @param [String] target_method
      # @param [Array] params Array of method parameters
      # @param [Array] types if defined, a Array of String classes of params type that lines up with params one to one.
      # @return [Object]
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
     
      #
      # Load a path into the internal Naether ClassLoader
      #
      # @param [Array] paths as an Array of String paths or a String path
      #
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
      
      #
      # Load a path onto the parent ClassLoader
      #
      # @param [Array] paths as an Array of String paths or a String path
      #
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
      
     #
     # Convert a Ruby Array to a java.util.ArrayList
     # 
     # @param [Array] ruby_array Array to convert to Java.util.ArrayList
     # @return [java.util.ArrayList]
     #
      def convert_to_java_list( ruby_array ) 
        list = Rjb::import("java.util.ArrayList").new
        ruby_array.each do |item|
          list.add( item )
        end
        
        list
      end
      
      #
      # Convert a java,util.List to a Ruby Array
      #
      # @param [java.util.ArrayList] java_array
      # @param [Boolean] to_string converts each element using toString
      # @return [Array]
      #      
      def convert_to_ruby_array( java_array, to_string = false )
        ruby_array = java_array.toArray()
        
        if to_string
          ruby_array = ruby_array.map { |x| x.toString()}
        end
        
        ruby_array
      end
      
      #
      # Convert a java.util.Map to a Ruby Hash
      #
      # @param [java.util.Map] java_hash
      # @param [Boolean] to_string converts each element using toString 
      # @return [Hash]
      #
      def convert_to_ruby_hash( java_hash, to_string = false )
        
        hash = {}
        unless java_hash.is_a? Hash
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
        else
          if to_string
            java_hash.each do |k,v|
              hash[k.toString()] = v.toString()
            end
          else
            hash = java_hash
          end
        end
        
        hash
        
      end
    end
    
  end
end