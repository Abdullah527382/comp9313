import json
import re
import sys 

from mrjob.job import MRJob
from mrjob.step import MRStep
from mrjob.compat import jobconf_from_env

# Regex to actually match on words
WORD_RE = re.compile(r"[\w']+")
NUM_YEARS = jobconf_from_env('myjob.settings.years')

class MRCalculateTFIDF(MRJob):

    wordFreqList = {}

    def wordsFromLines(self, _, line):
        year = line[:4]
        line = line[9:]
        line = WORD_RE.findall(line)
        for word in line:
            word = word.lower()
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
        yield word, (year, freq, 1)

    # Compute the num count of words overall
    # def overallNumWords(self, year, words_freq):
    #     wordList = []
    #     freqList = []
    #     # For each of the word_freq objects in words_freq (i.e. (word, freq))
    #     for word_freq in words_freq:
    #         # Get the word and freq and append to local state
    #         word = word_freq[0]
    #         freq = word_freq[1]
    #         wordList.append(word)
    #         freqList.append(freq)

    #     # # Loop through the lists and create a new list with totalCounts
    #     # wordFreqList = dict(zip(wordList,freqList))
    #     # sys.stdout.write(json.dumps(wordFreqList).encode())
    #     # Go through our lists and grab the relevant information
    #     listLength = len(wordList)
    #     for counter in range(listLength):
    #         yield(wordList[counter], year),(freqList[counter]) 

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
            yield(word, yearList[counter]),(freqList[counter], numOfYears, NUM_YEARS)        


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
            )
        ]
if __name__ == '__main__':
    MRCalculateTFIDF.run()