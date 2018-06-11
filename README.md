# SUF4OC

## What is it

This program contains algorithms for learning Ordinal Classifiers made of combinations of Sugeno Utility Functionals.

## The jar file

It contains a compiled version of the programm, that you can download and launch. The command line for this is
```
java -jar SUF4OC.jar [task] [options]
```

### Tasks
The task to execute is specified by the first argument:
* ``classification``: is the only task enabled for now. It requires to specify several options. 

### Options
* ``--nonparametric``: when this option is present, the nonparametric version of the algorithm is used (rho=1.0).
* ``-data``: the path of the file containing the data (see next section for the form of the data).
* ``-rhoStart``: lowest value of rho used in the test, default: 0.95
* ``-rhoEnd``: highest value of rho used in the test, default: 1.0
* ``-rhoSteps``: amount of change in the value of rho between each test, default: 0.01
* ``-nbruns``: number of times the tests are run. Results are averaged from all runs default: 1

### Examples
```java -jar SUF4OC.jar classification -data myfolder/myfile.txt -rhoStart 0.95 -rhoEnd 1.0 -rhoSteps 0.01 -nbruns 10```

```java -jar SUF4OC.jar classification -data myfolder/myfile.txt --nonparametric -nbruns 10```

## The data file

Todo.
