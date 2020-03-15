package org.aprestos.labs.spring.microservices.datalayer.exceptions;

public class DataValidationException extends DataLayerException {

  private static final long serialVersionUID = 1L;

  public DataValidationException() {
  }

  public DataValidationException(String message) {
    super(message);
  }

  public DataValidationException(Throwable cause) {
    super(cause);
  }

  public DataValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataValidationException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
