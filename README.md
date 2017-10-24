# Naether

[![Build Status](https://travis-ci.org/mguymon/naether.png?branch=master)](https://travis-ci.org/mguymon/naether)

Naether is a Java Dependency Resolver using Maven's [Aether](https://github.com/sonatype/sonatype-aether)
that can be used by Ruby or Java.

<https://github.com/mguymon/naether>

[RDoc](http://rubydoc.info/gems/naether/frames) | [JavaDoc](http://mguymon.github.com/naether/apidocs/index.html) | [Wiki](https://github.com/mguymon/naether/wiki)

## Install

### Ruby

JRuby is natively supported. Vanilla Ruby uses [Rjb](http://rjb.rubyforge.org) to proxy over JNI.

    gem install naether

The jar dependencies for Naether will automatically be downloaded when the gem installs.

#### Custom Maven Repo

You can override the default Maven Repo used in bootstraping Naether by setting the _NAETHER_MIRROR_ environment variable.

    NAETHER_MIRROR=http://my.local.repo/m2 gem install naether

The _NAETHER_MIRROR_ environment is also appended to the remote repositories used when resolving dependences.

### Java

    <dependency>
      <groupId>com.tobedevoured.naether</groupId>
      <artifactId>core</artifactId>
      <version>0.15.4</version>
    </dependency>

May have to add the Sonatype Repo if the sync to Maven Central is slow.

    <repositories>
      <repository>
        <id>oss.sonatype.org</id>
        <name>Sonatype Repository</name>
        <url>https://oss.sonatype.org/content/groups/public</url>
      </repository>
    </repositories>

## Usage

### Ruby

All of the Naether dependencies are loaded via a Custom ClassLoader.

#### Bootstraping

Naether [jar dependences](https://github.com/mguymon/naether/blob/master/jar_dependencies.yml)
must be loaded for Naether to work. This is easily done with the [bootstrap helper](http://rdoc.info/gems/naether/Naether/Bootstrap)

    Naether::Bootstrap.bootstrap_local_repo

#### Resolving Dependencies

    require 'rubygems'
    require 'naether'

    Naether::Bootstrap.bootstrap_local_repo
    naether = Naether.create
    naether.dependencies = [ "ch.qos.logback:logback-classic:jar:0.9.29", "junit:junit:jar:4.8.2" ]
    naether.resolve_dependencies()

    puts naether.dependencies_notation

Will output

    ["ch.qos.logback:logback-core:jar:0.9.29",
     "ch.qos.logback:logback-classic:jar:0.9.29",
     "junit:junit:jar:4.8.2",
     "org.slf4j:slf4j-api:jar:1.6.1" ]

[Additional documentation on Resolving Dependencies](https://github.com/mguymon/naether/wiki/Ruby-Resolving-Dependencies)

### Java

#### Resolving Dependencies
    import com.tobedevoured.naether.api.Naether;
    import com.tobedevoured.naether.impl.NaetherImpl;

    Naether naether = new NaetherImpl();
    naether.addDependency( "ch.qos.logback:logback-classic:jar:0.9.29" );
    naether.addDependency( "junit:junit:jar:4.8.2" );
    naether.resolveDependencies();
    System.out.println( naether.getDependenciesNotation().toString() );

Will output:

    ["ch.qos.logback:logback-core:jar:0.9.29",
     "ch.qos.logback:logback-classic:jar:0.9.29",
     "junit:junit:jar:4.8.2",
     "org.slf4j:slf4j-api:jar:1.6.1" ]

[Additional documentation on Resolving Dependencies](https://github.com/mguymon/naether/wiki/Java-Resolving-Dependencies)

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
