# Note pour mes relecteurs de thèse : la code ici n'est pas (encore) à jour ; il le sera bientôt.

## What is it

This program contains algorithms for learning Ordinal Classifiers made of combinations of Sugeno Utility Functionals. For more information about the method, see the paper Extracting Decision Rules from Qualitative Data via Sugeno Utility Functionals. Quentin Brabant, Miguel Couceiro, Henri Prade, Didier Dubois, Agnès Rico, IPMU 2018.

## The jar file

It contains a compiled version of the programm, that you can download and launch. The command line for this is
```
java -jar SUF4OC.jar [task] [options]
```

### Tasks
The task to execute is specified by the first argument:
* ``classification``: is the only task enabled for now. It requires to specify several options. A hyperparameter "rho" can be used. For maximizing accuracy, use rho=1. For more concise model, you can try lower values of rho (see Options).

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

Each row should be of the form ``x1,x2,....,xn,y`` where ``x1,x2,...,xn`` are the features/attributes/criteria of an object, and ``y`` is its label. All values must be numbers (note that each feature domain will be represented by as a totally ordered set, so the exact values of numbers do matter).

The first line is might be of the form ``option_1,option_2,....,option_n,option_n+1``, where option_i is either ``gain`` or ``cost``. If a feature is set to the same option as the label (both are set to gain or both are set to cost), then the feature has a "positive effect" (the higher the feature, the higher the label), otherwise it has a negative effect. The first line is optional. By default, the algorithm assumes that all features have a positive effect.

See files in the ``datasets`` folders for examples.
