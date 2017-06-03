package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
	Map<Character, Node> children;
	boolean complete;
	int frequency;
	
	public Node() {
		children = new HashMap<Character, Node>();
		complete = false;
		frequency = 0;
	}
	
	public int add(List<String> words, boolean addSpace) {
		if (words.size() == 0) {
			return 0;
		}
		else if (words.size() == 1) {
			String word = words.remove(0);
			if (addSpace) {
				word = " " + word;
			}
			Node n = add(word);
			n.complete = true;
			n.frequency++;
			
			return n.frequency;
		}
		else {
			String word = words.remove(0);
			if (addSpace) {
				word = " " + word;
			}
			Node n = add(word);
			return n.add(words, true);
		}
	}
	
	public Node add(String word) {
		if (word.length() == 0) {
//			complete = true;
			return this;
		}
		else {
			Character c = word.charAt(0);
			String rest = word.substring(1, word.length());
			Node child;
			if (children.containsKey(c)) {
				child = children.get(c);
			}
			else {
				child = new Node();
				children.put(c, child);
			}
			return child.add(rest);
		}
	}
	
	public boolean contains(List<String> q, boolean addSpace) {
		if (q.size() == 0) {
			return complete;
		}
		else {
			String word = q.remove(0);
			if (addSpace) {
				word = " " + word;
			}
			Node n = contains(word);
			if (n == null) {
				return false;
			}
			else {
				return n.contains(q, true);
			}
		}
	}
	
	private Node contains(String s) {
		if (s.length() == 0) {
			return this;
		}
		else {
			Character c = s.charAt(0);
			String rest = s.substring(1, s.length());
			
			if (!children.containsKey(c)) {
				return null;
			}
			else {
				return children.get(c).contains(rest);
			}
		}
	}
	
	public int getFrequency(List<String> query, boolean addSpace) {
		if (query.size() == 0) {
			return frequency;
		}
		else {
			String word = query.remove(0);
			if (addSpace) {
				word = " " + word;
			}
			Node n = contains(word);
			if (n == null) {
				return 0;
			}
			else {
				return n.getFrequency(query, true);
			}
		}
	}
	
	public List<String> getSuggestedQueries(List<String> currentQuerySoFar) {
		
		List<String> result = new ArrayList<String>();
		
		Node n = null;
		String word = null;
		String queryString = "";
		if (currentQuerySoFar.size() > 0) {
			word = currentQuerySoFar.remove(0);
			queryString += word;
			n = contains(word);
		}
		while (currentQuerySoFar.size() > 0) {
			
			if (n == null) {
				return result;
			}
			
			word = " " + currentQuerySoFar.remove(0);
			queryString += word;
			n = n.contains(word);
		}
		
		if (n == null) {
			return result;
		}
		
		for (Character c : n.children.keySet()) {
			n.children.get(c).buildSuggestions(queryString + c, result);
		}
		
		return result;
	}
	
	private void buildSuggestions(String qStringSoFar, List<String> result) {
		if (complete) {
			result.add(qStringSoFar);
		}
		
		for (Character c : children.keySet()) {
			children.get(c).buildSuggestions(qStringSoFar + c, result);
		}
	}
}
