GeoSGLM
=======

Code for learning geographically-informed word embeddings, as used in Bamman et al. 2014, "Distributed Representations of Geographically Situated Language" (ACL).  This draws on code from Mikolov et al. 2013, "Efficient estimation of word representations in vector space" (ICLR), https://code.google.com/p/word2vec/ (Apache 2.0).

To run, adjust the input/output parameters in `run.sh` and execute it.  The required arguments are as follows:

* DATA=data/data.test.txt

The data file contains the text (and associated metadata) to learn word representations from.  The main columns should be tab-separated, and the text should be space-separated (and tokenized).  Sample records include:

|id|location|message|
|---|---|---|
|480326347508969000 |     PA   |   There is a great research question in how long a sequence of blog comments can go before it descends into madness http://t.co/NFqKgaZRuO|
|472023364908118000	|	PA	|	So much easier than hunting through individual websites : using Google Scholar to get BibTeX citations http://t.co/H2inkMGMom|
|105039889808109000     | PA |     Just discovered Conflict Kitchen in Pittsburgh - brilliant idea that needs to catch on in other cities . http://t.co/FkSLGD9|

In the work described in Bamman et al. (2014), the metadata values = the 51 US States (including DC), but can be any categorical feature.



* VOCABFILE=data/vocab.txt

The vocab file contains the maximal set of words to learn representations for; if a word is not in this list, then don't learn a representation for it.  This list is further filtered in the code to only include words that are seen at least 5 times in the data, and a maximum of the $MAXVOCAB most frequent terms (specified below).

* FEATUREFILE=data/states.txt

The feature file lists the valid metadata values to learn embeddings for (e.g., a list of all US states).


* OUTFILE=data/out.embeddings

The outfile contains the learned word embeddings.  The output format is space-separated (facet, term, K-dimensional word representation). "Facet" denotes either the base representation (MAIN) or the state-specific deviation from that base representation (e.g., "CA" for california).  ***To get the word representation for the word "city" in California, add together the vectors for city/MAIN and city/CA.***


* MAXVOCAB=100000

Maxvocab specifies the largest size the vocabulary can be.

* DIMENSIONALITY=100

Dimensionality specifies the size of the learned word representations.

* L2=0.0001

L2 regularization parameter.

## Viewing embeddings

For a given query q, you can view the terms most similar to q in all 51 states using scripts/findNearest.py

`python scripts/findNearest.py $OUTFILE`

