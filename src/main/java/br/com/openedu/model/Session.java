package br.com.openedu.model;

import java.util.Date;
import java.util.UUID;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Session extends BasicDBObject {

	private static final long serialVersionUID = 19822794238530695L;

	public UUID getSessionId() {
		return (UUID) get("sessionId");
	}
	
	public void setSessionId(UUID sessionId) {
		super.put("sessionId", sessionId);
	}
	
	public Date getExpirationDate() {
		return getDate("expirationDate");
	}
	
	public void setExpirationDate(Date expirationDate) {
		super.put("expirationDate", expirationDate);
	}
	
	public String getMember() {
		return getString("member");
	}
	
	public void setMember(String member) {
		super.put("member", member);
	}
}
