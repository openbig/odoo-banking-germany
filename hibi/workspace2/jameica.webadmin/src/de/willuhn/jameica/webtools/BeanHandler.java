/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webtools/BeanHandler.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/06/28 09:56:26 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webtools;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import de.willuhn.annotation.Inject;
import de.willuhn.annotation.Lifecycle;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.webadmin.annotation.Request;
import de.willuhn.jameica.webadmin.annotation.Response;
import de.willuhn.logging.Logger;

/**
 * Hilfsklasse fuer den Lifecycle der Beans.
 */
public class BeanHandler
{
  private static Map<String,Object> context = new HashMap<String,Object>();
  
  /**
   * Liefert die Instanz der angegebenen Bean.
   * Die Funktion wertet intern aus, ob fuer die Bean ein
   * Lifecycle definiert ist und entscheidet selbst, ob eine neue Instanz
   * erzeugt wird oder eine existierende wiederverwendet wird.
   * Ist keine Lifecycle-Annotation angegeben, wird per Default
   * ein Request-Lifecycle verwendet.
   * @param config die Request-Config.
   * @param className Name der Klasse der Bean.
   * @return die Instanz der Bean.
   * @throws Exception wenn sich die Bean nicht instanziieren liess.
   */
  static Object getBean(RequestConfig config, String className) throws Exception
  {
    // Keine Klasse angegeben
    if (className == null || className.length() == 0)
    {
      Logger.debug("no class name given");
      return null;
    }

    // Den folgenden Code koennte man mit einem try/finally auch
    // so umstellen, dass der Aufruf "injectContext" nur einmal
    // noetig ist. So ist es aber besser lesbar, da die Lifecycles
    // besser getrennt sind.

    Object bean = context.get(className);
    ////////////////////////////////////////////////////////////////////////////
    // Lifecycle "CONTEXT"
    if (bean != null)
    {
      // Aktuelle Annotations registrieren.
      injectContext(bean,config);
      return bean;
    }
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Lifecycle "SESSION"
    HttpSession session = config.getRequest().getSession();
    bean = session.getAttribute(className);
    if (bean != null)
    {
      // Wir haben die Bean noch in der Session. Dann
      // koennen wir sie auch nochmal verwenden
      injectContext(bean,config);
      return bean;
    }
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Lifecycle "REQUEST" und Erst-Request
    // Wenn wir hier angekommen sind, hat die Bean entweder
    // den Lifecycle "REQUEST" oder es ist der allererste Aufruf.
    // Wir muessen so oder so eine neue Instanz erzeugen
    Class c = config.getClassloader().loadClass(className);
    bean = c.newInstance();
    
    // Lifecycle ermitteln
    Lifecycle lc = (Lifecycle) c.getAnnotation(Lifecycle.class);
    Lifecycle.Type type = lc != null ? lc.value() : Lifecycle.Type.REQUEST;
    
    switch(type)
    {
      case CONTEXT:
        context.put(className,bean);
        break;
      case SESSION:
        session.setAttribute(className,bean);
        break;
    }
    
    injectContext(bean,config);
    return bean;
    ////////////////////////////////////////////////////////////////////////////
  }

  /**
   * Injiziert die Context-Infos.
   * @param bean die Bean.
   * @throws Exception
   */
  private static void injectContext(Object bean, RequestConfig config) throws Exception
  {
    Inject.inject(bean,Request.class,config.getRequest());
    Inject.inject(bean,Response.class,config.getResponse());
  }
  
  /**
   * Durchsucht die Bean nach Properties mit den gleichen Namen wie Parameter im Request
   * und uebernimmt die zugehoerigen Werte des Requests ueber die gleichnamigen Setter
   * in die Bean.
   * @param bean die Bean.
   * @param request der HTTP-Request.
   * @throws Exception
   */
  static void applyRequest(Object bean, HttpServletRequest request) throws Exception
  {
    Enumeration e = request.getParameterNames();
    while (e.hasMoreElements())
    {
      String name  = null;
      String value = null;
      try
      {
        name = (String) e.nextElement();
        value = request.getParameter(name);
        BeanUtil.set(bean,name,value);
      }
      catch (Exception ex)
      {
        // Das koennen wir durchaus ignorieren - das darf passieren
      }
    }
  }

}



/**********************************************************************
 * $Log: BeanHandler.java,v $
 * Revision 1.3  2011/06/28 09:56:26  willuhn
 * @N Lifecycle-Annotation aus jameica.webadmin in util verschoben
 *
 * Revision 1.2  2011-03-30 12:14:05  willuhn
 * @N Neuer Injector fuer DI
 *
 * Revision 1.1  2010-10-27 14:32:18  willuhn
 * @R jameica.webtools ist jetzt Bestandteil von jameica.webadmin. Das separate webtools-Plugin ist nicht mehr noetig
 *
 * Revision 1.2  2010/03/04 13:20:43  willuhn
 * @C Request-Properties nicht auf globale Beans anwenden
 *
 * Revision 1.1  2010/02/01 15:13:36  willuhn
 * @N Neues Element "beans" fuer globale Beans in webtools.xml
 * @N Default-Action via Attribut "action"
 *
 **********************************************************************/