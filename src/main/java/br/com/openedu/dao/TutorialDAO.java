package br.com.openedu.dao;

import br.com.openedu.model.Tutorial;
import com.mongodb.MongoException;

public class TutorialDAO extends BasicDAO{

	public TutorialDAO() {
		super("tutorials");
	}
	
	public void create(Tutorial tutorial) throws MongoException {
		super.insert(tutorial);
	}
}
