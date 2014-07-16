package br.com.openedu.model;

import java.util.Date;
import org.bson.types.ObjectId;
import com.mongodb.BasicDBObject;

public class Tutorial extends BasicDBObject {

	private static final long serialVersionUID = 7514385180209576350L;
	
	public String getSessionId() {
		return getString("sessionId");
	}
	
	public void setSessionId(String sessionId) {
		super.put("sessionId", sessionId);
	}
	
	public String getTitle() {
		return getString("title");
	}
	
	public void setTitle(String title) {
		super.put("title", title);
	}
	
	public String getBody(){
		return getString("body");
	}
	
	public void setBody(String body) {
		super.put("body", body);
	}
	
	public Date getCreationDate() {
		return getDate("creationDate");
	}
	
	public void setCreationDate(Date creationDate) {
		super.put("creationDate", creationDate);
	}
	
	public Date getLastUpdate() {
		return getDate("lastUpdate");
	}
	
	public void setLastUpdate(Date lastUpdate) {
		super.put("lastUpdate", lastUpdate);
	}
	
	public String getImageId() {
		return getString("imageId");
	}
	
	public void setImageId(String imageId) {
		super.put("imageId", imageId);
	}
	
	public String getVideoURL() {
		return getString("videoURL");
	}
	
	public void setVideoURL(String videoURL) {
		super.put("videoURL", videoURL);
	}
	
	public String getImagePathDropBox(){
		return getString("imagePathDropBox");
	}
	
	public void setImagePathDropBox(String imagePathDropBox){
		super.put("imagePathDropBox", imagePathDropBox);
	}

	public ObjectId getAuthor() {
		return (ObjectId) get("author");
	}
	
	public void setAuthor(ObjectId memberId) {
		super.put("author", memberId);
	}
	
	public boolean isPublished() {
		return getBoolean("published");
	}
	
	public void setPublished(boolean published) {
		super.put("published", published);
	}
}
