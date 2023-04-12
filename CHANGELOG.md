## Naether Changelog

### 0.15.0 (June 3, 2015)

* Compile using Java 1.7

#### 0.15.?

* Neglect

#### 0.15.6 (Jan 16, 2020)

* Use HTTPS for maven repos.

#### 0.15.7 (Jan 17, 2020)

* Make rake a requirement since it is used in the post deploy.

#### 0.15.9 (Apr 12, 2023)

* Remove Gemfile.lock as an artifact in the gem

#### 0.15.10 (Apr 12, 2023)

* Remove dependency on jeweler (which was pulling in the psych dep) in the gemspec.

### 0.14.0 (Feburary 13, 2015)

* Add support for NAETHER_MIRROR env when bootstraping Naether.
 
#### 0.14.1 (Feburary 18, 2015)

* Add support for NAETHER_MIRROR env in Java

#### 0.14.2 (May 5, 2015)

* Fix Notation#to_notation [(LockJar#29)](https://github.com/mguymon/lock_jar/issues/29)

#### 0.14.3 (May 20, 2015)

* Loosen dep on RJB, allow 1.4 - 1.5

### 0.13.0 (January 20, 2014)

* Fix check that `Naether::Configuration` is initialized [(#9)](https://github.com/mguymon/naether/issues/9)
* Updated Jeweler dep

#### 0.13.5 (March 6, 2014)

* Set default logging level from LogBack to ERROR
* Use JDK logging instead of Logback to avoid conflict on logback.xml

#### 0.13.7 (March 14, 20014)

* Use simple logger instead of JDK logger, remove slf4j bridge for jdk and log4j [(#13)](https://github.com/mguymon/naether/pull/13) [[thanks ramarnat]](https://github.com/ramarnat)

### 0.12.0 (October 1, 2013)

* Add support for executing Maven goals from Naether
* Hookup Travis for integration tests for Java

#### 0.12.1 (October 2, 2013)

* Use ModelBuilder to load POM instead of MavenXpp3Reader

#### 0.12.2 (October 2, 2013)

* Check MAVEN_HOME env for excuting the Maven goal invoker
