import re
import sys
import json

from mrjob.job import MRJob
from mrjob.step import MRStep
# from mrjob.compat import jobconf_from_env

# Regex to actually match on words
WORD_RE = re.compile(r"[\w']+")
TFValues = []
class MRCount(MRJob):

    def mapper(self, _, line):
        year = line[:4]
        line = line[9:]
        for word in WORD_RE.findall(line):
            yield (word, year), 1
            # yield word, (year, 1)

    def reducer(self, values, count):
        # if (self.currentWord == values[0]):
        #     self.totalCount += sum(count)
        # else:
        #     self.totalCount = 0
        #     self.currentWord = values[0]
        yield (values[0], sum(count))

    def reducer_all(self, values, counts):
        TFValues.append({'word': values[0], 'count': counts})

        total = 0
        for value in TFValues:
            if value['word'] == values:
                sys.stdout.write((value['word'].encode()))
                total = int(value['count'])
        yield (sum(counts), values), total
    # def reducer(self, _, values):
    #     for word, year, count in sorted(values, key=lambda x: (x[0],x[1], x[2] )):
    #         yield (word, year), sum(count)
    
    def steps(self):
        return [
            MRStep(mapper=self.mapper, 
                   reducer=self.reducer), 
            MRStep(reducer=self.reducer_all)
        ]
    # Time for the normal wordcount:
    
if __name__ == '__main__':
    MRCount.run()