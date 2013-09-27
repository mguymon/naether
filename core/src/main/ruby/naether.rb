# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with this
# work for additional information regarding copyright ownership. The ASF
# licenses this file to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.


#
# Java dependency resolver
#
# @author Michael Guymon
# @see https://github.com/mguymon/naether/tree/master/core
#
require "#{File.dirname(__FILE__)}/naether/configuration"

module Naether


  class << self

    # List of Java dependencies needed to bootstrap Naether
    #
    # @param [String] dep_file path
    # @see {Naether::Bootstrap#dependencies}
    def bootstrap_dependencies( dep_file=nil )
      require "#{File.dirname(__FILE__)}/naether/bootstrap"
      Naether::Bootstrap.dependencies( dep_file )
    end

    #
    # Helper for platform detection
    #
    def platform
      Naether::Configuration.platform
    end

    #
    # Loads all jars creates a new instance of Naether
    #
    # @param [Array<String>] jars of paths
    # @return [Naether]
    def create_from_jars( jars )
      require "#{File.dirname(__FILE__)}/naether/java"
      Naether::Java.internal_load_paths( jars )
      create
    end

    def create
      require "#{File.dirname(__FILE__)}/naether/runtime"
      Naether::Runtime.new
    end
  end
end
