package br.com.openedu.dao;

import br.com.openedu.model.Member;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public class MemberDAO extends BasicDAO {

	public MemberDAO() {
		super("members");
	}

	public void create(Member member) throws MongoException {

		super.getCollection().createIndex(new BasicDBObject("email", 1), new BasicDBObject("unique", true));
		super.getCollection().createIndex(new BasicDBObject("tag", 1), new BasicDBObject("unique", true));
		super.insert(member);

	}
	
	public void updateByEmail(Member member) throws MongoException {
		BasicDBObject query = new BasicDBObject("email", member.getString("email"));
		super.update(query, member);
	}

	public DBCursor find(Member member) throws MongoException {
		return super.find(member);
	}

	public boolean exists(String propertie, String value) {
		
		boolean exist = false;
		DBObject query = new BasicDBObject();
		query.put(propertie, value);
		DBCursor cursor = super.find(query);
		if (cursor.count() > 0) {
			exist = true;
		}
		return exist;
	}

}
