package br.com.openedu.model;

public enum Codes {
	
	EMAIL_ALREADY_EXISTS(1000), 
	SUCCESSFULL_CREATED(1010), 
	MONGODB_EXCEPTION(1020), 
	TAG_ALREADY_EXISTS(1030), 
	SESSION_CREATED(1040), 
	NOT_EXISTS_MEMBER(1050), 
	EXISTS_SESSION(1060), 
	NOT_EXISTS_SESSION(1070),
	MEMBER_UPDATED(1080),
	DROPBOX_CREATED(1090),
	TOKEN_DROPBOX_NOT_FOUND(1100), 
	BAD_FORMATTED_DROPBOX_PATH(1110), 
	CONTAINS_ERRORS(1120), 
	EMAIL_CANT_BE_EMPTY_OR_NULL(1130), 
	EMAIL_FORMAT_NOT_MATCH(1140), 
	NAME_CANT_BE_EMPTY_OR_NULL(1150), 
	TITLE_CANT_BE_NULL_OR_EMPTY(1160), 
	AUTHOR_NOT_INFORMED(1170), 
	CATEGORY_CANT_BE_NULL_OR_EMPTY(1180);

	private int value;

	private Codes(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
