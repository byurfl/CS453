package hw3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import utils.Tokenizer;

public class CorpusWords {

	public static void main(String[] args) {
		String dirPath = null;
		boolean doVocab = false;
		
		if (args.length < 2) {
			System.err.println("You must provide a path to a directory containing your corpus!");
		}
		
		for (int i = 0; i < args.length; i++) {
			String option = args[i];
			if (option.equalsIgnoreCase("-P")) {
				try {
					dirPath = args[++i];
				}
				catch (ArrayIndexOutOfBoundsException ex) {
					System.err.println("You must provide a path to a directory containing your corpus!");
				}
			}
			else if (option.equalsIgnoreCase("-V")) {
				doVocab = true;
			}
		}
		
		File corpusDir = new File(dirPath);
		List<File> corpus = new ArrayList<File>();
		for (File f : corpusDir.listFiles()) {
			if (f.isDirectory()) {
				continue;
			}
			
			corpus.add(f);
		}
		
		if (doVocab) {
			doVocab(corpus);
		}
		else {
			doZipf(corpus);
		}
	}

	private static void doZipf(List<File> corpus) {
		
		// count up all the occurrences of every token across all documents
		Map<String, Double> wordsToCounts = new HashMap<String, Double>();
		for (File f : corpus) {
			try (Scanner s = new Scanner(f, "UTF-8")) {
				while (s.hasNextLine()) {
					String line = s.nextLine().trim();
					if (!line.isEmpty()) {
						List<String> tokens = Tokenizer.tokenize(line, true, null);
						for (String t : tokens) {
							double count = 0;
							if (wordsToCounts.containsKey(t)) {
								count = wordsToCounts.remove(t);
							}
							count++;
							wordsToCounts.put(t, count);
						}
					}
				}
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
		}
		calculateK(wordsToCounts, "hw3_zipf_unigrams.txt");
		
		// now do bigrams
		Map<String, Double> bigramsToCounts = new HashMap<String, Double>();
		for (File f : corpus) {
			try (Scanner s = new Scanner(f, "UTF-8")) {
				while (s.hasNextLine()) {
					String line = s.nextLine().trim();
					if (!line.isEmpty()) {
						List<String> tokens = Tokenizer.tokenize(line, true, null);
						for (int i = 0; i < tokens.size()-1; i++) {
							String bigram = tokens.get(i) + " " + tokens.get(i+1);
							double count = 0;
							if (bigramsToCounts.containsKey(bigram)) {
								count = bigramsToCounts.remove(bigram);
							}
							count++;
							bigramsToCounts.put(bigram, count);
						}
					}
				}
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
		}
		calculateK(bigramsToCounts, "hw3_zipf_bigrams.txt");
		
		Map<String, Double> both = new HashMap<String, Double>();
		both.putAll(wordsToCounts);
		both.putAll(bigramsToCounts);
		calculateK(both, "hw3_zipf_both.txt");
	}
	
	private static void calculateK(Map<String, Double> gramsToCounts, String outputFilename) {
		// k = rank * frequency
		List<String> sortedKeys = new ArrayList<String>();
		List<Double> sortedFreqs = new ArrayList<Double>();
		for (String key : gramsToCounts.keySet()) {
			double value = gramsToCounts.get(key);
			
			boolean added = false;
			
			for (int i = 0; i < sortedKeys.size(); i++) {
				if (value > sortedFreqs.get(i)) {
					// insert at i, shift everything else down
					sortedKeys.add(i, key);
					sortedFreqs.add(i, value);
					added = true;
					break;
				}
			}
			
			if (!added) {
				// smallest frequency so far, add to end of list
				sortedKeys.add(key);
				sortedFreqs.add(value);
			}
		}
		
		String outputDir = "C:\\Users\\leer1\\Documents\\aaPERSONAL\\School\\CS453 (Sp2017)\\";
		
		try (PrintWriter p = new PrintWriter(new File(outputDir + outputFilename), "UTF-8")) {
		
			for (int i = 0 ; i < sortedKeys.size(); i++) {
				String gram = sortedKeys.get(i);
				double rank = i+1;
				double freq = sortedFreqs.get(i);
				double k = rank * freq;
				
				p.println(gram + "	" + rank + "	" + freq + "	" + k + "	" + Math.log10(rank) + "	" + Math.log10(freq));
			}
		} 
		catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private static void doVocab(List<File> corpus) {
		Set<String> vocabulary = new HashSet<String>();
		int wordCount = 0;
		for (File f : corpus) {
			try (Scanner s = new Scanner(f, "UTF-8")) {
				while (s.hasNextLine()) {
					String line = s.nextLine().trim();
					if (!line.isEmpty()) {
						List<String> tokens = Tokenizer.tokenize(line, true, null);
						for (String t : tokens) {
							wordCount++;
							vocabulary.add(t);
						}
					}
				}
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			
			System.out.println(wordCount + "	" + vocabulary.size());
		}
	}
}
