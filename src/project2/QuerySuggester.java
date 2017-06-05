package project2;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import utils.AolLogParser;
import utils.PorterStemmer;
import utils.Tokenizer;
import utils.Trie;

public class QuerySuggester {

	public static void main(String[] args) {
		File dataDirectory = null;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-d")) {
				dataDirectory = new File(args[++i]);
			}
		}
		
		if (dataDirectory == null || !dataDirectory.exists() || !dataDirectory.isDirectory()) {
			System.err.println("Must enter a valid data directory using option -d <directory>");
			return;
		}
		
		System.out.print("Parsing provided log files...");
		AolLogParser alp = new AolLogParser(dataDirectory);
		alp.parseLogs();
//		System.err.println("Max mod freq. query pair:");
//		System.err.println(alp.getMaxModFreqQueries());
//		System.err.println(alp.getMaxModFrequency());
		Trie t = alp.getTrie();
		Map<String, Map<String, Integer>> modFreqs = alp.getModFrequencies();
		
		System.out.println("done.");
		System.out.println("Log size: " + t.count() + " entries");
		
		PorterStemmer ps = new PorterStemmer();
		String currentQuery = "";
		List<String> currentQueryTokens = new ArrayList<String>();
		try (Scanner s = new Scanner(System.in)) {
			while (s.hasNextLine()) {
//				byte b = s.nextByte();
//				char c = (char)b;
				currentQuery = s.nextLine();
				currentQueryTokens = Tokenizer.tokenize(currentQuery, false, null);
				if (currentQueryTokens.size() > 1 && alp.getStopWords().contains(currentQueryTokens.get(0))) {
					currentQueryTokens.remove(0);
				}
					
				List<String> suggestions = t.getSuggestedQueries(currentQueryTokens);
				List<String> sortedSuggestions = new ArrayList<String>();
				List<Double> sortedRanks = new ArrayList<Double>();
				
				for (String current : suggestions) {
					// current suggestion must be at least one word longer than original query
					List<String> currentSuggestionTokens = Tokenizer.tokenize(current, false, null);
					if (currentSuggestionTokens.size() <= currentQueryTokens.size()) {
						continue;
					}
					
					double freq = (double)t.getFrequency(currentSuggestionTokens) / (double)t.getMaxFrequency();
					
					String word1 = currentQueryTokens.get(currentQueryTokens.size()-1);
					String word2 = currentSuggestionTokens.get(currentQueryTokens.size());
					
					String stem1 = word1;
					if (word1.length() > 1) {
						stem1 = ps.stem(word1);
					}
					
					String stem2 = word2;
					if (word2.length() > 1) {
						stem2 = ps.stem(word2);
					}
					
//					System.out.println("Word 1: " + stem1);
//					System.out.println("Word 2: " + stem2);
					double wcf = getWcfScore(stem1, stem2);
//					double wcf = 1.0;
//					System.out.println("WCF: " + wcf);
					double mods = 0;
					
					Map<String, Integer> modQueries = modFreqs.get(currentQuery);
					if (modQueries != null) {
						if (modQueries.containsKey(current)) {
							mods = modQueries.get(current);
						}
					}
					
//					if (mods != 0) {
//						System.err.println("Found non-zero mod counts!");
//					}
					
					mods = mods / (double)alp.getMaxModFrequency();
					
					// normalize using logs
					freq = (freq > 0) ? Math.log10(freq) : 0;
					wcf = (wcf > 0) ? Math.log10(wcf) : 0;
					mods = (mods > 0) ? Math.log10(mods) : 0;
					
					double srNumerator = freq + wcf + mods;
					double srDenominator = 1 - Math.min(Math.min(freq, wcf), mods);
					
					double suggRank = srNumerator / srDenominator;
					
					if (sortedRanks.isEmpty()) {
						sortedRanks.add(suggRank);
						sortedSuggestions.add(current);
					}
					else {
						boolean added = false;
						for (int i = 0; i < sortedRanks.size(); i++) {
							if (suggRank < sortedRanks.get(i)) {
								sortedRanks.add(i, suggRank);
								sortedSuggestions.add(i, current);
								added = true;
								break;
							}
						}
						if (!added) {
							// smallest score so far, add to end
							sortedRanks.add(suggRank);
							sortedSuggestions.add(current);
						}
					}
				}
				
				if (suggestions.isEmpty()) {
					System.err.println("No suggestions found.");
					continue;
				}
		
				for (int i = 0; i < 8 && i < sortedSuggestions.size(); i++) {
					System.out.println(sortedSuggestions.get(i));
				}
			}
		}
	}
	
	private static double getWcfScore(String word1, String word2) {
		try{
			String myURL = "http://peacock.cs.byu.edu/CS453Proj2/?word1="+word1+"&word2="+word2;

			Document pageDoc = Jsoup.connect(myURL).get();
			String htmlContent = pageDoc.html();		
			Document contentDoc = Jsoup.parse(htmlContent);
			String contentVal = contentDoc.body().text();
			Double val= Double.parseDouble(contentVal);
			
			return val;
		}
		catch (SocketTimeoutException ste) {
			System.err.println("Socket read timed out.");
			return -1.0;
		}
		catch (IOException e) {
			e.printStackTrace();
			return -1.0;
		}
	}
}
