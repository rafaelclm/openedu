package br.com.openedu.dao;

import br.com.openedu.model.Tutorial;
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
}
