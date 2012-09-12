# Naether

Naether is a Java Dependency Resolver using Maven's [Aether](https://github.com/sonatype/sonatype-aether) 
that can be used by Ruby or Java.

<https://github.com/mguymon/naether>

[RDoc](http://rubydoc.info/gems/naether/frames)
 
[JavaDoc](http://mguymon.github.com/naether/apidocs/index.html)

## Install

### Ruby

Supports Ruby 1.8.7, Ruby 1.9.3, and JRuby 1.6.7

    gem install naether
    
The jar dependencies for Naether will automatically be downloaded when the gem installs.

### Java

    <dependency>
      <groupId>com.tobedevoured.naether</groupId>
      <artifactId>naether</artifactId>
      <version>0.9.0</version>
    </dependency>
    

## About

[Naether](https://github.com/mguymon/naether/blob/master/src/main/java/com/slackworks/naether/Naether.java) 
is a wrapper for [Aether](https://github.com/sonatype/sonatype-aether), the Maven dependency resolution framework. 
[Naether.rb](https://github.com/mguymon/naether/blob/master/src/main/ruby/naether.rb) provides access to Aether 
from Ruby. JRuby is natively supported, other Ruby VMs will use [Rjb](http://rjb.rubyforge.org) to proxy over JNI.

## Features

* Transitive resolution of Jars
* Override resolution with local Jars
* Download Jars
* Deploy Jars
* Load dependencies from a Maven pom.xml
* Can create a Maven pom.xml

## License

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with this
work for additional information regarding copyright ownership.  The ASF
licenses this file to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
