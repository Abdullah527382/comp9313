import re

from mrjob.job import MRJob
from mrjob.step import MRStep
# from mrjob.compat import jobconf_from_env

# Regex to actually match on words
WORD_RE = re.compile(r"[\w']+")

class count(MRJob):
    """
        A mapper() function. Takes in key, value argument and returns output in tuple format. 
        Splits the line and generates a word's count, e.g. <count> '<word>'
    """
    def mapper(self, _, line):
        # 
        for word in WORD_RE.findall(line):
            yield(word, 1)
    
    """
        A reducer() function which aggregates the result according to the key and produces output
        in a key value format with total count.
    """
    def reducer(self, word, counts):
        yield None, (sum(counts), word)

    """
        A second reducer which finds the max word
    """
    def reducer_maxword(self, _, word_counts):
        yield max(word_counts)

    def steps(self):
        return [
            MRStep(mapper=self.mapper, 
                   reducer=self.reducer), 
            MRStep(reducer=self.reducer_maxword)
        ]
"""
    Execute the code.
"""
if __name__ == '__main__':
    count.run()