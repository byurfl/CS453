package project1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import utils.*;

public class Indexer {

	private Index index;
	private StopWords stopWords;
	private PorterStemmer stemmer;
	private Set<String> dict;
	
	private String DICTPATH = "data/dictionary.txt";
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("You must provide a path to a directory containing files to be indexed!");
		}
		
		System.out.print("Building index...");
		File docPath = new File(args[0]);
		List<File> docList = new ArrayList<File>();
		
		for (File f : docPath.listFiles()) {
			if (f.isDirectory()) {
				continue;
			}
			docList.add(f);
		}
		
		Indexer i = new Indexer();
		i.index(docList);
		System.out.println("done.");
		
		System.out.println("Ready to receive queries.");
		String query = null;
		try (Scanner s = new Scanner(System.in)) {
			while (query == null || !query.isEmpty()) {
				query = s.nextLine();
				
				List<QueryResult> results = i.query(query);
				for (QueryResult q : results) {
					String id = q.getId();
					String snippet = q.getSnippet();
					double score = q.getScore();
					
					String toPrint = String.format("%s	%s	%4f", id, snippet, score);
					System.out.println(toPrint);
				}
				
//				System.err.println(query);
				System.out.println();
			}
		}
	}
	
	public Indexer() {
		index = new Index();
		stopWords = new StopWords();
		stemmer = new PorterStemmer();
		dict = readDict(DICTPATH);
	}
	
	private Set<String> readDict(String path) {
		Set<String> result = new HashSet<String>();
		try (Scanner s = new Scanner(new File(path), "UTF-8")) {
			while(s.hasNextLine()) {
				result.add(s.nextLine().trim());
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void index(List<File> documents) {
		for (File d : documents) {
			String filename = d.getName();
			String docId = filename.replace("Doc (", "").replace(").txt", "");
			
			index.storeDocument(docId, d);
			
			List<String> tokens = new ArrayList<String>();
			try (Scanner s = new Scanner(d, "UTF-8")) {
				while (s.hasNextLine()) {
					String line = s.nextLine().trim();
					if (!line.isEmpty()) {
						tokens = process(line);
						for (String t : tokens) {
							index.addToIndex(t, docId);
						}
					}
				}
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<QueryResult> query(String query) {
		List<String> tokens = process(query);
		return index.query(tokens);
	}
	
	public String getSnippet(String docId) {
		return index.getSnippet(docId);
	}
	
	public List<String> process(String line) {
		List<String> tokens = new ArrayList<String>();
		tokens = Tokenizer.tokenize(line, true, dict);
		tokens = removeStopwords(tokens);
		tokens = stem(tokens);
		return tokens;
	}
	
//	private List<String> tokenize(String line) {
//		List<String> result = new ArrayList<String>();
//		
//		// split on whitespace and punctuation
//		String[] tokens = line.split("[\\s\\p{P}&&[^-]]+");
//		for (int i = 0; i < tokens.length; i++) {
//			String t = tokens[i];
//			t = t.toLowerCase().trim();
//			if (t.isEmpty()) {
//				continue;
//			}
//			
//			if (t.contains("-")) {
////				String noHyphen = t.replace("-", "");
//				boolean containsAll = true;
//				String[] parts = t.split("-");
//				for (String noHyphen : parts) {
//					if (dict.contains(noHyphen)) {
////						result.add(noHyphen);
//						// do nothing
//					}
//					else {
////						result.add(t);
//						containsAll = false;
//						break;
//					}
//				}
//				
//				if (containsAll) {
//					for (String noHyphen : parts) {
//						result.add(noHyphen);
//					}
//				}
//				else {
//					result.add(t);
//				}
//			}
//			else {
//				result.add(t);
//			}
//		}
//		return result;
//	}
	
	private List<String> removeStopwords(List<String> tokens) {
		List<String> result = new ArrayList<String>();
		for (String t : tokens) {
			if (stopWords.contains(t)) {
				continue;
			}
			result.add(t);
		}
		return result;
	}
	
	private List<String> stem(List<String> tokens) {
		List<String> result = new ArrayList<String>();
		for (String t : tokens) {
			boolean hasDigit = false;
			for (int i = 0; i < t.length(); i++) {
				if (Character.isDigit(t.charAt(i))) {
					hasDigit = true;
					break;
				}
				if (hasDigit) {
					break;
				}
			}
			if (!hasDigit) {
				result.add(stemmer.stem(t.trim()));
			}
			else {
				result.add(t.trim());
			}
		}
		return result;
	}
}
