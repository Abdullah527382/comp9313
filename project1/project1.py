from mrjob.job import MRJob

class count(MRJob):
    """
        A mapper() function. Takes in key, value argument and returns output in tuple format. 
        Splits the line and generates a word's count, e.g. <count> '<word>'
    """
    def mapper(self, _, line):
        # 
        for word in line.split():
            yield(word, 1)
    
    """
        A reducer() function which aggregates the result according to the key and produces output
        in a key value format with total count.
    """
    def reducer(self, word, counts):
        yield(word, sum(counts))

"""
    Execute the code.
"""
if __name__ == '__main__':
    count.run()