package utils;

import java.util.ArrayList;
import java.util.List;

public class Trie {

	Node root;
	int entryCount;
	int maxFreq;
	
	public Trie() {
		root = new Node();
		entryCount = 0;
		maxFreq = 0;
	}
	
	public void add(List<String> query) {
		List<String> copy = new ArrayList<String>(query);
		int newFreq = root.add(query, false);
		if (newFreq > maxFreq) {
			maxFreq = newFreq;
		}
		entryCount++;
	}
	
	public boolean contains(List<String> query) {
		// list is modified as part of this call,
		// so make a copy to use
		List<String> copy = new ArrayList<String>(query);
		return root.contains(copy, false);
	}
	
	public int count() {
		return entryCount;
	}
	
	public double getMaxFrequency() {
		return maxFreq;
	}
	
	public void setMaxFrequency(int maxFrequency) {
		maxFreq = maxFrequency;
	}
	
	public int getFrequency(List<String> query) {
		List<String> copy = new ArrayList<String>(query);
		return root.getFrequency(copy, false);
	}
	
	public List<String> getSuggestedQueries(List<String> currentQuerySoFar) {
		List<String> copy = new ArrayList<String>(currentQuerySoFar);
		return root.getSuggestedQueries(copy);
	}
}
