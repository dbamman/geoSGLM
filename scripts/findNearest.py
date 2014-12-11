"""
Load up the learned word embeddings in sys.argv[1] in memory; for a given search term q, find the 10 closest terms to q in each of the 51 states.

"""

import sys,math,operator

import numpy as np
from numpy import linalg as LA

def find(word, data, name):
	print "Finding: %s in %s" % (word, name)
	scores={}
	if word not in data:
		print "%s not in vocab" % word
		return

	a=data[word]
	for word2 in data:
		try:
			score=np.inner(a, data[word2])
			scores[word2]=score
		except:
			pass

	sorted_x = sorted(scores.iteritems(), key=operator.itemgetter(1), reverse=True)
	for i in range(10):
		(k,v) = sorted_x[i]
		print "%s\t%.3f" % (k,v)
	print ""

# find closest terms for all states
def bigfind(word, embeddings):
	for n in sorted(embeddings):
		find(word, embeddings[n], n)

# normalize vectors for faster cosine similarity calculation
def normalize(embeddings):
	for name in embeddings:
		for word in embeddings[name]:
			a=embeddings[name][word]
			norm=LA.norm(a, 2)

			a /= norm	
			embeddings[name][word]=a

# get all active facets in embeddings
def getFacets(filename):
	file=open(filename)

	facets={}
	for line in file:
		cols=line.rstrip().split(" ")
		facets[cols[0]]=1
	file.close()

	# don't count the base facet
	del facets["MAIN"]

	return facets.keys()

def process(filename):
	file=open(filename)

	embeddings={}

	facets=getFacets(filename)

	# if you want to only consider a few metadata facets and not all 51 states, do that here.  e.g.:
	# facets=["MA", "PA"]

	for facet in facets:
		embeddings[facet]={}

	for line in file:
		cols=line.rstrip().split(" ")
		if len(cols) < 10:
			continue

		facet=cols[0]

		if facet != "MAIN" and facet not in embeddings:
			continue

		word=cols[1]
		vals=cols[2:]
		a=np.array(vals, dtype=float);
		size=len(vals)

		## 
		# State embeddings for a word = the MAIN embedding for that word *plus* the state-specific deviation
		# e.g.
		# "wicked" in MA = wicked/MAIN + wicked/MA
		##

		if facet == "MAIN":
			for n in embeddings:
				if word not in embeddings[n]:
					embeddings[n][word]=np.zeros(size)
				
				embeddings[n][word]+=a

		else:
			if word not in embeddings[facet]:
				embeddings[facet][word]=np.zeros(size)
				
			embeddings[facet][word]+=a
		
	file.close()

	normalize(embeddings)

	print "query (ctrl-c to quit): ",
	line = sys.stdin.readline()
	while line:
		word=line.rstrip()
		print word
		bigfind(word, embeddings)
		print "query (ctrl-c to quit): ",
		line = sys.stdin.readline()

if __name__ == "__main__":
	process(sys.argv[1])