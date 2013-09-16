/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/server/RestServiceImpl.java,v $
 * $Revision: 1.29 $
 * $Date: 2011/06/28 09:56:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import de.willuhn.annotation.Inject;
import de.willuhn.annotation.Lifecycle;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.annotation.Doc;
import de.willuhn.jameica.webadmin.annotation.Path;
import de.willuhn.jameica.webadmin.annotation.Request;
import de.willuhn.jameica.webadmin.annotation.Response;
import de.willuhn.jameica.webadmin.beans.RestBeanDoc;
import de.willuhn.jameica.webadmin.beans.RestMethodDoc;
import de.willuhn.jameica.webadmin.rmi.RestService;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung des REST-Services.
 */
public class RestServiceImpl implements RestService
{
  private Map<String,Method> commands     = null;
  private Map<String,Object> contextScope = null;
  private List<RestBeanDoc> doc           = null;

  private MessageConsumer register    = new RestConsumer(true);
  private MessageConsumer unregister  = new RestConsumer(false);


  /**
   * @see de.willuhn.jameica.webadmin.rmi.RestService#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
  {
    if (!this.isStarted())
      throw new IOException("REST service not started");

    // Wie Umlaute im Query-String (also URL+GET-Parameter) codiert sind,
    // ist nirgends so richtig spezifiziert. Also schicken es die
    // Browser unterschiedlich. Jetty interpretiert es als UTF-8.
    // Wenn man auf dem Client jedoch ISO-8859-15 als Zeichensatz
    // eingestellt hat, schickt es auch der Browser damit.
    // Effekt: Der Server weiss nicht, wie er die Umlaute interpretieren soll.
    // Das fuehrt bisweilen soweit, dass nicht nur die Umlaute in request.getParameter(name)
    // kaputt sind sondern ggf. auch noch die Buchstaben hinter dem Umlaut
    // verloren gehen.
    // Immerhin bietet Jetty eine Funktion an, um das Encoding
    // vorzugeben. Das gaenge mit explizit mit:
    // ((org.mortbay.jetty.Request)request).setQueryEncoding(String);
    // oder implizit via (damit muss der Request nicht auf org.mortbay.jetty.Request gecastet werden:
    // request.setAttribute("org.mortbay.jetty.Request.queryEncoding","String");
    
    // siehe hierzu auch
    // http://jira.codehaus.org/browse/JETTY-113
    // http://jetty.mortbay.org/jetty5/faq/faq_s_900-Content_t_International.html
    String queryencoding = de.willuhn.jameica.webadmin.Settings.SETTINGS.getString("http.queryencoding",null);
    int jsonIndent       = de.willuhn.jameica.webadmin.Settings.SETTINGS.getInt("json.indent",2);
    if (queryencoding != null)
    {
      Logger.debug("query encoding: " + queryencoding);
      request.setAttribute("org.mortbay.jetty.Request.queryEncoding",queryencoding);
    }
    String command = request.getPathInfo();
    if (command == null)
      throw new IOException("missing REST command");
    
    Iterator<String> patterns = this.commands.keySet().iterator();

    try
    {
      while (patterns.hasNext())
      {
        String path     = patterns.next();
        Method method   = this.commands.get(path);

        Pattern pattern = Pattern.compile(path);
        Matcher match   = pattern.matcher(command);
        
        if (match.matches())
        {
          Object[] params = new Object[match.groupCount()];
          for (int k=0;k<params.length;++k)
            params[k] = match.group(k+1); // wir fangen bei "1" an, weil an Pos. 0 der Pattern selbst steht

          Object bean = getBean(method.getDeclaringClass(),request);
          applyAnnotations(bean, request, response);

          Logger.debug("applying command " + path + " to " + method);
          Object value = method.invoke(bean,params);
          if (method.getReturnType() != null && value != null)
          {
            String s = null;
            if ((value instanceof JSONObject) && jsonIndent > 0)
              s = ((JSONObject)value).toString(jsonIndent);
            else if ((value instanceof JSONArray) && jsonIndent > 0)
              s = ((JSONArray)value).toString(jsonIndent);
            else
              s = value.toString();
            response.getWriter().print(s);
          }
          return;
        }
      }
    }
    catch (Exception e)
    {
      String errorText = null;
      if (e instanceof InvocationTargetException)
      {
        Throwable cause = e.getCause();
        if (cause != null && (cause instanceof IOException || cause instanceof ApplicationException))
          errorText = cause.getMessage();
      }
      
      if (errorText == null)
      {
        // Wir wissen nicht, wie wir den Fehler behandeln sollen.
        // Also ist es ein unerwarteter Fehler - und den loggen wir
        Logger.error("error while executing command " + command,e);
        errorText = e.getMessage();
      }
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,errorText);
      return;
    }
    throw new IOException("no command found for REST url " + command);
  }
  
  /**
   * Liefert eine Instanz der Bean.
   * Die Funktion wertet die Lifecycle-Annotation der Bean aus
   * und verwendet eine eventuell vorhandene Instanz, falls sie
   * mit dem Lifecycle Context oder Session markiert ist.
   * @param c die Klasse der Bean.
   * @param request brauchen wir, um in der Session nachschauen zu koennen.
   * @return die Instanz der Bean.
   * @throws Exception
   */
  private Object getBean(Class c, HttpServletRequest request) throws Exception
  {
    String id = c.getName();
    
    Object bean = null;
    
    // 1. Checken, ob wir sie im Context-Scope haben
    bean = contextScope.get(id);
    if (bean != null)
      return bean;
    
    // 2. Checken, ob wir sie im Session-Scope haben
    HttpSession session = request.getSession();
    bean = session.getAttribute(id);
    if (bean != null)
      return bean;
    
    // 3. Bean erzeugen
    bean = c.newInstance();
    
    // Ggg in Context oder Session speichern
    Lifecycle lc = (Lifecycle) c.getAnnotation(Lifecycle.class);
    Lifecycle.Type type = lc != null ? lc.value() : Lifecycle.Type.REQUEST;
    
    switch(type)
    {
      case CONTEXT:
        contextScope.put(id,bean);
        break;
      case SESSION:
        session.setAttribute(id,bean);
        break;
    }
    
    return bean;
  }

  /**
   * @see de.willuhn.jameica.webadmin.rmi.RestService#register(java.lang.Object)
   */
  public void register(Object bean) throws RemoteException
  {
    if (!isStarted())
      throw new RemoteException("REST service not started");

    Hashtable<String,Method> found = eval(bean);
    if (found.size() > 0)
    {
      Logger.info("register REST commands for " + bean.getClass());
      this.commands.putAll(found);
      
      //////////////////////////////////////////////////////////////////////////
      // Dokumentation der REST-Bean
      RestBeanDoc bd = new RestBeanDoc();
      bd.setBeanClass(bean.getClass());
      Doc d = bean.getClass().getAnnotation(Doc.class);
      if (d != null)
        bd.setText(d.value());
      Enumeration<String> e = found.keys();
      List<RestMethodDoc> methods = new ArrayList<RestMethodDoc>();
      while (e.hasMoreElements())
      {
        String path = e.nextElement();
        Method m = found.get(path);
        RestMethodDoc md = new RestMethodDoc();
        md.setPath(path);
        md.setMethod(m.getName());
        d = m.getAnnotation(Doc.class);
        if (d != null)
        {
          md.setText(d.value());
          md.setExample(d.example());
        }
        methods.add(md);
      }
      bd.setMethods(methods);
      this.doc.add(bd);
      //
      //////////////////////////////////////////////////////////////////////////
      
    }
    else
    {
      Logger.warn(bean.getClass() + " contains no valid annotated methods, skip bean");
    }
  }

  /**
   * @see de.willuhn.jameica.webadmin.rmi.RestService#unregister(java.lang.Object)
   */
  public void unregister(Object bean) throws RemoteException
  {
    if (!isStarted())
    {
      Logger.info("REST service not started");
      return;
    }

    Hashtable<String,Method> found = eval(bean);
    if (found.size() > 0)
    {
      Logger.info("un-register REST commands for " + bean.getClass());
      Iterator<String> s = found.keySet().iterator();
      while (s.hasNext())
      {
        if (!this.isStarted())
        {
          Logger.info("REST service not started");
          return;
        }
        this.commands.remove(s.next());
      }
      for (RestBeanDoc bd:this.doc)
      {
        if (bd.getBeanClass().equals(bean.getClass()))
        {
          this.doc.remove(bd);
          break;
        }
      }
    }
    else
    {
      Logger.warn(bean.getClass() + " contains no valid annotated methods, skip bean");
    }
  }
  
  /**
   * Analysiert die Bean und deren Annotations und liefert sie zurueck.
   * @param bean die zu evaluierende Bean.
   * @return Hashtable mit den URLs und zugehoerigen Methoden.
   * @throws RemoteException
   */
  private Hashtable<String,Method> eval(Object bean) throws RemoteException
  {
    if (bean == null)
      throw new RemoteException("no REST bean given");

    Hashtable<String,Method> found = new Hashtable<String,Method>();
    Method[] methods = bean.getClass().getMethods();
    for (Method m:methods)
    {
      Path path = m.getAnnotation(Path.class);
      if (path == null)
        continue;

      String s = path.value();
      if (s == null || s.length() == 0)
      {
        Logger.warn("no path specified for method " + m + ", skipping");
        continue;
      }

      m.setAccessible(true);
      Logger.debug("REST command " + m + ", URL pattern: " + s);
      found.put(s,m);
    }
    return found;
  }

  /**
   * Injiziert die Annotations.
   * @param bean die Bean.
   * @throws Exception
   */
  private void applyAnnotations(Object bean, HttpServletRequest request, HttpServletResponse response) throws Exception
  {
    try
    {
      Inject.inject(bean,Request.class,request);
      Inject.inject(bean,Response.class,response);
    }
    catch (Exception e)
    {
      Logger.error("unable to inject context",e);
      throw new IOException("unable to inject context");
    }
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "REST-API Service";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return !this.isStarted();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return this.commands != null;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (isStarted())
    {
      Logger.warn("service allread started, skipping request");
      return;
    }
    
    Logger.info("init REST registry");
    this.commands     = new Hashtable<String,Method>();
    this.contextScope = new Hashtable<String,Object>();
    this.doc          = new ArrayList<RestBeanDoc>();
    
    Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.register").registerMessageConsumer(this.register);
    Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.unregister").registerMessageConsumer(this.unregister);

    // Fremdsysteme benachrichtigen, dass wir online sind.
    Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.start").sendMessage(new QueryMessage());
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if (!this.isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }
    
    try
    {
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.register").unRegisterMessageConsumer(this.register);
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.unregister").unRegisterMessageConsumer(this.unregister);
    }
    finally
    {
      Logger.info("REST service stopped");
      this.contextScope = null;
      this.commands     = null;
      this.doc          = null;
    }
  }


  
  /**
   * @see de.willuhn.jameica.webadmin.rmi.RestService#getDoc()
   */
  public List<RestBeanDoc> getDoc() throws RemoteException
  {
    return this.doc;
  }



  /**
   * Hilfsklasse zum Registrieren von REST-Kommandos via Messaging
   */
  private class RestConsumer implements MessageConsumer
  {
    private boolean r = false;
    
    /**
     * @param register true zum Registrieren, false zum De-Registrieren.
     */
    private RestConsumer(boolean register)
    {
      this.r = register;
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
      QueryMessage m = (QueryMessage) message;
      Object bean = m.getData();
      if (bean == null)
        return;
      
      if (r)
        register(bean);
      else
        unregister(bean);
    }
  }
}


/*********************************************************************
 * $Log: RestServiceImpl.java,v $
 * Revision 1.29  2011/06/28 09:56:25  willuhn
 * @N Lifecycle-Annotation aus jameica.webadmin in util verschoben
 *
 * Revision 1.28  2011-03-30 12:14:05  willuhn
 * @N Neuer Injector fuer DI
 *
 * Revision 1.27  2010/05/18 10:43:20  willuhn
 * @N Lesbarere Fehlermeldungen
 *
 * Revision 1.26  2010/05/11 23:21:44  willuhn
 * @N Automatische Dokumentations-Seite fuer die REST-Beans basierend auf der Annotation "Doc"
 *
 * Revision 1.25  2010/05/11 16:41:20  willuhn
 * @N Automatisches Indent bei JSON-Objekten
 *
 * Revision 1.24  2010/05/11 14:59:48  willuhn
 * @N Automatisches Deployment von REST-Beans
 *
 * Revision 1.23  2010/03/31 16:01:09  willuhn
 * @B Compile-Fehler unter JDK 1.5
 *
 * Revision 1.22  2010/03/19 16:04:13  willuhn
 * @N Exception durchreichen
 *
 * Revision 1.21  2010/03/19 15:56:17  willuhn
 * @N LifeCycle-Annotation-Support jetzt auch fuer REST-Beans
 *
 * Revision 1.20  2010/03/18 09:29:35  willuhn
 * @N Wenn REST-Beans Rueckgabe-Werte liefern, werrden sie automatisch als toString() in den Response-Writer geschrieben
 *
 * Revision 1.19  2010/02/10 13:43:48  willuhn
 * @N InvocationTargetException entpacken
 *
 * Revision 1.18  2009/12/08 16:46:14  willuhn
 * @B NPE
 *
 * Revision 1.17  2009/09/10 16:48:39  willuhn
 * @C Annotations via BeanUtils ermitteln
 *
 * Revision 1.16  2009/08/05 09:03:40  willuhn
 * @C Annotations in eigenes Package verschoben (sind nicht mehr REST-spezifisch)
 *
 * Revision 1.15  2009/01/06 01:44:14  willuhn
 * @N Code zum Hinzufuegen von Servern erweitert
 *
 * Revision 1.14  2008/11/07 00:14:37  willuhn
 * @N REST-Bean fuer Anzeige von System-Infos (Start-Zeit, Config)
 *
 * Revision 1.13  2008/11/06 23:29:15  willuhn
 * @B s/return/continue/
 *
 * Revision 1.12  2008/10/21 22:33:47  willuhn
 * @N Markieren der zu registrierenden REST-Kommandos via Annotation
 *
 * Revision 1.11  2008/10/08 21:38:23  willuhn
 * @C Nur noch zwei Annotations "Request" und "Response"
 *
 * Revision 1.10  2008/10/08 17:54:32  willuhn
 * @B message an der falschen Stelle geschickt
 *
 * Revision 1.9  2008/10/08 16:01:38  willuhn
 * @N REST-Services via Injection (mittels Annotation) mit Context-Daten befuellen
 *
 * Revision 1.8  2008/10/07 23:45:16  willuhn
 * @N Registrieren/Deregistrieren von REST-Commands via Messaging
 *
 * Revision 1.7  2008/09/09 14:40:09  willuhn
 * @D Hinweise zum Encoding. Siehe auch http://www.willuhn.de/blog/index.php?/archives/415-Umlaute-in-URLs-sind-Mist.html
 *
 * Revision 1.6  2008/07/11 15:38:55  willuhn
 * @N Service-Deployment
 *
 * Revision 1.5  2008/06/16 22:31:53  willuhn
 * @N weitere REST-Kommandos
 *
 * Revision 1.4  2008/06/16 14:22:11  willuhn
 * @N Mapping der REST-URLs via Property-Datei
 *
 * Revision 1.3  2008/06/15 22:48:24  willuhn
 * @N Command-Chains
 *
 * Revision 1.2  2008/06/13 15:11:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2008/06/13 14:11:04  willuhn
 * @N Mini REST-API
 *
 **********************************************************************/