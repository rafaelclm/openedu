package br.com.openedu.util;

import java.util.UUID;
import br.com.openedu.dao.SessionDAO;
import br.com.openedu.model.Member;
import br.com.openedu.model.Session;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public abstract class SessionValidation {
	
	private final SessionDAO sessionDAO;
	private final Session session;
	
	public SessionValidation() {
		session = new Session();
		sessionDAO = new SessionDAO();
	}
	
	public Member validateSession(String sessionId) throws MongoException{
		
		Member member = null;
		
		session.setSessionId(UUID.fromString(sessionId));
		DBCursor cursor = sessionDAO.find(session);
		if(cursor.count() == 1){
			member = new Member();
			member.putAll((DBObject) cursor.next().get("member"));
		}
		
		return member;
	}

}
