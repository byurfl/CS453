package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class AolLogParser {
	
	File dataDir;
	StopWords sw;
	Trie trie;
	Map<String, Map<String, Integer>> modFreqs;
	int maxModFreq;
	String maxModFreqQueries;
	
	public AolLogParser(File dataDirectory) {
		dataDir = dataDirectory;
		sw = new StopWords();
		trie = new Trie();
		modFreqs = new HashMap<String, Map<String, Integer>>();
		maxModFreq = 0;
		maxModFreqQueries = "";
	}
	
	public StopWords getStopWords() {
		return sw;
	}
	
	public Trie getTrie() {
		return trie;
	}
	
	public Map<String, Map<String, Integer>> getModFrequencies() {
		return modFreqs;
	}
	
	public int getMaxModFrequency() {
		return maxModFreq;
	}
	
	public String getMaxModFreqQueries() {
		return maxModFreqQueries;
	}
	
	public void parseLogs() {
//		Trie result = new Trie();
		File[] dataFiles = dataDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".txt");
			}
			
		});
		
		int numQueries = 0;
		int prevSessionId = -1;
		String prevQuery = null;
		LocalDateTime prevQueryTime = null;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		for (File f : dataFiles) {
			try (Scanner s = new Scanner(f, "UTF-8")) {
				s.nextLine(); // skip header
				while (s.hasNextLine()) {
					numQueries++;
					String line = s.nextLine();
					String[] parts = line.split("\\t+");
					
					// mod frequencies
					int id = Integer.parseInt(parts[0]);
					String query = parts[1];
					LocalDateTime qTime =  LocalDateTime.parse(parts[2], dtf);
					if (id == prevSessionId) {
						// same session
						double diff = (qTime.toEpochSecond(ZoneOffset.UTC) - prevQueryTime.toEpochSecond(ZoneOffset.UTC)) / 60.0;
						if (diff <= 10.0 && !query.equals(prevQuery)) {
							Map<String, Integer> value;
							if (modFreqs.containsKey(prevQuery)) {
								value = modFreqs.remove(prevQuery);
							}
							else {
								value = new HashMap<String, Integer>();
							}
							
							int modFreq = 0;
							if (value.containsKey(query)) {
								modFreq = value.remove(query);
							}
							modFreq++;
							value.put(query, modFreq);
							modFreqs.put(prevQuery, value);
							
							if (modFreq > maxModFreq) {
								maxModFreq = modFreq;
								maxModFreqQueries = prevQuery + "\r\n" + query; 
							}
						}
					}
					
//					System.err.println(qTime.getYear() + " " + qTime.getMonthValue() + " " + qTime.getDayOfMonth() + " " + qTime.getHour() + ":" + qTime.getMinute() + ":" + qTime.getSecond());
					
					List<String> tokens = Tokenizer.tokenize(parts[1], false, null);
					if (sw.contains(tokens.get(0))) {
						tokens.remove(0);
					}
					trie.add(tokens);
					prevSessionId = Integer.parseInt(parts[0]);
					prevQuery = query;
					prevQueryTime = qTime;
				}
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
//		System.out.println("Total # of queries: " + numQueries);
//		return trie;
	}
}
