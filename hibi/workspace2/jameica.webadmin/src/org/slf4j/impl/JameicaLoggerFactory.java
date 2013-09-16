/**
 * $Id: JameicaLoggerFactory.java,v 1.1 2008/04/27 23:32:02 willuhn Exp $
 */
package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * @author Markus Wolf <markus@emedia-solutions-wolf.de>
 */
public class JameicaLoggerFactory implements ILoggerFactory {

	private final Logger logger = new JameicaLoggerAdapter();

	/**
	 * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
	 */
	public Logger getLogger(String s) {
		return this.logger;
	}

}
