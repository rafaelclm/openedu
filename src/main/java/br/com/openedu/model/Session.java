package br.com.openedu.model;

import java.util.UUID;
import com.mongodb.BasicDBObject;

public class Session extends BasicDBObject {

	private static final long serialVersionUID = 19822794238530695L;

	public UUID getSessionId() {
		return (UUID) get("sessionId");
	}
	
	public void setSessionId(UUID sessionId) {
		super.put("sessionId", sessionId);
	}
	
	public Member getMember() {
		return (Member) get("member");
	}
	
	public void setMember(Member member) {
		super.put("member", member);
	}
}
