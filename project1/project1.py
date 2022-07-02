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
        line = line[9:]
        line = WORD_RE.findall(line)
        for word in line:
            word = word.lower()
            # self.finalResult.setdefault(word, "")
            # self.finalResult[word] = ""
            yield (word, year), 1
    
    # ((word, year), N)
    def TFByYear(self, value, count):
        word = value[0]
        year = value[1]
        freq = sum(count)
        yield(word, year), freq

    # (year, (word, N))
    def getYearsAndTF(self, value, freq):
        word = value[0]
        year = value[1]
        freq = freq
       # yield (word, year), freq
        yield word, (year, freq, 1, word)

    # (word, year), (tf, yearNum)
    def yearOccurence(self, word, year_freqs):
        yearList = []
        freqList = []
        wordList = []
        numOfYears = 0
        for year_freq in year_freqs:
            numOfYears += 1
            year = year_freq[0]
            freq = year_freq[1]
            currWord = year_freq[3]
            yearList.append(year)
            freqList.append(freq)
            wordList.append(currWord)
        
        listLength = len(yearList)
        # combinedList = []
        for counter in range(listLength):
            # i = i + 1
            # if counter >= 0 and counter in range(listLength + 1):
            #     currWord = wordList[counter]
            #     if currWord == wordList[counter - 1] and counter != 0:
            #         # Check if list contains dict
            #         for item in combinedList:
            #             if currWord in item:
            #                 item[currWord].append
            #         formattedDict = {}
            #         formattedDict['word'] = currWord
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
        yield (word), (year, tfIdf)

    def formatOutput(self, word, value):
        yield (word, value)

    def steps(self):
        return [
            MRStep(
                mapper=self.wordsFromLines,
                combiner=self.TFByYear,
                reducer=self.TFByYear
            ), 
            MRStep(
                mapper=self.getYearsAndTF, 
                reducer=self.yearOccurence
            ), 
            MRStep (
                mapper=self.calcTfIdf,

            ), 
            MRStep (
                mapper=self.formatOutput,
            )
        ]
if __name__ == '__main__':
    MRCalculateTFIDF.run()