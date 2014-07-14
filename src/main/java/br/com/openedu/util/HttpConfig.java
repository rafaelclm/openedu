package br.com.openedu.util;

public class HttpConfig {

	public static String getBaseURI() {
		return HttpParamaters.HOST.getValue() + HttpParamaters.COLON.getValue() + HttpParamaters.PORT.getValue()
						+ HttpParamaters.PATH.getValue();
	}

	private enum HttpParamaters {
		
		HOST("http://localhost"), PORT("8080"), COLON(":"), PATH("/openedu/rest/");

		private String value;

		private HttpParamaters(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
