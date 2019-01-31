# RL-SUF / SRL

SRL and RL-SUF are two algorithms for learning monotonic classifiers. SRL learns a set of decision rules of the form
> if feature1 >= threshold1 and feature2 >= threshold2 and ... and featureN >= thresholdN then class >= threshold

while RL-SUF learns a model defined as the maximum of several SUFs (see [Q. Brabant, M. Couceiro, D. Dubois, H. Prade, A. Rico, Extracting Decision Rules from Qualitative Data via Sugeno Utility Functionals. IPMU, 253–265, 2018](https://hal.inria.fr/hal-01670924)).
This repository contains two jars ``SRL.jar`` and ``RL-SUF.jar``.


## SRL

``SRL.jar`` contains a compiled version of the SRL algorithm. The command line for launching it is
```
java -jar SRL.jar [options]
```

### Options
* ``-data [path]``: the argument ``[path]`` is the path of the file containing the data (see next section for how to format data). In case you want to reproduce the results of the paper, you can put ``VCDomLEMBenchmark`` instead of a path ; SRL will then be run on the 12 datasets from the ``datasets`` folder (``datasets`` has to be in the working directory).
* ``-reverse``: learn rules of the form
> if feature1 <= threshold1 and feature2 <= threshold2 and ... and featureN <= thresholdN then class <= threshold

instead of the rules of standard form.
* ``-longrules``: skip the step of the algorithm where the rules are simplified. The rules that are learned thus contain more conditions in the left hand side and are more prone to overfitting.
* ``-validation``: by default, SRL use the entire dataset to learn one classifier. With the option, ``-validation`` a 10-fold crossvalidation is performed.
	* ``-nbruns [value]``: number of times the crossvalidation is done. Results are averaged from all runs. By default, the number of run is set to 1.

### Examples
Suppose that SRL.jar is in the same directory as ``dataset1.txt``. The following command line
```
java -jar SRL.jar -data dataset1.txt -reverse
```

gives a result of the form

```
{
	x1 <= 14	==>	y <= 1
	x0 <= 5		==>	y <= 1
	x1 <= 10	==>	y <= 1
	x2 <= 11	==>	y <= 1
	x0 <= 8		x3 <= 13	==>	y <= 0
	x0 <= 12	x2 <= 12	==>	y <= 0
	x1 <= 9		x3 <= 4		==>	y <= 0
	x0 <= 10	x1 <= 9		x2 <= 10	==>	y <= 0
}
```

which represents the set of decision rules learned from the data in ``dataset1.txt``. For evaluating SRL on ``dataset1.txt``, launch

```
java -jar SRL.jar -validation -data dataset1.txt -nbruns 10
```

This gives a result of the form

```
             MER: 	0.081	
(over folds) std: 	0.018	
 (over runs) std:	0.0034	
             MAE: 	0.081	
(over folds) std: 	0.018	
 (over runs) std:	0.0034	
        nb rules: 	118.8	
    rule lengths: 	2.3	
Rule length distribution (max-length:ratio):
0:0.0 1:0.058 2:0.62 3:0.272 4:0.045 5:0.005 6:0.0
```

MER and MAE respectively stand for Missclassification Error Rate (the ratio of missclassified rows) and for Mean Absolute Error. Both error are measured on test data for each fold and averaged. Std over folds correspond to the standard deviation of error scores over the folds during one run. Std over runs is the standard deviation of the average error scores of each run (this line is displayed only if nbruns > 1). Nb rules and rule lengths are respectively the overall average number of rules in the model and the overall average size of rules.
Finally, rule length distribution give an average distribution of rules by size.



## RL-SUF

RLSUF.jar contains a compiled version of the RL-SUF algorithm. The command line for launching the algorithm on data is

```
java -jar SRL.jar [options]
```

* ``-data [path]``: same as for SRL.
* ``-reverse``: use a minimum of SUFs instead of a maximum of SUFs.
* ``rho [value]``: If this option is used, the program runs an additionnal step of pruning (which will remove some SUFs from the model). This step depends on a parameter ``rho``. The value of ``rho`` should usually be set close to 1. The lower the value of ``rho``, the more SUFs are removed from the model.
* ``-validation``: by default, SRL uses the entire dataset to learn one classifier. With the option, ``-validation`` a 10-fold crossvalidation is performed. When using this option, it is possible to compare the result for several values of ``rho``. The pruning step will be performed only if the values of ``-rhoStart`` and ``-rhoEnd`` are specified.
	* ``-rhoStart [value]``: lowest value of ``rho`` used in the test, default: 1.1
	* ``-rhoEnd [value]``: highest value of ``rho`` used in the test, default: 1.1
	* ``-rhoSteps [value]``: amount of change in the value of ``rho`` between each test, default: 0.01
	* ``-nbruns [value]``: number of times the crossvalidation is done. Results are averaged from all runs. By default, the number of runs is set to 1.



### Examples

Suppose that RLSUF.jar is in the same directory as ``dataset1.txt``.
The command 

```
java -jar RLSUF.jar -data dataset1.txt
```

gives a result of the form

```
{
<<
Focal sets:
{2}->3, {1}->2, {}->0, {3,4}->3, {1,3}->3, {0}->3, 
Qualitative normalization functions:
________0_______________1_______________2_______________3_______________
phi0:	0-58		X		X		59
phi1:	0-13		14-18		X		19-24
phi2:	0-11		12-16		17-18		19-22
phi3:	0-12		X		X		13-21
phi4:	0-7		X		X		8-14
phi5:	0-29		X		X		30
>><<
Focal sets:
{3}->3, {1,4}->2, {}->0, {2,4}->1, {0,5}->2, 
Qualitative normalization functions:
________0_______________1_______________2_______________3_______________
phi0:	0-39		X		40-58		59
phi1:	0-17		X		18-23		24
phi2:	0-8		9-21		X		22
phi3:	0-8		X		9-17		18-21
phi4:	0-5		X		6-13		14
phi5:	0-16		X		17-29		30
>>
}
```

which represents a set of SUFs. For evaluating SRL on ``dataset1.txt``, launch

```
java -jar RLSUF.jar -validation -data dataset1.txt -nbruns 10 -rhoStart 0.95 -rhoEnd 1.01
```

This gives a result of the form

```
             MER: 	0.081	
(over folds) std: 	0.018	
 (over runs) std:	0.0034	
             MAE: 	0.081	
(over folds) std: 	0.018	
 (over runs) std:	0.0034	
        nb rules: 	118.8	
    rule lengths: 	2.3	
Rule length distribution (max-length:ratio):
0:0.0 1:0.058 2:0.62 3:0.272 4:0.045 5:0.005 6:0.0
```

MER and MAE respectively stand for Missclassification Error Rate (the ratio of missclassified rows) and for Mean Absolute Error. Both error are measured on test data for each fold and averaged. Std over folds correspond to the standard deviation of error scores over the folds during one run. Std over runs is the standard deviation of the average error scores of each run (this line is displayed only if nbruns > 1). Nb rules and rules lengths are respectively the overall average number of rules in the model and the overall average size of rules.
Finally, rule length distribution give an average distribution of rules by size.


## The data file
Example of the content of a datafile :
```
ATTRIBUTES:
numerical
numerical
numerical_reversed
[bad,average,good]
DATA:
0,16,8,good
0,4,7,bad
1,5,10,average
1,14,7,good
...
```
The keyword ``ATTRIBUTES:`` should be followed by the type of the scale of each attribute. The last row before ``DATA:`` should describe the scale of classes.
Three types of scales are possible
* ``numerical``: when the scale is numerical,
* ``numerical_reversed``: when the scale is numerical, but the algorithm should use the inverse order (i.e., the attribute is negatively correlated to the class),
* ``[a,b,...,z]``: a list of elements (between square brackets, separated by commas), is interpreted as the definition of a scale. Elements should appear in increasing order.

Note that if the number of attributes is n, then there should be n+1 lines between ``ATTRIBUTES:`` and ``DATA:``. The last of this line should correspond to the classes.
The rows after ``DATA:`` contain the examples from which the classifier is learned.

**Note** The ``datasets`` folder contains the datasets used in _J. Błaszczyński, R. Słowiński, M. Szeląg, Sequential covering rule induction algorithm for variable consistency rough set approaches, Information Sciences 181(5), 2011_.
