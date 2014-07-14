package br.com.openedu.dao;

import br.com.openedu.mongodb.MongoResource;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;

public abstract class BasicDAO {

	private final MongoResource mongoResource = MongoResource.INSTANCE;
	private final DBCollection collection;

	public BasicDAO(String collectionName) {
		collection = mongoResource.getDataBase().getCollection(collectionName);
	}

	public WriteResult insert(DBObject object) throws MongoException {
		try {
			WriteResult result = collection.insert(object, WriteConcern.SAFE);
			return result;
		} catch (MongoException exception) {
			throw new MongoException(exception.getMessage());
		}

	}
	
	public GridFS getGridFS(String namespace){
		return new GridFS(mongoResource.getDataBase(), namespace);
	}
	
	public WriteResult update(BasicDBObject query, BasicDBObject object) throws MongoException{
		try {
			WriteResult result = collection.update(query, object);
			return result;
		} catch (MongoException exception) {
			throw new MongoException(exception.getMessage());
		}
	}

	public DBCursor find(DBObject query) {
		try {
			return collection.find(query);
		} catch (Exception exception) {
			throw new MongoException(exception.getMessage());
		}
	}

	public DBCollection getCollection() {
		return collection;
	}

}
