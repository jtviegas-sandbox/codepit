package org.challenges.adthena.shopbasket.exceptions;

public class ConfigException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConfigException() {
		super();
	}

	public ConfigException(String arg0) {
		super(arg0);
	}

	public ConfigException(Throwable arg0) {
		super(arg0);
	}

	public ConfigException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ConfigException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
