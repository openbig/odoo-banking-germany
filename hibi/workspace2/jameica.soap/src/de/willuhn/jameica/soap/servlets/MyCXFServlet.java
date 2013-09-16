/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.soap/src/de/willuhn/jameica/soap/servlets/MyCXFServlet.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/08/08 11:24:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.soap.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.cxf.BusFactory;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Ueberschrieben, um manuell Services deployen zu koennen, ohne sie
 * in cxf.xml manuell deklarieren zu muessen. Auf diese Weise koennen
 * auch andere Plugins Web-Services zur Verfuegung stellen, ohne
 * die Datei aendern zu muessen.
 * Siehe http://cwiki.apache.org/CXF20DOC/servlet-transport.html
 */
public class MyCXFServlet extends CXFNonSpringServlet implements MessageConsumer
{
  /**
   * @see org.apache.cxf.transport.servlet.CXFNonSpringServlet#loadBus(javax.servlet.ServletConfig)
   */
  public void loadBus(ServletConfig config) throws ServletException
  {
    super.loadBus(config);
    BusFactory.setDefaultBus(this.getBus()); // Wir machen den Servlet-Bus zum Default-Bus
    Application.getMessagingFactory().getMessagingQueue("jameica.soap.interceptor.add").registerMessageConsumer(this);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{QueryMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    QueryMessage msg = (QueryMessage) message;
    
    Object o = msg.getData();
    if (o == null || !(o instanceof Interceptor))
      return;
    
    String direction = msg.getName();
    if ("in".equals(direction))
    {
      Logger.info("register IN interceptor " + o.getClass().getName());
      this.getBus().getInInterceptors().add((Interceptor) o);
    }
    else
    {
      Logger.info("register OUT interceptor " + o.getClass().getName());
      this.getBus().getOutInterceptors().add((Interceptor) o);
    }
  }
}


/**********************************************************************
 * $Log: MyCXFServlet.java,v $
 * Revision 1.5  2008/08/08 11:24:26  willuhn
 * @N Console-Logging von Java-Logging ausschalten. Da wir es auf den Jameica-Logger umbiegen, wuerde es sonst doppelt auf der Console erscheinen
 *
 * Revision 1.4  2008/08/07 15:37:43  willuhn
 * @N MessageConsumer zum Registrieren von Interceptors in CXF
 *
 * Revision 1.3  2008/07/11 15:38:52  willuhn
 * @N Service-Deployment
 *
 * Revision 1.2  2008/07/10 09:19:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2008/07/09 23:30:53  willuhn
 * @R Nicht benoetigte Jars (gemaess WHICH_JARS) entfernt
 * @N Deployment vereinfacht
 *
 **********************************************************************/
