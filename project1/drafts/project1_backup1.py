import re
import sys
import json

from mrjob.job import MRJob
from mrjob.step import MRStep
# from mrjob.compat import jobconf_from_env

# Regex to actually match on words
WORD_RE = re.compile(r"[\w']+")

class MRCount(MRJob):
    def init_get_freqs(self):
        self.wordFreqs = {}

    def mapper(self, _, line):
        yearOfWord = line[:4]
        line = line[9:]

        for word in WORD_RE.findall(line):
            self.wordFreqs.setdefault(word, 0)
            self.wordFreqs[word] = self.wordFreqs[word] + 1
            self.wordFreqs["year"] = yearOfWord

    def total_words(self, word, count):
        yield (word, sum(count))
    
    # def mapper2(self, value, line):
    #     # line = line[9:]
    #     # for word in WORD_RE.findall(line):
    #         yield value[0], line

    def reducer2(self):
        for word, val in self.wordFreqs.items():
            yield (word, val)


    def steps(self):
        return [
            MRStep(mapper_init=self.init_get_freqs,
                    mapper=self.mapper, 
                    mapper_final=self.reducer2)
                    ]
    # Time for the normal wordcount:
    
if __name__ == '__main__':
    MRCount.run()