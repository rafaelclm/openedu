package br.com.openedu.model;

import java.util.Date;
import com.mongodb.BasicDBObject;

public class Member extends BasicDBObject {

	private static final long serialVersionUID = -3282300734571810356L;
	
	public String name() {
		return getString("name");
	}

	public String getName() {
		return getString("name");
	}

	public void setName(String name) {
		super.put("name", name);
	}

	public Gender getGender() {
		return (Gender) get("gender");
	}

	public void setGender(Gender gender) {
		super.put("gender", gender);
	}

	public int getAge() {
		return getInt("age");
	}

	public void setAge(int age) {
		super.put("age", age);
	}

	public Role getRole() {
		return (Role) get("role");
	}

	public void setRole(Role role) {
		super.put("role", role);
	}

	public String getEmail() {
		return getString("email");
	}

	public void setEmail(String email) {
		super.put("email", email);
	}

	public void setPassword(String password) {
		super.put("password", password);
	}
	
	public String getTag() {
		return getString("tag");
	}
	
	public void setTag(String tag) {
		super.put("tag", tag);
	}
	
	public Date getSince(){
		return getDate("since");
	}
	
	public void setSince(Date since) {
		super.put("since", since);
	}
	
	public String getDropBoxToken() {
		return getString("dropBoxToken");
	}
	
	public void setDropBoxToken(String dropBoxToken) {
		super.put("dropBoxToken", dropBoxToken);
	}

}
