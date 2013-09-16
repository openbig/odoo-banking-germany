/**
 * $Id: JameicaLoggerAdapter.java,v 1.3 2008/08/08 11:24:48 willuhn Exp $
 */
package org.slf4j.impl;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

import org.slf4j.Marker;

import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * @author Markus Wolf <markus@emedia-solutions-wolf.de>
 */
public class JameicaLoggerAdapter implements org.slf4j.Logger {
  static
  {
    // Wir deaktivieren den Console-Logger aus, weil wir
    // die ganzen Ausgaben durch Jameica ohnehin schon dort
    // haben und sonst doppelt haetten
    java.util.logging.Logger logger = java.util.logging.Logger.getLogger("");
    Handler[] handlers = logger.getHandlers();
    if (handlers != null)
    {
      for (int i=0;i<handlers.length;++i)
      {
        if (handlers[i] == null)
          continue;
        if (handlers[i] instanceof ConsoleHandler)
        {
          Logger.info("disable console logging for java logging");
          handlers[i].setLevel(java.util.logging.Level.OFF);
        }
      }
    }
  }

	/**
	 * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void debug(Marker marker, String s, Object obj, Object obj1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object)
	 */
	public void debug(Marker marker, String s, Object obj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object[])
	 */
	public void debug(Marker marker, String s, Object[] aobj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void debug(Marker marker, String s, Throwable throwable) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String)
	 */
	public void debug(Marker marker, String s) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object,
	 *      java.lang.Object)
	 */
	public void debug(String s, Object obj, Object obj1) {
		debug(replace(s, obj, obj1));
	}

	/**
	 * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object)
	 */
	public void debug(String s, Object obj) {
		debug(replace(s, obj));
	}

	/**
	 * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object[])
	 */
	public void debug(String s, Object[] aobj) {
		debug(replace(s, aobj));
	}

	/**
	 * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Throwable)
	 */
	public void debug(String s, Throwable throwable) {
		Logger.write(Level.DEBUG, s, throwable);
	}

	/**
	 * @see org.slf4j.Logger#debug(java.lang.String)
	 */
	public void debug(String s) {
		Logger.debug(s);
	}

	/**
	 * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void error(Marker marker, String s, Object obj, Object obj1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object)
	 */
	public void error(Marker marker, String s, Object obj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object[])
	 */
	public void error(Marker marker, String s, Object[] aobj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void error(Marker marker, String s, Throwable throwable) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String)
	 */
	public void error(Marker marker, String s) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object,
	 *      java.lang.Object)
	 */
	public void error(String s, Object obj, Object obj1) {
		error(replace(s, obj, obj1));
	}

	/**
	 * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object)
	 */
	public void error(String s, Object obj) {
		error(replace(s, obj));
	}

	/**
	 * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object[])
	 */
	public void error(String s, Object[] aobj) {
		error(replace(s, aobj));
	}

	/**
	 * @see org.slf4j.Logger#error(java.lang.String, java.lang.Throwable)
	 */
	public void error(String s, Throwable throwable) {
		Logger.write(Level.ERROR, s, throwable);
	}

	/**
	 * @see org.slf4j.Logger#error(java.lang.String)
	 */
	public void error(String s) {
		Logger.error(s);
	}

	/**
	 * @see org.slf4j.Logger#getName()
	 */
	public String getName() {
		return "Jameica Logger"; //$NON-NLS-1$
	}

	/**
	 * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void info(Marker marker, String s, Object obj, Object obj1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object)
	 */
	public void info(Marker marker, String s, Object obj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object[])
	 */
	public void info(Marker marker, String s, Object[] aobj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void info(Marker marker, String s, Throwable throwable) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String)
	 */
	public void info(Marker marker, String s) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object,
	 *      java.lang.Object)
	 */
	public void info(String s, Object obj, Object obj1) {
		info(replace(s, obj, obj1));
	}

	/**
	 * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object)
	 */
	public void info(String s, Object obj) {
		info(replace(s, obj));
	}

	/**
	 * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object[])
	 */
	public void info(String s, Object[] aobj) {
		info(replace(s, aobj));
	}

	/**
	 * @see org.slf4j.Logger#info(java.lang.String, java.lang.Throwable)
	 */
	public void info(String s, Throwable throwable) {
		Logger.write(Level.INFO, s, throwable);
	}

	/**
	 * @see org.slf4j.Logger#info(java.lang.String)
	 */
	public void info(String s) {
		Logger.debug(s);
	}

	/**
	 * @see org.slf4j.Logger#isDebugEnabled()
	 */
	public boolean isDebugEnabled() {
		return isEnabled(Level.DEBUG);
	}

	/**
	 * @see org.slf4j.Logger#isDebugEnabled(org.slf4j.Marker)
	 */
	public boolean isDebugEnabled(Marker marker) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#isErrorEnabled()
	 */
	public boolean isErrorEnabled() {
		return isEnabled(Level.ERROR);
	}

	/**
	 * @see org.slf4j.Logger#isErrorEnabled(org.slf4j.Marker)
	 */
	public boolean isErrorEnabled(Marker marker) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#isInfoEnabled()
	 */
	public boolean isInfoEnabled() {
		return isEnabled(Level.INFO);
	}

	/**
	 * @see org.slf4j.Logger#isInfoEnabled(org.slf4j.Marker)
	 */
	public boolean isInfoEnabled(Marker marker) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#isTraceEnabled()
	 */
	public boolean isTraceEnabled() {
		return isEnabled(Level.DEBUG);
	}

	/**
	 * @see org.slf4j.Logger#isTraceEnabled(org.slf4j.Marker)
	 */
	public boolean isTraceEnabled(Marker marker) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#isWarnEnabled()
	 */
	public boolean isWarnEnabled() {
		return isEnabled(Level.WARN);
	}

	/**
	 * @see org.slf4j.Logger#isWarnEnabled(org.slf4j.Marker)
	 */
	public boolean isWarnEnabled(Marker marker) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void trace(Marker marker, String s, Object obj, Object obj1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object)
	 */
	public void trace(Marker marker, String s, Object obj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object[])
	 */
	public void trace(Marker marker, String s, Object[] aobj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void trace(Marker marker, String s, Throwable throwable) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String)
	 */
	public void trace(Marker marker, String s) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object,
	 *      java.lang.Object)
	 */
	public void trace(String s, Object obj, Object obj1) {
		debug(replace(s, obj, obj1));
	}

	/**
	 * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object)
	 */
	public void trace(String s, Object obj) {
		debug(replace(s, obj));
	}

	/**
	 * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object[])
	 */
	public void trace(String s, Object[] aobj) {
		debug(replace(s, aobj));
	}

	/**
	 * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Throwable)
	 */
	public void trace(String s, Throwable throwable) {
		Logger.write(Level.DEBUG, s, throwable);
	}

	/**
	 * @see org.slf4j.Logger#trace(java.lang.String)
	 */
	public void trace(String s) {
		Logger.debug(s);
	}

	/**
	 * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void warn(Marker marker, String s, Object obj, Object obj1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object)
	 */
	public void warn(Marker marker, String s, Object obj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Object[])
	 */
	public void warn(Marker marker, String s, Object[] aobj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void warn(Marker marker, String s, Throwable throwable) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String)
	 */
	public void warn(Marker marker, String s) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object,
	 *      java.lang.Object)
	 */
	public void warn(String s, Object obj, Object obj1) {
		warn(replace(s, obj, obj1));
	}

	/**
	 * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object)
	 */
	public void warn(String s, Object obj) {
		warn(replace(s, obj));
	}

	/**
	 * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object[])
	 */
	public void warn(String s, Object[] aobj) {
		warn(replace(s, aobj));
	}

	/**
	 * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Throwable)
	 */
	public void warn(String s, Throwable throwable) {
		Logger.write(Level.WARN, s, throwable);
	}

	/**
	 * @see org.slf4j.Logger#warn(java.lang.String)
	 */
	public void warn(String s) {
		Logger.warn(s);
	}

	/**
   * Prueft, ob das angegebene Level Verwendung findet.
	 * @param level zu pruefendes Level.
	 * @return true, wenn es aktiv ist.
	 */
	private boolean isEnabled(Level level) {
		return Logger.getLevel().getValue() <= level.getValue();
	}

  /**
   * Fuellt Platzhalter.
   * @param s Text mit Platzhaltern.
   * @param param Wert 1 fuer die Platzhalter.
   * @return der ersetzte Text.
   */
  private String replace(String s, Object param) {
    return replace(s,new Object[]{param});
  }

  /**
   * Fuellt Platzhalter.
   * @param s Text mit Platzhaltern.
   * @param param1 Wert 1 fuer die Platzhalter.
   * @param param2 Wert 1 fuer die Platzhalter.
   * @return der ersetzte Text.
   */
  private String replace(String s, Object param1, Object param2) {
    return replace(s,new Object[]{param1,param2});
  }

  /**
   * Fuellt Platzhalter.
   * @param s Text mit Platzhaltern.
   * @param params Werte fuer die Platzhalter.
   * @return der ersetzte Text.
   */
	private String replace(String s, Object[] params) {
		for (int i=0;i<params.length;++i) {
			String v = "";
			if (params[i] != null) {
				v = params[i].toString();
				if (v.contains("$")) {
					v = v.replace("$", "\\$");
				}
			}
			s = s.replaceFirst("\\{\\}", v);
		}
		return s;
	}
}
