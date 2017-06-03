package project1;

public class QueryResult {
	String _id;
	String _snippet;
	double _score;
	
	public QueryResult(String id, String snippet, double score) {
		_id = id;
		_snippet = snippet;
		_score = score;
	}
	
	public String getId() {
		return _id;
	}
	
	public void setId(String newId) {
		_id = newId;
	}
	
	public String getSnippet() {
		return _snippet;
	}
	
	public void setSnippet(String newSnippet) {
		_snippet = newSnippet;
	}
	
	public double getScore() {
		return _score;
	}
	
	public void setScore(double newScore) {
		_score = newScore;
	}
}
