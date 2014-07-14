package br.com.openedu.model;

public enum Gender {
	MALE(1), FEMALE(2);
	
	private final int value;
	
	private Gender(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
}
