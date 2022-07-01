*Bismillah Hir Rahman Nir Rahim*
## Run without hadoop:
`python3 project1.p < mydata.txt `
- Can use additional flag ` --mapper --step-num=N` to see output of mapper function
  e.g. ` --mapper --step-num=0` -> This checks step number 0

## Understanding the commands:
`python3 wordcount.py -r hadoop hdfs://localhost:9000/user/comp9313/input/mydata.txt`
- The '-r' flag just references the hadoop conf 
- We can add additional conf properties with flag `--jobconf`
    - Example: `--jobconf myjob.settings.years=20 --jobconf mapreduce.job.reduces=2`
    - The example above is setting 2 reduces, the same can be done for maps
    - NOTE: These can be accessed in the py file using jobconf_from_env()
- Focusing on `--jobconf mapreduce.job.reduces=2`:
    - Defines parallelism, if this value --jobconf mapreduce.job.reduces=1, then no parallelism
      if 2, then you'd ideally want to reduce workload/execution time to half

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
## Understanding TF-IDF w/ Examples:
Consider an example:
```
Consider a year containing 100 words wherein the word cat appears 3 times. The term frequency (i.e., tf) for cat is then (3 / 100) = 0.03. Now, assume we have 10 million years and the word cat appears in one thousand of these. Then, the inverse document frequency (i.e., idf) is calculated as log(10,000,000 / 1,000) = 4. Thus, the Tf-idf weight is the product of these quantities: 0.03 * 4 = 0.12.
```

Now lets consider a more relevant example: 
```
Given the data source:
20191124,woman stabbed adelaide shopping centre
20191204,economy continue teetering edge recession
20200401,coronanomics learnt coronavirus economy
20200401,coronavirus home test kits selling chinese community
20201015,coronavirus pacific economy foriegn aid china
20201016,china builds pig apartment blocks guard swine flu
20211216,economy starts bounce unemployment
20211224,online shopping rise due coronavirus
20211229,china close encounters elon musks

1. Find the TF-IDF for woman given the data source: 
2. Find the TF-IDF for shopping given the data source: 

```
1. 
Here we have:
- TF = 1/1 
- IDF = log (3/1)
- TF * IDF = 1 * 0.4771212547196623 = *0.4771212547196623*

2. Here we have: 
2019: 
- TF = 1/1
- IDF = log(3/2)
- TF * IDF = 1 * 0.17609125905568124 = *0.17609125905568124*

## Implementation: 

### TF:
This value only cares about the line and year i.e. the frequency of t in y
- You won't be mapping by line, but instead year. In your first mapper you will need to:
  - Get substr of year for first line