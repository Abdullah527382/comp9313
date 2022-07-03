import json
import re
import sys 

from mrjob.job import MRJob
from mrjob.step import MRStep
from mrjob.compat import jobconf_from_env

from math import log10
# Regex to actually match on words
WORD_RE = re.compile(r"[\w']+")
NUM_YEARS = 3 #jobconf_from_env('myjob.settings.years')

class MRCalculateTFIDF(MRJob):

    # def init_final_result(self):
    #     self.finalResult = {}

    def wordsFromLines(self, _, line):
        year = line[:4]
        lines = line[9:]

        lines = re.split("[ *$&#/\t\n\f\"\'\\,.:;?!\[\](){}<>~\-_]", lines.lower())

        for word in lines:
            yield word+","+str(year), 1
            yield word+",*",1
        # for i in range(0, len(line)):
        #     if len(line[i]):           
        #         for j in range(i+1, len(line)):
        #             if len(line[j]):
        #                 yield line[i]+","+str(year), 1
        #                 yield line[i]+",*", 1


    # ((word, year), N)
    def TFByYear(self, key, count):
        freq = sum(count)
        yield(key), freq

    def reducer_init(self):
        self.marginal = 0

    def reducer(self, key, values):        
        wi, wj = key.split(",", 1)
        if wj=="*":
            self.marginal = sum(values)
        else:
            count = sum(values)
            yield wi,(wj, count, self.marginal)

    # (year, (word, N))
    # def getYearsAndTF(self, key, freq):
    #     word, year = key.split(",", 1)
    #         # yield (word, year), freq
    #     yield word, (year, freq)

    # (word, year), (tf, yearNum)
    def yearOccurence(self, word, year_freqs):
        yearList = []
        freqList = []
        numOfYears = 0
        for year_freq in year_freqs:
            numOfYears += 1
            year = year_freq[0]
            freq = year_freq[1]
            yearList.append(year)
            freqList.append(freq)
        
        listLength = len(yearList)
        for counter in range(listLength):
            yield(word, yearList[counter]),( freqList[counter], numOfYears, int(NUM_YEARS))        

    # Calculate TFIDF given the prev values
    def calcTfIdf (self, word_year, freq_nums):
        # word_year object 
        word = word_year[0]
        year = word_year[1]
        # freq_nums object
        freq = freq_nums[0]
        wordYearCount = freq_nums[1]
        numOfYears = freq_nums[2]
        # Calculate the TF-IDF 
        tfIdf = freq * log10(numOfYears/wordYearCount)
        # yield(word, year),(freq, numOfYears, numOfYears)
        # self.finalResult[f"{word}"] += f"{year},{tfIdf}"
        concatenated = year + "," + str(tfIdf) + ";"
        yield (word), (concatenated)

    def formatOutput(self, word, values):
        combinedValue = ""
        for value in values:
            combinedValue +=  value
        combinedValue = combinedValue[:-1]
        yield (word, combinedValue)

    def steps(self):
        return [
            MRStep(
                mapper=self.wordsFromLines,
                combiner=self.TFByYear,
                reducer_init=self.reducer_init,
                reducer=self.reducer
            ), 
            MRStep(
                reducer=self.yearOccurence
            ), 
            MRStep (
                mapper=self.calcTfIdf,
                reducer=self.formatOutput
            )
        ]
if __name__ == '__main__':
    MRCalculateTFIDF.run()