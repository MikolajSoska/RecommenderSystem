import nltk
import re

stemmer = nltk.SnowballStemmer("english")


# Function for word stemming.
def stem_words(description: str):
    words = list()

    for word in nltk.word_tokenize(description):
        if re.search('[a-zA-Z]', word):
            words.append(stemmer.stem(word))

    return words
