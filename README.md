# EvoMaster: A Tool For Automatically Generating System-Level Test Cases


![](docs/img/carl-cerstrand-136810_compressed.jpg  "Photo by Carl Cerstrand on Unsplash")

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.evomaster/evomaster-client-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.evomaster/evomaster-client-java)
[![Build Status](https://travis-ci.org/EMResearch/EvoMaster.svg?branch=master)](https://travis-ci.org/EMResearch/EvoMaster)
[![CircleCI](https://circleci.com/gh/EMResearch/EvoMaster.svg?style=svg)](https://circleci.com/gh/EMResearch/EvoMaster)
[![codecov](https://codecov.io/gh/EMResearch/EvoMaster/branch/master/graph/badge.svg)](https://codecov.io/gh/EMResearch/EvoMaster)
<!---
Needs auth :(
[[JaCoCo]](https://circleci.com/api/v1.1/project/github/arcuri82/evomaster/latest/artifacts/0/home/circleci/evomaster-build/report/target/site/jacoco-aggregate/index.html)
-->



### Summary 

_EvoMaster_ ([www.evomaster.org](http://evomaster.org)) is a tool 
that automatically *generates* system-level test cases.
Internally, it uses an [Evolutionary Algorithm](https://en.wikipedia.org/wiki/Evolutionary_algorithm) 
and [Dynamic Program Analysis](https://en.wikipedia.org/wiki/Dynamic_program_analysis)  to be 
able to generate effective test cases.
The approach is to *evolve* test cases from an initial population of 
random ones, trying to maximize measures like code coverage and fault detection.


__Key features__:

* At the moment, _EvoMaster_ targets RESTful APIs compiled to 
  JVM __8__ and __11__ bytecode. Might work on other JVM versions, but we provide __NO__ support for it.

* The APIs must provide a schema in [OpenAPI/Swagger](https://swagger.io) 
  format (either _v2_ or _v3_).

* The tool generates _JUnit_ (version 4 or 5) tests, written in either Java or Kotlin.

* _Fault detection_: _EvoMaster_ can generate tests cases that reveal faults/bugs in the tested applications.
  Different heuristics are employed, like checking for 500 status codes and mismatches from the API schemas. 

* Self-contained tests: the generated tests do start/stop the application, binding to an ephemeral port.
  This means that the generated tests can be used for _regression testing_ (e.g., added to the Git repository
  of the application, and run with any build tool such as Maven and Gradle). 

* Advanced _whitebox_ heuristics: _EvoMaster_ analyses the bytecode of the tested applications, and uses
  several heuristics such as _testability transformations_ and _taint analysis_ to be able to generate 
  more effective test cases. 

* SQL handling: _EvoMaster_ can intercept and analyse all communications done with SQL databases, and use
  such information to generate higher code coverage test cases. Furthermore, it can generate data directly
  into the databases, and have such initialization automatically added in the generated tests. 
  At the moment, _EvoMaster_ supports _H2_ and _Postgres_ databases.  

* _Blackbox_ testing mode: can run on any API (regardless of its programming language), 
  as long as an OpenAPI schema is provided. However, results will be worse than whitebox testing (e.g., due
  to lack of bytecode analysis).



__Known limitations__:

* To be used for _whitebox_ testing, users need to write a [driver manually](docs/write_driver.md).
  We recommend to try _blackbox_ mode first (should just need a few minutes to get it up and running) to get
  an idea of what _EvoMaster_ can do for you.  

* Execution time: to get good results, you might need to run the search for several hours. 
  We recommend to first try the search for 10 minutes, just to get an idea of what type of tests can be generated.
  But, then, you should run _EvoMaster_ for something like between 1 and 24 hours (the longer the better, but
  it is unlikely to get better results after 24 hours).
  
* External services (e.g., other RESTful APIs): currently there is no support for them (e.g., to automatically mock them).
  It is work in progress.
  
* NoSQL databases (e.g., MongoDB): currently no support. It is work in progress. 

* Failing tests: the tests generated by _EvoMaster_ should all pass, and not fail, even when they detect a fault.
  In those cases, comments/test-names would point out that a test is revealing a possible fault, while still passing.
  However, in some cases the generated tests might fail. This is due to the so called _flaky_ tests, e.g., when
  a test has assertions based on the time clock (e.g., dates and timestamps). 
  There is ongoing effort to address this problem, but it is still not fully solved.   

<!--### Videos---> 
<!-- 
<div>Icons made by <a href="https://www.flaticon.com/authors/freepik" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a></div> 
-->

[![](docs/img/video-player-flaticon.png)](https://youtu.be/3mYxjgnhLEo) 

A [short video](https://youtu.be/3mYxjgnhLEo) (5 minutes)
shows the use of _EvoMaster_ on one of the 
case studies in [EMB](https://github.com/EMResearch/EMB). 


### Hiring

Each year we usually have funding for _postdoc_ and _PhD student_ positions to work on this project (in Oslo, Norway).
For more details on current vacancies, see our group page at [AISE Lab](https://emresearch.github.io/).

### Examples

![](docs/img/evomaster_console.png)


The following code is an example of one test that was automatically
generated by _EvoMaster_ for a REST service called 
"scout-api" (see [EMB repository](https://github.com/EMResearch/EMB)).
The generated test uses the [RestAssured](https://github.com/rest-assured/rest-assured) library.

```
@Test
public void test_36_with500() throws Exception {
        
   String location_media_files = "";
        
   String id_0 = given().accept("application/json")
                .header("Authorization", "ApiKey moderator") // moderator
                .contentType("application/json")
                .body(" { " + 
                    " \"uri\": \"hXf3e8B3ikGtuGjT\", " + 
                    " \"name\": \"nkeUfXVTC\", " + 
                    " \"copy_right\": \"\" " + 
                    " } ")
                .post(baseUrlOfSut + "/api/v1/media_files")
                .then()
                .statusCode(200)
                .assertThat()
                .contentType("application/json")
                .body("'uri'", containsString("hXf3e8B3ikGtuGjT"))
                .body("'name'", containsString("nkeUfXVTC"))
                .body("'copy_right'", containsString(""))
                .extract().body().path("id").toString();
                
   location_media_files = "/api/v1/media_files/" + id_0;
        
   given().accept("*/*")
                .header("Authorization", "ApiKey moderator") // moderator
                .get(resolveLocation(location_media_files, baseUrlOfSut + "/api/v1/media_files/1579038228/file"))
                .then()
                .statusCode(500) // se/devscout/scoutapi/resource/MediaFileResource_268_downloadFile
                .assertThat()
                .contentType("application/json")
                .body("'code'", numberMatches(500.0));
}
```

In this automatically generated test, a new resource is first created with a _POST_ command.
The _id_ of this newly generated resource is then extracted from the _POST_ response, and used in the URL
of a following _GET_ request on a sub-resource.
Such _GET_ request does break the backend, as it returns a __500__ HTTP status code.
The last line executed in the business logic of the backend is then printed as comment, to help debugging this fault.    


The generated tests are self-contained, i.e., they 
start/stop the REST server by themselves:

```
    private static SutHandler controller = new em.embedded.se.devscout.scoutapi.EmbeddedEvoMasterController();
    private static String baseUrlOfSut;
    
    
    @BeforeClass
    public static void initClass() {
        baseUrlOfSut = controller.startSut();
        assertNotNull(baseUrlOfSut);
        RestAssured.urlEncodingEnabled = false;
    }
    
    
    @AfterClass
    public static void tearDown() {
        controller.stopSut();
    }
    
    
    @Before
    public void initTest() {
        controller.resetStateOfSUT();
    }
```

The ability of starting/resetting/stopping the tested application is critical for using the generated 
tests in _Continuous Integration_ (e.g., Jenkins, Travis and CircleCI).
However, it requires to write a [_driver_](docs/write_driver.md) to tell _EvoMaster_ how to do 
such start/reset/stop.
  

A generated test is not only going to be a sequence of HTTP calls toward a running application.
_EvoMaster_ can also set up the _environment_ of the application, like automatically adding all the
needed data into a SQL database.




### Documentation


* [Download EvoMaster](docs/download.md)
* [Build EvoMaster from source](docs/build.md)
* [Console options](docs/options.md)
* [OpenApi/Swagger Schema](docs/openapi.md)
* [Using EvoMaster for Black-Box Testing (easier to setup, but worse results)](docs/blackbox.md)
* [Using EvoMaster for White-Box Testing (harder to setup, but better results)](docs/whitebox.md)
    * [Write an EvoMaster Driver for White-Box Testing](docs/write_driver.md)
* [Academic papers related to EvoMaster](docs/publications.md)
* [Slides of presentations/seminars](docs/presentations.md)
* [Notes for developers contributing to EvoMaster](docs/for_developers.md)



### How to Contribute

There are many ways in which you can contribute.
If you found _EvoMaster_ of any use, _the easiest
way to show appreciation is to **star** it_.
Issues and feature requests can be reported on
the [issues](https://github.com/EMResearch/EvoMaster/issues) page:  
  
* *Bugs*: as for any bug report, the more detailed
  you can be the better.
  If you are using _EvoMaster_ on an open source project,
  please provide links to it, as then it is much easier
  to reproduce the bugs.
  
* *Documentation*: if you are trying to use _EvoMaster_, but the instructions
  in these notes are not enough to get you started, 
  then it means it is a "bug" in the documentation, which then would need
  to be clarified. 
  
* *Feature Requests*: to improve _EvoMaster_,
  we are very keen to receive feature requests, although of course we cannot
  guarantee when they are going to be implemented, if implemented at all. 
  As researchers, we want to know what are the problems that engineers in industry
  do face, and what could be done to improve _EvoMaster_ to help them.
  
  
* *Pull Requests*: we are keen to receive PRs, as long as you agree
  with the license of _EvoMaster_, and as long as you are allowed by your employer to contribute
  to open-source projects. However, before making a PR, you should read
  the [notes for developers](docs/for_developers.md).  


* *Industry Collaborations*: to evaluate the effectiveness of _EvoMaster_, we need case studies.
  There are some open-source projects that can be used (e.g., which we selected and aggregated in the
  [EMB repository](https://github.com/EMResearch/EMB)).
  But open-source applications are not necessarily representative of software developed in industry.
  Therefore, we "collaborate" with different companies (e.g., [Universitetsforlaget](https://www.universitetsforlaget.no/)),
  to apply _EvoMaster_ on their systems.
      
  * *Benefits for us*: access to the source code of real, industrial systems (of course, under NDAs). 
    It makes easier to publish academic papers, and to get funding from the research councils
    to improve _EvoMaster_ even further.  
  * *Benefits for the industrial collaborators*: 
    
    1. getting priority on new features and bug fixing.
    2. "free" human resources (MSc/PhD students and researchers) that try to break your systems and
        find faults in them.    
    
* *Academic Collaborations*: we are keen to hear from students and researchers that want to 
  collaborate on topics in which _EvoMaster_ can be used on, or extended for.
  For example, it is possible to have visiting PhD students in our lab.
  However, all communications about academic collaborations should not be done here on GitHub,
  but rather by email, directly to Prof. A. Arcuri. 




### Funding

_EvoMaster_ has been funded by: 
* 2020-2025: a 2 million Euro grant by the European Research Council (ERC),
as part of the *ERC Consolidator* project 
<i>Using Evolutionary Algorithms to Understand and Secure Web/Enterprise Systems</i>.
*  2018-2021: a 7.8 million Norwegian Kroner grant  by the Research Council of Norway (RCN), 
as part of the Frinatek project <i>Evolutionary Enterprise Testing</i>.  


### License
_EvoMaster_'s source code is released under the LGPL (v3) license.
For a list of the used third-party libraries, you can directly see the root [pom.xml](./pom.xml) file.
For a list of code directly imported (and then possibly modified/updated) from 
other open-source projects, see [here](./docs/reused_code.md).


### ![](https://www.yourkit.com/images/yklogo.png)

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of 
<a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>
and 
<a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.


