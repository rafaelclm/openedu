package br.com.openedu.dao;

import br.com.openedu.model.Session;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;

public class SessionDAO extends BasicDAO {

	public SessionDAO() {
		super("sessions");
	}

	public void create(Session session) throws MongoException{
		super.getCollection().createIndex(new BasicDBObject("sessionId", 1), new BasicDBObject("unique", true));
		super.insert(session);
	}
	
	public DBCursor find(Session session) throws MongoException {
		return super.find(session);
	}
	
	public void updateBySessionId(Session session) throws MongoException{
		BasicDBObject query = new BasicDBObject("sessionId", session.getSessionId());
		super.update(query, session);
	}
}
