import re
import sys
import json

from mrjob.job import MRJob
from mrjob.step import MRStep
# from mrjob.compat import jobconf_from_env

# Regex to actually match on words
WORD_RE = re.compile(r"[\w']+")

class MRCount(MRJob):

    def mapper(self, _, line):
        year = line[:4]
        line = line[9:]
        for word in WORD_RE.findall(line):
            yield (word, year), 1

    def reducer(self, values, count):

        yield (values, sum(count))

    def reducer_all(self, values, counts):
        yield (sum(counts), values)
    
    def steps(self):
        return [
            MRStep(mapper=self.mapper, 
                   reducer=self.reducer), 
            MRStep(reducer=self.reducer_all)
        ]
    # Time for the normal wordcount:
    
if __name__ == '__main__':
    MRCount.run()