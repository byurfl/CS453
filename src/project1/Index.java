package project1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class Index {
	
	// index maps from keyword to docID to occurrences in the document
	Map<String, Map<String, Integer>> _index;
	Map<String, File> _docStore;
	Map<String, Integer> _maxFreqs;
	
	public Index() {
		_index = new TreeMap<String, Map<String, Integer>>();
		_docStore = new HashMap<String, File>();
		_maxFreqs = new HashMap<String, Integer>();
	}
	
	/**
	 * Add a keyword and associated document to the index.
	 * @param token The keyword to add
	 * @param docId The document in which {@code token} was found.
	 */
	public void addToIndex(String token, String docId) {
		Map<String, Integer> docsToFreqs;
		if (_index.containsKey(token)) {
			docsToFreqs = _index.remove(token);
		}
		else {
			docsToFreqs = new HashMap<String, Integer>();
		}
		
		int freq = 1;
		if (docsToFreqs.containsKey(docId)) {
			freq = docsToFreqs.remove(docId);
			freq++;
		}
		docsToFreqs.put(docId, freq);
		_index.put(token, docsToFreqs);
		
		if (!_maxFreqs.containsKey(docId) || _maxFreqs.get(docId) < freq) {
			_maxFreqs.put(docId, freq);
		}
	}
	
	public void storeDocument(String docId, File doc) {
		if (_docStore.containsKey(docId)) {
			System.err.println("Duplicate docID: " + docId);
		}
		
		_docStore.put(docId, doc);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<QueryResult> query(List<String> qTokens) {
		Map<String, Double> docScores = new HashMap<String, Double>();
		
		for (String qt : qTokens) {
			Map<String, Integer> docsToFreqs = _index.get(qt);
			if (docsToFreqs != null) {
				// can just calculate IDF once per term
				double idfFraction = (double)_docStore.keySet().size() / (double)docsToFreqs.keySet().size();
				double idf = Math.log10(idfFraction) / Math.log10(2.0);
				
				for (String docId : docsToFreqs.keySet()) {
					// TF is normalized by the frequency of the most frequently-occurring term in the document
					double tf = (double)docsToFreqs.get(docId) / (double)_maxFreqs.get(docId);
					
					double score = tf * idf;
					
					if (docScores.containsKey(docId)) {
						double sum = docScores.remove(docId);
						sum += score;
						docScores.put(docId, sum);
					}
					else {
						docScores.put(docId, score);
					}
				}
			}
		}
		
		// sort the entries in docScores by score
		List<Entry<String, Double>> entries = new ArrayList<Entry<String, Double>>();
		entries.addAll(docScores.entrySet());
		Collections.sort(entries, new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				Entry<String, Double> e1 = (Entry<String, Double>)o1;
				Entry<String, Double> e2 = (Entry<String, Double>)o2;
				
				Double d1 = e1.getValue();
				Double d2 = e2.getValue();
				return Double.compare(d2,d1);
			}
			
		});
		
		List<QueryResult> results = new ArrayList<QueryResult>();
		for (int i = 0; i < 10 && i < entries.size(); i++) {
			Entry<String, Double> entry = entries.get(i);
			String docId = entry.getKey();
			double score = entry.getValue();
			String snippet = getSnippet(docId);
			
			QueryResult result = new QueryResult(docId, snippet, score);
			results.add(result);
		}
		
		return results;
	}
	
	public String getSnippet(String docId) {
		File document = _docStore.get(docId);
		if (document == null) {
			System.err.println("DocID not found: " + docId);
		}
		else {
			try (Scanner s = new Scanner(document, "UTF-8")) {
				if (s.hasNextLine()) {
					String line = s.nextLine();
					String[] tokens = line.split(" ");
					String result = "";
					for (int i = 0; i < 25 && i < tokens.length; i++) {
						result += tokens[i] + " ";
					}
					return result.trim();
				}
				else {
					throw new Exception("Document with ID " + docId + " is empty!");
				}
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		return null;
	}
}
