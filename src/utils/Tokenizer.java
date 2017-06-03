package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Tokenizer {
	
	public static List<String> tokenize(String str, boolean splitOnPunctuation, Set<String> dict) {
		List<String> result = new ArrayList<String>();
		
		// split on whitespace and punctuation (if specified)
		String regex = "\\s+";
		if (splitOnPunctuation) {
			regex = "[\\s\\p{P}&&[^-]]+";
		}
		String[] tokens = str.split(regex);
		for (int i = 0; i < tokens.length; i++) {
			String t = tokens[i];
			t = t.toLowerCase().trim();
			if (t.isEmpty()) {
				continue;
			}
			
			if (t.contains("-") && (dict != null)) {
				boolean containsAll = true;
				String[] parts = t.split("-");
				for (String noHyphen : parts) {
					if (dict.contains(noHyphen)) {
						// do nothing...yet
					}
					else {
						containsAll = false;
						break;
					}
				}
				
				if (containsAll) {
					for (String noHyphen : parts) {
						result.add(noHyphen);
					}
				}
				else {
					result.add(t);
				}
			}
			else {
				result.add(t);
			}
		}
		return result;
	}
}
