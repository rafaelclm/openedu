package br.com.openedu.dao;

import java.util.Date;
import org.bson.types.ObjectId;
import br.com.openedu.model.Tutorial;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;

public class TutorialDAO extends BasicDAO{

	public TutorialDAO() {
		super("tutorials");
	}
	
	public void create(Tutorial tutorial) throws MongoException {
		super.insert(tutorial);
	}
	
	public DBCursor find(int skip, int limit) throws MongoException {		
		return super.find().skip(skip).limit(limit);
	}
	
	public DBCursor find(ObjectId author, int skip, int limit) throws MongoException{
		BasicDBObject query = new BasicDBObject("author", author);
		return super.find(query).skip(skip).limit(limit);
	}
	
	public DBCursor find(Date startdate, Date enddate) throws MongoException{
		BasicDBObject query = new BasicDBObject("lastUpdate", new BasicDBObject("$gte", startdate).append("$lte", enddate));
		return super.find(query);
	}
	
	public DBCursor find(String title) throws MongoException{
		BasicDBObject query = new BasicDBObject("title", java.util.regex.Pattern.compile(title));
		return super.find(query);
	}
}
