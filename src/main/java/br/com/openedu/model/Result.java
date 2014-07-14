package br.com.openedu.model;

import java.util.Map;

import com.mongodb.BasicDBObject;

public class Result extends BasicDBObject{

	private static final long serialVersionUID = -5701721496095710566L;
	private int code;
	private Map<?, ?> entity;
	
	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public Map<?, ?> getEntity() {
		return entity;
	}
	
	public void setEntity(Map<?, ?> entity) {
		this.entity = entity;
	}
	
}
