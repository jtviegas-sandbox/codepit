package org.aprestos.code.bok.logger.interfaces;



public interface LoggerFactoryInterface 
{
	public LoggerInterface getRootLogger();
	public LoggerInterface getLogger(String name);
	public LoggerInterface getLogger(Class<? extends Object> clazz);
}
 
