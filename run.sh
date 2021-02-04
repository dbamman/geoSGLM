# input file
DATA=data/data.test.txt

# vocab file
VOCABFILE=data/vocab.txt

# set of metadata facets
FEATUREFILE=data/states.txt

# output file to write embeddings to
OUTFILE=data/out.embeddings

# max vocab size
MAXVOCAB=100000

# dimensionality of embeddings
DIMENSIONALITY=100

# L2 regularization parameter
L2=0.0001

CONTEXTS_FILE=data/test/out.contexts

./runjava geosglm.ark.cs.cmu.edu/GeoSGLM $DATA $VOCABFILE $FEATUREFILE $OUTFILE $MAXVOCAB $DIMENSIONALITY $L2 $CONTEXTS_FILE
