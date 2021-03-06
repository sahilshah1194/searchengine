package crawler.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class DocumentData {

	@PrimaryKey
	private String url;
	private String content;
	private String lastSeen;
	private ArrayList<String> links;
	
	public void setUrl(String data) {
		url = data;
	}
	
	public void setLinks(ArrayList<String> data){
		links = data;
	}
	
	public void setContent(String data) {
		content = data;
	}
	
	public void setLastSeen(String data) {
		lastSeen = data;
	}
	
	public String getContent() {
		return content;
	}
	
	public String getLastSeen() {
		return lastSeen;
	}
	
	public String getUrl() {
		return url;
	}
	
	public ArrayList<String> getLinks() {
		return links;
	}
}
