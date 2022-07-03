### Note that if you use a single reducer, it is not necessary to configure JOBCONF.
### Since Hadoop automatically sorts the keys (a pair of terms) in alphabetical order.

import re
from mrjob.job import MRJob
from mrjob.step import MRStep

class NSRelativeFreq(MRJob):                
   
    

    def mapper(self, _, line):
        year = line[:4]
        line = line[9:]

        line = re.split("[ *$&#/\t\n\f\"\'\\,.:;?!\[\](){}<>~\-_]", line.lower())


        for i in range(0, len(line)):
            if len(line[i]):           
                for j in range(i+1, len(line)):
                    if len(line[j]):
                        yield line[i]+","+str(year), 1
                        yield line[i]+",*", 1

    def combiner(self, key, values):
        yield key, sum(values)
            
    def reducer_init(self):
        self.marginal = 1
           
    def reducer(self, key, values):        
        wi, wj = key.split(",", 1)
        if wj=="*":
            self.marginal = sum(values)
        else:
            count = sum(values)
            yield wi, wj + ","  + str(float(count/self.marginal)) + ";" + str(count)  + ";" + str(self.marginal) 

    def finalise(self, key, values):
        combinedValue = ""
        for value in values:
            combinedValue +=  value
        yield (key, combinedValue)
        
    def steps(self):
        return [
            MRStep(
                mapper=self.mapper,
                combiner=self.combiner,
                reducer_init=self.reducer_init,
                reducer=self.reducer
            ), 
            MRStep(
                reducer=self.finalise
            )
        ]
if __name__ == '__main__':
    NSRelativeFreq.run()
