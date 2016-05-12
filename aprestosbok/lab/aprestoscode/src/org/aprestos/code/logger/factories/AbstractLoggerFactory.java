package org.aprestos.code.logger.factories;

import java.util.Properties;

import org.aprestos.code.exceptions.FactoryInitializeException;
import org.aprestos.code.logger.interfaces.LoggerFactoryInterface;
import org.aprestos.code.logger.interfaces.LoggerInterface;


public abstract class AbstractLoggerFactory implements LoggerFactoryInterface
{
	/**
	 *@see org.aprestos.code.logger.interfaces.LoggerFactoryInterface#initialize(java.util.Properties)
	 */
	public static LoggerFactoryInterface getFactory(Properties props) throws FactoryInitializeException
	{
	    return parseLoggingProvider(props);
	}
	 
	/**
	 * @see org.aprestos.code.logger.interfaces.LoggerFactoryInterface#getLogger(java.lang.String)
	 */
	public abstract LoggerInterface getLogger(String name);
	 
	/**
	 * @see org.aprestos.code.logger.interfaces.LoggerFactoryInterface#getLogger(java.lang.Class)
	 */
	public abstract LoggerInterface getLogger(Class clazz) ;
	
	/**
	 * @see org.aprestos.code.logger.interfaces.LoggerFactoryInterface#getRootLogger()
	 */
	public abstract LoggerInterface getRootLogger();
	 
	
	private static LoggerFactoryInterface parseLoggingProvider(Properties props) throws FactoryInitializeException
	{
	    LoggerFactoryInterface result = null;
	    
	    //this method can be changed to accommodate for additional logging providers
	    try
	    {
	    	result = new Log4JLoggerFactory(props);
	    }
	
	    catch(Exception x)
	    {
	    	throw new FactoryInitializeException(x);
	    }
	    
	    return result;
	    
	}
}
 
