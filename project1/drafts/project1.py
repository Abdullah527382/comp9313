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
        line = WORD_RE.findall(line)
        yield(year, line)


    # def mapper(self, _, line):
    #     year = line[:4]
    #     line = line[9:]
    #     for word in WORD_RE.findall(line):
    #         yield (word, year), 1

    def reducer(self, year, words):
        for word in words: 
            yield year, word

    

    
if __name__ == '__main__':
    MRCount.run()