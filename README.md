substeps-framework [![Build Status](https://travis-ci.org/Substeps/substeps-framework.svg)](https://travis-ci.org/Substeps/substeps-framework)&nbsp;[![Maven Central](https://img.shields.io/maven-central/v/org.substeps/substeps-core.png?label=substeps)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.substeps)
===================

[![Join the chat at https://gitter.im/Substeps/substeps-framework](https://badges.gitter.im/Substeps/substeps-framework.svg)](https://gitter.im/Substeps/substeps-framework?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Welcome to the substeps-framework project!

The new home for:  
[![Maven Central](https://img.shields.io/maven-central/v/org.substeps/substeps-core-api.png?label=substeps-api)](https://maven-badges.herokuapp.com/maven-central/org.substeps/substeps-core-api)  
 [![Maven Central](https://img.shields.io/maven-central/v/org.substeps/substeps-core.png?label=substeps-core)](https://maven-badges.herokuapp.com/maven-central/org.substeps/substeps-core)  
 [![Maven Central](https://img.shields.io/maven-central/v/org.substeps/substeps-maven-plugin.png?label=substeps-maven-plugin)](https://maven-badges.herokuapp.com/maven-central/org.substeps/substeps-maven-plugin)  
 [![Maven Central](https://img.shields.io/maven-central/v/org.substeps/substeps-junit-runner.png?label=substeps-junit-runner)](https://maven-badges.herokuapp.com/maven-central/org.substeps/substeps-junit-runner)  
 [![Maven Central](https://img.shields.io/maven-central/v/org.substeps/substeps-ant-runner.png?label=substeps-ant-runner)](https://maven-badges.herokuapp.com/maven-central/org.substeps/substeps-ant-runner)  
 [![Maven Central](https://img.shields.io/maven-central/v/org.substeps/substeps-glossary-builder.png?label=substeps-glossary)](https://maven-badges.herokuapp.com/maven-central/org.substeps/substeps-glossary-builder)

Requirements
------------
 * Java 8

1.0.6
-----
* Redacted some of the output of the config
* Passed the name of the running feature and scenario into the context.
* RemoteWebDriverFactory sets up additional information to pass though via capabilities (current git branch, version)
* Made the report builder more robust when there are no tests run
* Modified the Substeps exceptions to limit the stack trace
* Improved error message when no tests are run
* escaped < and > in the glossary data
* Corrected the path to the screenshot images to be relative so that they work when served off a server and locally.
* various sonar fixes
* Upgraded guava to 21.0 - required to support selenium 3.3 and latest firefox drivers

1.0.5
-----
* Single Maven plugin, glossary functionality moved into the same plugin that provides execution and report building, 
  original glossary-builder kept in place to avoid breakages.  Removed XML variant of the Glossary in favour of json.
  

1.0.4
-----
* Added a checkbox to the report to optionally hide skipped steps
* Substeps Mojos - Failures should prevent the Maven install phase from running; The runner mojo will throw a MojoFailureException only if the verify phase is not planned.  The Report Builder will throw any such exception if one is encountered in the maven session.
* Report enhancements
  * Upgraded Bootstrap to 3.3.7
  * 3 progress bars rather than one, toggle to show data in original tabular form
  * Step implementation method usage report (Beta)
  * Replaced hand rolled recursive File listing with commons.io implementation
* Fixed Issue #24 - ExecutionResultsCollector didn't work with forked mode
* Added 3rd colour to usage report pie charts to show not run state
* Corrected the path to the screenshots in the report data and the final report
* Replaced Apache config with [Typesafe Config](https://github.com/typesafehub/config) - similar functionality but provides better nesting of properties, variable substitution
* Added System property switch to use original properties files over new .conf files (`-Dsubsteps.use.dot.properties=true`)
* Enable any parameters to be substituted with values from config - user ${config.expression}. Delimitters can be specified and Charset conversion too, see core-api reference.conf for details 
* Enabled arguments to be evaluated at runtime against objects in the execution context

1.0.3
-----
* Added capability for outline descriptions to contain parameter placeholders that can be populated from the example tables, each iteration of the outline potentially then being named something different.
* Default core config
* Uncalled and Unused step impls written to json file rather than logs, overridable via a config property
* Added scala into the mix
* Execution now generates number of data files, report now generated from those files.
* Split up the Maven runner into two, one to run, the other to build the report in the verify phase
* Maven runner mojo doesn't throw a build failed exception, just adds as a result - allows the report to be built and subsequent tear down phases to run


1.0.0
-----
* Forked from G2G3.Digital as no longer being maintained.  Group renamed to org.substeps and version number reset to 1.0



com.technophobia.substeps releases
==================================

2.0.0
-----
 * New single repo to make ongoing maintenance / build dependencies easier
 * Enabled a scenario name to be used in addition to a feature file to restrict what gets run
 * Travis-CI now produces publicly accessible snapshot builds
 * Java 8 requirement, we haven't included any Java 8 features ourselves yet, but the latest libraries we use do

Useful Links
------------
 * [New Substeps docs !](http://substeps.github.io/)
 * [Original Substeps documentation](http://substeps.g2g3.digital) - http://substeps.g2g3.digital
 * [Substeps Google group](https://groups.google.com/forum/?hl=en-GB#!forum/substeps) - https://groups.google.com/forum/?hl=en-GB#!forum/substeps
 
