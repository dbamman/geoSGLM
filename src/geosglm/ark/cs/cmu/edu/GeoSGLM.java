package geosglm.ark.cs.cmu.edu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.io.input.BoundedInputStream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Code for learning geographically-informed word embeddings, as used in Bamman
 * et al. 2014,
 * "Distributed Representations of Geographically Situated Language" (ACL). This
 * draws on code from Mikolov et al. 2013,
 * "Efficient estimation of word representations in vector space" (ICLR).
 * https://code.google.com/p/word2vec/ (Apache 2.0)
 * 
 */
public class GeoSGLM {

	public class DataSubsetThread extends Thread {

		long end;
		int id;
		String infile;
		long start;

		/**
		 * Initialize thread with input file, thread id, and start byte offset
		 * 
		 * @param infile
		 * @param id
		 * @param start
		 * @param end
		 */
		public DataSubsetThread(String infile, int id, long start, long end) {
			this.infile = infile;
			this.id = id;
			this.start = start;
			this.end = end;
			System.out.println(String.format(
					"Starting thread %d, byte offset: %s-%s", id, start, end));

		}

		/**
		 * Run backprop on this thread's subset of the data. To process large
		 * files, don't store the data in memory but rather process it
		 * sequentially during reading. Core skip-gram language model drawn from
		 * https://code.google.com/p/word2vec/.
		 * 
		 */
		public void run() {

			try {

				FileInputStream file = new FileInputStream(infile);
				file.skip(start);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new BoundedInputStream(file, end - start), "UTF-8"));

				double g = 0;
				String str1;
				double[] neu1e = new double[hiddenLayerSize];

				long n = 0;
				long last = 0;
				while ((str1 = br.readLine()) != null) {

					if (n % regularizeEveryNLines == 0 && n > 0) {
						last = n - last;
						regularize(last);
					}
					n++;
					try {

						String[] cols = str1.trim().split("\t");
						String facet = cols[1];
						String intersection = cols[2];
						String message = cols[3];
						String[] words = message.toLowerCase().split(" ");

						HashSet<Integer> activeFeatures = getFeatures(facet, intersection);
						ArrayList<Word> newparts = Lists.newArrayList();

						normalizeWords(words);

						for (int i = 0; i < words.length; i++) {

							Integer id = vocabIds.get(words[i]);
							if (id == null) {
								continue;
							}

							Word word = vocab[id];
							newparts.add(word);

						}

						totalWordsSeen += newparts.size();

						Word[] vparts = new Word[newparts.size()];
						for (int i = 0; i < newparts.size(); i++) {
							vparts[i] = newparts.get(i);
						}

						for (int i = 0; i < vparts.length; i++) {

							if (totalWordsSeen - lastUpdateWordCount >= 10000) {
								update();
							}

							Word word = vparts[i];

							int b = 0;

							for (int a = b; a < window * 2 + 1 - b; a++) {

								int c = i - window + a;

								if (c == i) {
									continue;
								}

								if (c < 0)
									continue;
								if (c >= vparts.length)
									continue;

								Word lastWord = vparts[c];
								Integer lastWordId = lastWord.id;

								Arrays.fill(neu1e, 0.0);

								double[] sum = new double[hiddenLayerSize];
								for (int k = 0; k < hiddenLayerSize; k++) {

									for (int z : activeFeatures) {
										sum[k] += embeddings[z][lastWordId][k];
									}
								}

								for (int j = 0; j < word.codeLength; j++) {

									double f = 0;
									for (int k = 0; k < hiddenLayerSize; k++) {

										f += sum[k]
												* outputWeights[k][word.point[j]];
									}
									if (f <= -MAX_EXP) {
										continue;
									} else if (f >= MAX_EXP) {
										continue;
									}

									else {
										f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE
												/ (double) MAX_EXP / 2.))];
									}

									g = (word.code[j] - f) * alpha;

									for (int k = 0; k < hiddenLayerSize; k++) {
										neu1e[k] += g
												* outputWeights[k][word.point[j]];
									}

									for (int k = 0; k < hiddenLayerSize; k++) {

										outputWeights[k][word.point[j]] += g
												* sum[k];
									}

								}

								for (int z : activeFeatures) {
									nonZeroEmbeddings[z][lastWordId] = true;
									for (int k = 0; k < hiddenLayerSize; k++) {
										embeddings[z][lastWordId][k] += neu1e[k];
									}
								}

							}
						}
					} catch (Exception e) {
						if (detailedOutput) {
							e.printStackTrace();
						}
					}

				}

				br.close();

			} catch (Exception e) {
				if (detailedOutput) {
					e.printStackTrace();
				}
			}
		}
	}

	public class Word {
		int[] code;
		int codeLength;
		long count;
		int id;
		int[] point;
		String word;

		public Word(String word, int id, long count) {
			this.word = word;
			this.id = id;
			this.count = count;
		}
	}

	static int epochs = 10;

	public static void main(String[] args) {

		// input data file, in TSV format
		String inputData = args[0];

		// list of terms to generate word representations for
		String vocabFile = args[1];

		// list of valid metadata facets, one per line
		String featureFile = args[2];

		// path to write the word embeddings to
		String outputFile = args[3];

		// max size of vocabulary
		int maxVocab = Integer.valueOf(args[4]);

		// word representation dimensionality
		int hiddenLayerSize = Integer.valueOf(args[5]);

		// L2 regularization term
		double L2 = Double.valueOf(args[6]);

		// path to write the context embeddings to
		String contextsFile = args[7];

		GeoSGLM model = new GeoSGLM();

		model.hiddenLayerSize = hiddenLayerSize;
		model.maxVocab = maxVocab;
		model.L2 = L2;

		model.setFeatures(featureFile);
		model.setVocab(vocabFile, inputData);

		model.init();
		for (int i = 0; i < epochs; i++) {
			model.learn(inputData);
			model.totalWordsSeen=i+1*model.totalWordCount;
			model.update();
			//if (((i+1) % 5) == 0){
			//	model.write (outputFile + "." + Integer.toString(i));
			//	model.contexts_write (contextsFile);
			//}
			System.out.println(String.format("Iterations %d completed", i+1));
		}
		System.out.println("All iterations completed");
		model.write(outputFile);
		model.contexts_write (contextsFile);
		System.out.println(String.format("Target embeddings written in  %s", outputFile));
		System.out.println(String.format("Context embeddings written in  %s", contextsFile));

	}

	double alpha = 0.025;

	boolean detailedOutput = false;

	// V x H input->hidden layer weights (embeddings)
	float[][][] embeddings;

	int EXP_TABLE_SIZE = 1000;

	// fast exp calculations, from Mikolov
	double[] expTable;

	HashMap<String, Integer> featureMap;

	// dimensionality of the embeddings
	int hiddenLayerSize = 100;

	boolean isCurrentlyRegularizing = false;

	// L2 regularization strength
	double L2 = 1e-4;

	// number of words seen at last update
	long lastUpdateWordCount = 0L;

	// exp truncation bounds
	double MAX_EXP = 6;
	// maximum number of words in vocab
	int maxVocab = 10000;

	// minimum count for word to enter vocab
	int minimumVocabCount = 5;

	boolean[][] nonZeroEmbeddings;

	int numFeatures;

	// number of threads
	int numThreads = 32;
	// H x V hidden->output layer weights
	float[][] outputWeights;

	int regularizeEveryNLines = 1000;

	HashMap<Integer, String> reverseFeatureMap;

	// learning rate
	double starting_alpha = 0.025;
	long startTime;

	// total number of bytes (to partition the file for the threads)
	long totalBytes = 0;

	long totalTweets;

	// total number of words
	long totalWordCount = 0L;
	// count of current words seen among all threads
	long totalWordsSeen = 0;

	Word[] vocab;

	HashMap<String, Long> vocabCounts;
	HashMap<String, Integer> vocabIds;

	int vocabSize;

	// window of words around target word to learn from. range is
	// [i-window,i+window]
	int window = 5;

	boolean writeFlag = false;

	/**
	 * From Mikolov (https://code.google.com/p/word2vec/)
	 * 
	 * Create a Huffman code representation if the counts are meaningful; to
	 * randomize order (and create a simple binary tree), set counts to some
	 * constant.
	 */
	public void createBinaryTree() {
		int MAX_CODE_LENGTH = 40;
		int a, b, i, min1i, min2i, pos1, pos2;
		int[] point = new int[MAX_CODE_LENGTH];
		int[] code = new int[MAX_CODE_LENGTH];
		long[] count = new long[vocabSize * 2];
		int[] binary = new int[vocabSize * 2];
		int[] parent_node = new int[vocabSize * 2];

		for (a = 0; a < vocabSize; a++)
			count[a] = vocab[a].count;
		for (a = vocabSize; a < vocabSize * 2; a++)
			count[a] = 10000000000L;
		pos1 = vocabSize - 1;
		pos2 = vocabSize;

		for (a = 0; a < vocabSize - 1; a++) {
			if (pos1 >= 0) {
				if (count[pos1] < count[pos2]) {
					min1i = pos1;
					pos1--;
				} else {
					min1i = pos2;
					pos2++;
				}
			} else {
				min1i = pos2;
				pos2++;
			}
			if (pos1 >= 0) {
				if (count[pos1] < count[pos2]) {
					min2i = pos1;
					pos1--;
				} else {
					min2i = pos2;
					pos2++;
				}
			} else {
				min2i = pos2;
				pos2++;
			}
			count[vocabSize + a] = count[min1i] + count[min2i];
			parent_node[min1i] = vocabSize + a;
			parent_node[min2i] = vocabSize + a;
			binary[min2i] = 1;
		}
		for (a = 0; a < vocabSize; a++) {
			b = a;
			i = 0;
			while (true) {
				code[i] = binary[b];
				point[i] = b;
				i++;
				b = parent_node[b];
				if (b == vocabSize * 2 - 2)
					break;
			}
			vocab[a].codeLength = i;
			vocab[a].code = new int[i];
			vocab[a].point = new int[i + 1];

			vocab[a].point[0] = vocabSize - 2;
			for (b = 0; b < i; b++) {
				int z = code[b];
				vocab[a].code[i - b - 1] = z;
				vocab[a].point[i - b] = point[b] - vocabSize;
			}
		}
	}

	public void filterVocab() {

		vocabIds = Maps.newHashMap();
		int numAboveMinCount = 0;
		ArrayList<Object> sorted = Util.sortHashMapByValue(vocabCounts);
		for (int i = 0; i < sorted.size(); i++) {
			String w = (String) sorted.get(i);
			if (vocabCounts.get(w) >= minimumVocabCount) {
				numAboveMinCount++;
			}
			if (numAboveMinCount >= maxVocab) {
				break;
			}
		}

		vocabSize = numAboveMinCount;

		ArrayList<String> shuffled = Lists.newArrayList();

		for (int i = 0; i < vocabSize; i++) {
			String w = (String) sorted.get(i);
			totalWordCount += vocabCounts.get(w);
			shuffled.add(w);

		}
		Collections.shuffle(shuffled);

		vocab = new Word[vocabSize];

		for (int i = 0; i < vocabSize; i++) {
			String w = (String) shuffled.get(i);
			Word word = new Word(w, i, 1);
			vocab[i] = word;
			vocabIds.put(w, i);

		}

	}

	/**
	 * Get the embedding indices for a particular metadata value. All facets
	 * touch the base representation (0); also include the index of valid
	 * states.
	 * 
	 * @param facet
	 * @return
	 */
	public HashSet<Integer> getFeatures(String facet, String intersection) {

		HashSet<Integer> activeFeatures = Sets.newHashSet();
		activeFeatures.add(0);

		if (featureMap.containsKey(facet)) {
			activeFeatures.add(featureMap.get(facet));
		}

		/* If facet and intersection are the same, 
		 * then this block does not have any effect.
		 */
		if (featureMap.containsKey(facet)) {
			activeFeatures.add (featureMap.get(intersection));
		}

		return activeFeatures;

	}

	public void init() {

		Random random = new Random();
		startTime = System.currentTimeMillis();

		embeddings = new float[numFeatures][vocabSize][hiddenLayerSize];
		outputWeights = new float[hiddenLayerSize][vocabSize];

		nonZeroEmbeddings = new boolean[numFeatures][vocabSize];

		int f = 0;
		for (int i = 0; i < vocabSize; i++) {
			for (int j = 0; j < hiddenLayerSize; j++) {
				embeddings[f][i][j] = (float) ((random.nextDouble() - 0.5) / hiddenLayerSize);
			}
		}

		// Mikolov trick
		expTable = new double[EXP_TABLE_SIZE];
		for (int i = 0; i < EXP_TABLE_SIZE; i++) {
			expTable[i] = Math.exp((i / (double) EXP_TABLE_SIZE * 2 - 1)
					* MAX_EXP);
			expTable[i] = expTable[i] / (expTable[i] + 1);
		}

	}

	/**
	 * Divide the entire input data among NUMTHREADS different threads.
	 * 
	 * @param infile
	 */
	public void learn(String infile) {

		long chunkLength = (long) totalBytes / numThreads;
		Thread[] threadArray = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			long end = chunkLength * (i + 1) - 1;
			if (i == numThreads - 1) {
				end = Long.MAX_VALUE;
			}
			Thread thread = new DataSubsetThread(infile, i, chunkLength * i,
					end);
			threadArray[i] = thread;
			threadArray[i].start();
		}
		for (int i = 0; i < numThreads; i++) {
			try {
				threadArray[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void normalizeWords(String[] words) {
		for (int i = 0; i < words.length; i++) {
			if (words[i].startsWith("@") && words[i].length() > 1) {
				words[i] = "@USER";
			} else if (words[i].startsWith("http")) {
				words[i] = "@URL";
			}
		}

	}

	public void regularize(double samples) {
		double fraction = samples / totalTweets;

		// Another thread is already regularizing
		if (isCurrentlyRegularizing) {
			return;
		}

		isCurrentlyRegularizing = true;

		double batchL2 = fraction * L2;

		for (int k = 0; k < hiddenLayerSize; k++) {
			for (int j = 0; j < vocabSize; j++) {
				outputWeights[k][j] -= (batchL2 * outputWeights[k][j]);
			}
		}

		for (int z = 0; z < numFeatures; z++) {
			for (int j = 0; j < vocabSize; j++) {
				if (z == 0 || nonZeroEmbeddings[z][j] == true) {
					boolean allzero = true;
					for (int k = 0; k < hiddenLayerSize; k++) {
						embeddings[z][j][k] -= (batchL2 * embeddings[z][j][k]);
						if (embeddings[z][j][k] != 0) {
							allzero = false;
						}
					}
					if (allzero) {
						nonZeroEmbeddings[z][j] = false;
					}
				}

			}
		}

		isCurrentlyRegularizing = false;
	}

	/**
	 * Read possible facets from file (list of e.g. state names) and integerize
	 * 
	 * @param infile
	 */
	public void setFeatures(String infile) {
		featureMap = Maps.newHashMap();
		reverseFeatureMap = Maps.newHashMap();

		featureMap.put("MAIN", 0);
		reverseFeatureMap.put(0, "MAIN");

		int current = 1;

		try {
			BufferedReader in1 = new BufferedReader(new InputStreamReader(
					new FileInputStream(infile), "UTF-8"));
			String str1;

			while ((str1 = in1.readLine()) != null) {
				try {
					String key = str1.trim();
					System.out.println("adding " + key);
					featureMap.put(key, current);
					reverseFeatureMap.put(current, key);
					current++;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			in1.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		numFeatures = current;

	}

	/**
	 * Read in target vocabFile (the words to generate representations for, sort
	 * by frequency). Exclude all words in this vocab file that occur fewer than
	 * minimumVocabCount times in the input dataFile.
	 * 
	 * @param vocabFile
	 * @param dataFile
	 */
	public void setVocab(String vocabFile, String dataFile) {
		vocabCounts = Maps.newHashMap();
		int messageOffset=3;

		try {
			BufferedReader in1 = new BufferedReader(new InputStreamReader(
					new FileInputStream(vocabFile), "UTF-8"));
			String str1;

			int c = 0;

			// Read valid vocab from file
			while ((str1 = in1.readLine()) != null) {
				try {
					String[] parts = str1.trim().split("\t");
					String word = parts[1];
					vocabCounts.put(word, 0L);

					c++;
					if (c > maxVocab) {
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			in1.close();

			/*
			 * Get vocab/byte counts in input file
			 */
			in1 = new BufferedReader(new InputStreamReader(new FileInputStream(
					dataFile), "UTF-8"));

			while ((str1 = in1.readLine()) != null) {
				try {
					totalBytes += str1.getBytes().length + 1;
					totalTweets++;

					String[] bigparts = str1.trim().split("\t");
					String words = bigparts[messageOffset];
					String[] parts = words.toLowerCase().split(" ");

					normalizeWords(parts);
					for (String word : parts) {
						if (vocabCounts.containsKey(word)) {
							vocabCounts.put(word, vocabCounts.get(word) + 1);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			in1.close();

			// Filter vocab by counts
			filterVocab();
			vocabCounts = null;
			createBinaryTree();

			System.out.println("Vocab size: " + vocabSize);

			System.out.println(String.format("Total word count: %s",
					totalWordCount));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update learning rate and print progress
	 */
	public void update() {

		alpha = starting_alpha
				* (1 - (double) totalWordsSeen
						/ ((epochs * totalWordCount) + 1));
		if (alpha < starting_alpha * 0.0001)
			alpha = starting_alpha * 0.0001;

		long elapsedTime = System.currentTimeMillis() - startTime;

		lastUpdateWordCount = totalWordsSeen;
		double progress = (double) totalWordsSeen / (epochs * totalWordCount);

		double totalTime = (double) elapsedTime / progress;
		double timeRemaining = totalTime - elapsedTime;
		timeRemaining /= (1000 * 60);
		System.out.print(String.format(
				"alpha: %.5f; Progress: %.1f%%, Time Remaining: %.1f mins\r",
				alpha, progress * 100, timeRemaining));

	}

	/**
	 * Write target embeddings to file
	 * 
	 * @param outputPath
	 * @param id
	 */

	public void write(String modelFile) {

		if (writeFlag) {
			return;
		}
		writeFlag = true;
		BufferedWriter out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(modelFile), "UTF-8"));

			out.write(vocabSize + " " + hiddenLayerSize + "\n");
			for (int f = 0; f < numFeatures; f++) {
				for (int i = 0; i < vocabSize; i++) {
					out.write(reverseFeatureMap.get(f) + " " + vocab[i].word
							+ " ");
					for (int j = 0; j < hiddenLayerSize; j++) {
						out.write(String.format("%.6f", embeddings[f][i][j])
								+ " ");
					}
					out.write("\n");
				}
			}
			out.flush();
			out.close();

		} catch (Exception e) {
			if (detailedOutput) {
				System.out.println("error");
				e.printStackTrace();
			}
		}
		writeFlag = false;
	}

	/**
	 * Write context embeddings to file
	 * 
	 * @param outputPath
	 * @param id
	 */

	public void contexts_write(String modelFile) {

		if (writeFlag) {
			return;
		}
		writeFlag = true;
		BufferedWriter out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(modelFile), "UTF-8"));

			out.write(vocabSize + " " + hiddenLayerSize + "\n");
			for (int i = 0; i < vocabSize; i++) {
				out.write(vocab[i].word + " ");
				for (int j = 0; j < hiddenLayerSize; j++) {
					out.write(String.format("%.6f", outputWeights[j][i])
							+ " ");
				}
				out.write("\n");
			}
			out.flush();
			out.close();

		} catch (Exception e) {
			if (detailedOutput) {
				System.out.println("error");
				e.printStackTrace();
			}
		}
		writeFlag = false;
	}

}
