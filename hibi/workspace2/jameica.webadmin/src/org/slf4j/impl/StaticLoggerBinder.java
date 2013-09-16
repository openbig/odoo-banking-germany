/**
 * $Id: StaticLoggerBinder.java,v 1.2 2008/05/30 11:55:37 willuhn Exp $
 */
package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * @author Markus Wolf <markus@emedia-solutions-wolf.de>
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

	/**
	 * Singleton.
	 */
	public static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

	private final ILoggerFactory loggerFactory = new JameicaLoggerFactory();

	private StaticLoggerBinder() {
	}

	/**
	 * @see org.slf4j.spi.LoggerFactoryBinder#getLoggerFactory()
	 */
	public ILoggerFactory getLoggerFactory() {
		return this.loggerFactory;
	}

	/**
	 * @see org.slf4j.spi.LoggerFactoryBinder#getLoggerFactoryClassStr()
	 */
	public String getLoggerFactoryClassStr() {
		return JameicaLoggerFactory.class.getName();
	}
}
