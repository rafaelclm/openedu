package br.com.openedu.model;

public enum Role {
	ADMIN(1), MODERATOR(2), TEACHER(3), BASIC(4);
	
	private int value;
	private Role(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
}
