<p align="center">
<a href="https://plri.github.io/arden2bytecode"><img src="https://plri.github.io/arden2bytecode/images/logo.png" alt="Arden2ByteCode" height="90"></a>
</p>
[Arden2ByteCode](https://plri.github.io/arden2bytecode/) is a compiler for [Arden Syntax](https://en.wikipedia.org/wiki/Arden_syntax) with [Java Bytecode](https://en.wikipedia.org/wiki/Java_bytecode) output.


## Usage
Check out the [wiki](https://github.com/PLRI/arden2bytecode/wiki) to see how to [install and start using](https://github.com/PLRI/arden2bytecode/wiki/Installation) Arden2ByteCode, learn more about [Arden Syntax](https://github.com/PLRI/arden2bytecode/wiki/Basics) or for a list of [command line options](https://github.com/PLRI/arden2bytecode/wiki/Command-Line-Options).

Compile an MLM:
```sh
arden2bytecode -c hello_world.mlm
```

Compile and run an MLM:
```sh
arden2bytecode -r hello_world.mlm
```


## Building
To compile Arden2ByteCode, you first need to generate the parser using the SableCC parser generator:

- To build the parser with [Eclipse](https://eclipse.org/), import the project and choose *Project* &rArr; *Build project* from the menu. On the first build you need to refresh the Project (F5), so Eclipse finds the downloaded [dependencies](#dependencies).  
- To build with [Apache Ant](https://ant.apache.org/), `cd` into the project root and type `ant` at the command line.  

Both will automatically call the "sableCC" target in the [build.xml](build.xml) and generate the .java files for the parser. This can take some time, but will only happen if the parser does not exist or is out-of-date.

Check out the wiki for [detailed instructions](https://github.com/PLRI/arden2bytecode/wiki/Building).


## Dependencies
The dependencies are downloaded automatically if you use Ant or Eclipse to build the project. See the "dependencies" target in [build.xml](build.xml) for download links.

- [SableCC](http://www.sablecc.org/): A "compiler-compiler" which is used to generate the arden parser from a grammar file. Usage: `java -jar tools/sablecc.jar -d src/ src/arden.scc src/ardenConstants.scc`
- [JewelCli](http://jewelcli.lexicalscope.com/): A command line arguments parser.
- [JUnit](http://junit.org/): A testing framework to test the correct implementation and standard compliance.
- [Hamcrest-core](http://hamcrest.org/JavaHamcrest/): Used with JUnit to create short and concise tests.


## Testing
This project contains two test suites:
- An [implementation test suite](test/arden/tests/implementation) to test Arden2ByteCode  specific features (databases, loading MLMs from bytecode, etc.).
- A compiler independent [specification test suite](test/arden/tests/specification) to check for Arden Syntax standard conformance. Have a look at the suites [README file](test/arden/tests/specification/README.md) for more information.

To run the test suites you can use Eclipse or Ant:
- To test in Eclipse, right-click on a test suite and select *Run As* &rArr; *JUnit Test*.  
- To test with Ant, `cd` to the project root and type `ant test`. A report will be generated into the [report](report) directory.


## Standard Conformance
This compiler implements Arden Syntax 2.5 with the following exceptions:

Languages features not implemented:

* Include Statement
* Some string formatting specificiers are not implemented.
* Citation/links slots are not syntax checked.
* The compiler does not check that no languages features newer than the specified 'Arden Version' are used.


## Copyright and license
- Copyright 2004: University of British Columbia
- Copyright 2009-2010: Daniel Grunwald
- Copyright 2011-2012: Hannes Flicka
Portions (arden.scc)

See [LICENSE.md](LICENSE.md) for licensing information.
