/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webtools/FrontController.java,v $
 * $Revision: 1.5 $
 * $Date: 2012/03/29 21:11:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webtools;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.services.VelocityService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.XPathEmu;
import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;

/**
 * Der Frontcontroller.
 */
public class FrontController extends HttpServlet
{
  private String plugin              = null;
  private MultipleClassLoader loader = null;
  private List<PageConfig> pages     = new ArrayList<PageConfig>();
  private Map<String,String> beans   = new HashMap<String,String>();
  
  /**
   * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);

    ////////////////////////////////////////////////////////////////////////////
    // Wir holen uns den Namen des Plugins. Ist eines angegeben,
    // nehmen wir dessen Classloader.
    this.plugin = config.getInitParameter("plugin");
    if (plugin != null && plugin.length() > 0)
    {
      Logger.info("trying to determine classloader for plugin " + plugin);
      try
      {
        Manifest mf = Application.getPluginLoader().getManifest(plugin);
        Logger.info("found plugin (" + mf.getName() + " " + mf.getVersion() + ") - using its classloader");
        this.loader = mf.getClassLoader();
      }
      catch (Exception e)
      {
        Logger.warn("unable to load plugin, using global classloader: " + e.getMessage());
      }
    }
    else
    {
      this.plugin = null; // sicherstellen, dass es NULL ist
    }
    
    if (this.loader == null)
      this.loader = Application.getClassLoader();
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Config laden
    ////////////////////////////////////////////////////////////////////////////
    // Wir holen uns die webtools-config
    String s = config.getInitParameter("config");
    if (s == null || s.length() == 0)
      s = "/WEB-INF/webtools.xml";
    //
    ////////////////////////////////////////////////////////////////////////////

    Logger.info("trying to load webtools config: " + s);
    InputStream is = config.getServletContext().getResourceAsStream(s);
    if (is == null)
      throw new ServletException("config " + s + " not found in servlet context");
    else
      Logger.info("loaded " + config.getServletContext().getRealPath(s));
    
    try
    {
      IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
      parser.setReader(new StdXMLReader(is));
      XPathEmu xpath = new XPathEmu((IXMLElement) parser.parse());

      //////////////////////////////////////////////////////////////////////////
      // Pages laden
      IXMLElement[] list = xpath.getElements("pages/page");
      if (list == null || list.length == 0)
        throw new ServletException("webtools config " + s + " contains no page definitions");
      
      for (IXMLElement child:list)
      {
        try
        {
          this.pages.add(new PageConfig(child));
        }
        catch (Exception e)
        {
          Logger.error("error while loading page config: " + e.getMessage() + ", skipping page",e);
        }
      }
      Logger.info("found " + this.pages.size() + " page definitions");
      //////////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////////
      // Beans laden
      list = xpath.getElements("beans/bean");
      if (list != null && list.length > 0)
      {
        for (IXMLElement child:list)
        {
          try
          {
            String name      = child.getAttribute("name",null);
            String className = child.getAttribute("class",null);
            if (name == null)
              throw new Exception("bean has no name attribute");
            if (className == null)
              throw new Exception("bean has no class attribute");
            this.beans.put(name,className);
          }
          catch (Exception e)
          {
            Logger.error("error while loading bean: " + e.getMessage() + ", skipping bean",e);
          }
        }
      }
    }
    catch (ServletException se)
    {
      throw se;
    }
    catch (Exception e)
    {
      throw new ServletException("unable to load webtools config " + s,e);
    }
    //
    ////////////////////////////////////////////////////////////////////////////
  }
  

  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String path = request.getPathInfo();
    if (path == null)
      path = request.getServletPath();

    Logger.debug("looking for page definition for: " + path);
    if (path == null)
      path = "";
    
    for (PageConfig pc:this.pages)
    {
      if (path.matches(pc.getPattern()))
      {
        try
        {
          // Template mit Context mergen und an Browser liefern
          VelocityService service = (VelocityService) Application.getBootLoader().getBootable(VelocityService.class);
          VelocityEngine engine   = service.getEngine(this.plugin);
          RequestConfig cfg       = new RequestConfig(this.loader,request,response);

          // Velocity-Context erzeugen
          VelocityContext ctx = new VelocityContext();
          ctx.put("request",  request);
          ctx.put("response", response);
          
          // Globale Beans
          Iterator<String> i = this.beans.keySet().iterator();
          while (i.hasNext())
          {
            String name = i.next();
            Object bean = BeanHandler.getBean(cfg,this.beans.get(name));
            if (bean != null)
              ctx.put(name,bean);
          }
          

          // Controller ausfuehren
          Object controller = BeanHandler.getBean(cfg,pc.getController());
          if (controller != null)
          {
            // Es kann sein, dass ein Fehler geflogen ist und der User
            // auf eine Custom-Fehlerseite weitergeleitet wurde. Wenn
            // diese Fehlerseite ebenfalls eine Webtools-Page samt Controller
            // ist, wird der gesamte Request incl. der Action an diesen
            // Controller delegiert. Dieser hat die fehlerausloesende Action
            // aber natuerlich nicht. Daher markieren wir den Request unten
            // als fehlerhaft. Sollte anschliessend eine Anfrage hier rein
            // kommen, die diese Markierung traegt, dann handelt es sich
            // um eine Custom-Fehlerseite und wir fuehren die Action hier
            // nicht nochmal aus.
            if (request.getAttribute("__jameica.webtools.error") == null)
            {
              // Request-Properties in Controller uebernehmen
              BeanHandler.applyRequest(controller,cfg.getRequest());

              // Wenn die Page eine Default-Action hat, rufen wir sie immer auf
              String action = pc.getAction();
              if (action != null && action.length() > 0)
                BeanUtil.invoke(controller,action,null);
              
              // Wenn der Request auch eine Action hat, dann die ebenfalls
              action = request.getParameter("action");
              if (action != null && action.length() > 0)
                BeanUtil.invoke(controller,action,null);
            }

            // Controller dem Context bekannt machen
            ctx.put("c",controller);
          }

          // Der Controller hat das Reponse bereits erledigt. Dann brauchen wir
          // das Template nicht mehr.
          if (response.isCommitted())
            return;
          
          // Template ausfuehren
          Template template = engine.getTemplate(pc.getTemplate()); // TODO: Sollte hier ein Encoding angegeben sein?
          template.merge(ctx,response.getWriter());
        }
        catch (Exception e)
        {
          request.setAttribute("__jameica.webtools.error",e);
          throw new ServletException(e);
        }
        // done
        return;
      }
    }

    // Wenn wir hier angekommen sind, wurde keine Page gefunden
    Logger.warn("no page config found for path " + path);
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }
}


/**********************************************************************
 * $Log: FrontController.java,v $
 * Revision 1.5  2012/03/29 21:11:30  willuhn
 * @C Kompatibilitaet zu Jameica 2.2 leider doch nicht moeglich
 *
 * Revision 1.4  2012/03/29 20:54:40  willuhn
 * @C Kompatibilitaet zu Jameica 2.2 wieder hergestellt
 *
 * Revision 1.3  2012/03/28 22:28:21  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.2  2011-01-27 16:26:54  willuhn
 * @N Importieren und Loeschen von SSL-Zertifikaten
 *
 * Revision 1.1  2010-10-27 14:32:18  willuhn
 * @R jameica.webtools ist jetzt Bestandteil von jameica.webadmin. Das separate webtools-Plugin ist nicht mehr noetig
 *
 * Revision 1.15  2010/03/04 13:20:43  willuhn
 * @C Request-Properties nicht auf globale Beans anwenden
 *
 * Revision 1.14  2010/02/18 17:09:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2010/02/17 23:04:39  willuhn
 * @N Default-Action immer ausfuehren
 *
 * Revision 1.12  2010/02/01 15:13:36  willuhn
 * @N Neues Element "beans" fuer globale Beans in webtools.xml
 * @N Default-Action via Attribut "action"
 *
 * Revision 1.11  2010/01/21 10:05:18  willuhn
 * @N webtools um Default-Action erweitert
 *
 * Revision 1.10  2009/08/28 15:02:58  willuhn
 * @N webtool.xml nach webapps/$context/WEB-INF verschoben - dort kann sie ohne Namenskonflikte mit anderen "webtools.xml" geladen werden
 *
 * Revision 1.9  2009/08/24 12:05:33  willuhn
 * @N Umstellung auf neuen VelocityService - damit funktioniert das Plugin jetzt nur noch mit Jameica 1.9
 *
 * Revision 1.8  2009/08/24 10:49:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2009/08/19 21:45:12  willuhn
 * @N Fehlerhandling
 *
 * Revision 1.6  2009/08/19 15:05:52  willuhn
 * @C Exception beim Ausfuehren der Action an den Servlet-Container weiterreichen, damit er sich um die Fehlerbehandlung kuemmern kann. Damit kann in web.xml auch eine eigene Fehlerseite definiert werden.
 *
 * Revision 1.5  2009/08/07 12:14:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2009/08/05 11:04:00  willuhn
 * @C Code cleanup
 * @N Webtools kennt jetzt die Lifecycle-Annotation
 *
 * Revision 1.3  2009/08/05 09:03:30  willuhn
 * @C Annotations in eigenes Package verschoben (sind nicht mehr REST-spezifisch)
 *
 * Revision 1.2  2009/08/03 23:44:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/08/03 23:17:18  willuhn
 * @N spartanisches Mini-Web-MVC-Framework mittels Frontcontroller und Velocity
 *
 **********************************************************************/
