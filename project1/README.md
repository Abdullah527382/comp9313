*Bismillah Hir Rahman Nir Rahim*

## Our plan

We want our output in the following format: 

```
"adelaide"	"2019,0.47712125471966244"
"aid"	"2020,0.47712125471966244"
"apartment"	"2020,0.47712125471966244"
"blocks"	"2020,0.47712125471966244"
"bounce"	"2021,0.47712125471966244"
"builds"	"2020,0.47712125471966244;2021,47712125471966244"
...
```

Which is in the format: 
(word, (year,weight; year,weight; ...))

Where weight is calculated as:

```
TF(term t, year y) = the frequency of t in y 
IDF(term t, dataset D)= log10(the number of years in D/the number of years having t)
Weight(term t, year y, dataset D) = TF(term t, year y)*IDF(term t, dataset D)
```