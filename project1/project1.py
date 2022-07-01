import re
import sys

from mrjob.job import MRJob
from mrjob.step import MRStep
# from mrjob.compat import jobconf_from_env

# Regex to actually match on words
WORD_RE = re.compile(r"[\w']+")

class count(MRJob):
    def mapper(self, _, line):
        year = line[:4]
        line = line[9:]
        for word in WORD_RE.findall(line):
            yield (word, year), 1
            # yield word, (year, 1)

    def reducer(self, values, count):
        # sys.stdout.write(values[0].encode())
        # for word, year, count in sorted(values, key=lambda x: (x[0],x[1], x[2] )):
            yield  values, (sum(count))

    # def reducer(self, _, values):
    #     for word, year, count in values:
    #         yield  (sum(count), word), year
    # def reducer(self, _, values):
    #     for word, year, count in sorted(values, key=lambda x: (x[0],x[1], x[2] )):
    #         yield (word, year), sum(count)

if __name__ == '__main__':
    count.run()