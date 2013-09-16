/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/Rule.java,v $
 * $Revision: 1.8 $
 * $Date: 2012/03/28 22:28:18 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.notify.notifier.Notifier;
import de.willuhn.jameica.sensors.notify.operator.Operator;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.XPathEmu;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;

/**
 * Implementierung einer einzelnen Benachrichtigungsregel.
 */
public class Rule
{
  private boolean enabled           = true;
  private String sensor             = null;
  private String limit              = null;
  private Operator operator         = null;
  private Notifier notifier         = null;
  private Map<String,String> params = new HashMap<String,String>();
  
  /**
   * ct.
   */
  public Rule()
  {
    
  }
  
  /**
   * ct.
   * Liest die Regel-Einstellungen aus dem XML-Element.
   * @param node
   * @throws Exception 
   */
  public Rule(IXMLElement node) throws Exception
  {
    ////////////////////////////////////////////////////////////////////////////
    // enabled
    {
      IXMLElement i = node.getFirstChildNamed("enabled");
      if (i != null)
      {
        String s = i.getContent();
        if (s != null)
          this.enabled = s.trim().toLowerCase().matches("true|1|yes");
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Sensor
    {
      IXMLElement i = node.getFirstChildNamed("sensor");
      if (i != null)
      {
        this.sensor = i.getContent();
        if (this.sensor != null)
          this.sensor = this.sensor.trim();
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Limit
    {
      IXMLElement i = node.getFirstChildNamed("limit");
      if (i != null)
      {
        this.limit = i.getContent();
        if (this.limit != null)
          this.limit = this.limit.trim();
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Operator
    {
      IXMLElement i = node.getFirstChildNamed("operator");
      if (i != null)
      {
        String s = i.getContent();
        if (s != null)
        this.operator = (Operator) load(s.trim());
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Notifier
    {
      IXMLElement i = node.getFirstChildNamed("notifier");
      if (i != null)
      {
        String s = i.getContent();
        if (s != null)
        this.notifier = (Notifier) load(s.trim());
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Params
    {
      XPathEmu xpath = new XPathEmu(node);
      IXMLElement[] list = xpath.getElements("params/param");
      for (IXMLElement e:list)
      {
        String name = e.getAttribute("name",null);
        String value = e.getAttribute("value",null);
        if (name != null && value != null)
          this.params.put(name.trim(),value.trim());
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////////
  
  }
  
  /**
   * Liefert true, wenn die Regel angewendet werden soll.
   * @return true, wenn die Regel angewendet werden soll.
   */
  public boolean isEnabled()
  {
    return this.enabled;
  }
  
  /**
   * Legt fest, ob die Regel angewendet werden soll.
   * @param b true, wenn die Regel angewendet werden soll.
   */
  public void setEnabled(boolean b)
  {
    this.enabled = b;
  }
  
  /**
   * Liefert die UUID des zu ueberwachenden Sensors.
   * @return UUID des Sensors.
   */
  public String getSensor()
  {
    return this.sensor;
  }
  
  /**
   * Speichert die UUID des Sensors.
   * @param uuid UUID des Sensors.
   */
  public void setSensor(String uuid)
  {
    this.sensor = uuid;
  }
  
  /**
   * Liefert den Grenzwert.
   * @return der Grenzwert.
   */
  public String getLimit()
  {
    return this.limit;
  }
  
  /**
   * Speichert den Grenzwert.
   * @param limit der Grenzwert.
   */
  public void setLimit(String limit)
  {
    this.limit = limit;
  }
  
  /**
   * Liefert den Operator, der entscheidet, ob der Grenzwert ueberschritten ist.
   * @return der Operator.
   */
  public Operator getOperator()
  {
    return this.operator;
  }
  
  /**
   * Speichert den Operator, der entscheidet, ob der Grenzwert ueberschritten ist.
   * @param o der Operator.
   */
  public void setOperator(Operator o)
  {
    this.operator = o;
  }
  
  /**
   * Liefert den Notifier, der die Benachrichtigung absendet.
   * @return der Notifier.
   */
  public Notifier getNotifier()
  {
    return this.notifier;
  }
  
  /**
   * Speichert den Notifier, der die Benachrichtigung absendet.
   * @param n der Notifier.
   */
  public void setNotifier(Notifier n)
  {
    this.notifier = n;
  }
  
  /**
   * Liefert die optionalen Regel-Parameter.
   * @return optionale Regel-Parameter.
   */
  public Map<String,String> getParams()
  {
    return this.params;
  }
  
  /**
   * Erzeugt einen Identifier fuer die Regel.
   * @return ein Identifier.
   * @throws Exception
   */
  String getID() throws Exception
  {
    StringBuffer sb = new StringBuffer();

    sb.append(this.getSensor());
    sb.append(".");

    Operator o = this.getOperator();
    if (o != null)
    {
      sb.append(o.getClass().getName());
      sb.append(".");
    }

    sb.append(this.getLimit());
    
    return sb.toString();
  }

  /**
   * Liefert ein XML-Fragment mit der Regel.
   * @return XML-Fragment mit der Regel.
   */
  public IXMLElement toXml()
  {
    IXMLElement root = new XMLElement("rule");
    
    {
      IXMLElement e = new XMLElement("enabled");
      e.setContent(Boolean.toString(this.enabled));
      root.addChild(e);
    }

    if (this.sensor != null)
    {
      IXMLElement e = new XMLElement("sensor");
      e.setContent(this.sensor);
      root.addChild(e);
    }

    if (this.limit != null)
    {
      IXMLElement e = new XMLElement("limit");
      e.setContent(this.limit);
      root.addChild(e);
    }
    
    if (this.operator != null)
    {
      IXMLElement e = new XMLElement("operator");
      e.setContent(this.operator.getClass().getName());
      root.addChild(e);
    }

    if (this.notifier != null)
    {
      IXMLElement e = new XMLElement("notifier");
      e.setContent(this.notifier.getClass().getName());
      root.addChild(e);
    }
    
    if (this.params.size() > 0)
    {
      IXMLElement e = new XMLElement("params");
      Iterator<String> i = this.params.keySet().iterator();
      while (i.hasNext())
      {
        String name = i.next();
        String value = this.params.get(name);
        if (name == null || value == null || name.length() == 0 || value.length() == 0)
          continue;
        IXMLElement param = new XMLElement("param");
        param.setAttribute("name",name);
        param.setAttribute("value",value);
        e.addChild(param);
      }
      root.addChild(e);
    }
    return root;
  }

  /**
   * Laedt und instanziiert die angegebene Klasse.
   * @param classname Name der Klasse.
   * @return Instanz.
   * @throws Exception
   */
  private Object load(String classname) throws Exception
  {
    try
    {
      ClassLoader l = Application.getPluginLoader().getManifest(Plugin.class).getClassLoader();
      Class c = l.loadClass(classname);
      
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      return service.get(c);
    }
    catch (Exception e)
    {
      throw e;
    }
    catch (Throwable t)
    {
      // u.a. fuer NoClassDefFoundError
      throw new Exception("unable to load class " + classname,t);
    }
  }
}



/**********************************************************************
 * $Log: Rule.java,v $
 * Revision 1.8  2012/03/28 22:28:18  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.7  2011-09-13 09:08:34  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.6  2010-08-31 11:00:59  willuhn
 * @D javadoc
 *
 * Revision 1.5  2010/03/23 18:35:45  willuhn
 * @N Rule serialisierbar
 *
 * Revision 1.4  2010/03/02 00:28:41  willuhn
 * @B bugfixing
 *
 * Revision 1.3  2010/03/01 23:51:07  willuhn
 * @N Benachrichtigung, wenn Sensor zurueck im normalen Bereich ist
 * @N Merken des letzten Notify-Status, sodass nur beim ersten mal eine Mail gesendet wird
 *
 * Revision 1.2  2010/03/01 18:12:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/