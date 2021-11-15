# (B)ihan's (Lox) (I)ntepreter

## Running
### Bash script
There's a bash script to run the interpreter.
You'll need to build it first with the -b flag or with `ant jar`.
```bash
$ # build and run
$ ./bloxi -b
$ # running the REPL
$ ./bloxi
$ # running a script
$ ./bloxi script.lox
```

### Running manually
```bash
$ # build the jar
$ ant jar
$ # running the REPL
$ java -jar build/jar/bloxi.jar
$ # running a script
$ java -jar build/jar/bloxi.jar script.lox
```

## Tests
This section assumes you've cloned the original repo in `orig` folder.
If you've cloned it to another path, change the `test` script and `bloxi` script
to reflect that.

### Prerequisites
Install the test suite dependencies:
```bash
$ cd orig
$ make get
```

### Running the tests
```bash
$ # run all tests
$ ./test
$ # run a specific suite
$ ./test suite_name
```
