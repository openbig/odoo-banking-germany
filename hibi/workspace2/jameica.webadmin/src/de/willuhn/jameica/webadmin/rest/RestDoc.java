/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/rest/RestDoc.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/11/02 00:56:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.annotation.Doc;
import de.willuhn.jameica.webadmin.annotation.Path;
import de.willuhn.jameica.webadmin.beans.RestBeanDoc;
import de.willuhn.jameica.webadmin.beans.RestMethodDoc;
import de.willuhn.jameica.webadmin.rmi.RestService;
import de.willuhn.logging.Logger;

/**
 * REST-Bean fuer den Zugriff auf die Doku der REST-Beans.
 */
@Doc("System: Bietet Zugriff auf die Dokumentation der REST-Services.")
public class RestDoc implements AutoRestBean
{
  /**
   * Liefert die Liste der REST-Services.
   * @return Liste der REST-Services.
   * @throws IOException
   */
  @Doc(value="Liefert die Liste der REST-Services",
       example="list")
  @Path("/list$")
  public JSONArray getList() throws IOException
  {
    try
    {
      List<Map> list = new ArrayList<Map>();

      RestService service = (RestService) Application.getServiceFactory().lookup(de.willuhn.jameica.webadmin.Plugin.class,"rest");
      List<RestBeanDoc> beans = service.getDoc();

      if (beans != null)
      {
        for (RestBeanDoc b:beans)
        {
          Map data = new HashMap();
          data.put("name",        StringUtils.trimToEmpty(b.getBeanClass().getSimpleName()));
          data.put("description", StringUtils.trimToEmpty(b.getText()));
          
          List<RestMethodDoc> methods = b.getMethods();
          List<Map> list2 = new ArrayList<Map>();
          for (RestMethodDoc m:methods)
          {
            Map data2 = new HashMap();
            data2.put("path",StringUtils.trimToEmpty(m.getPath()));
            data2.put("method",StringUtils.trimToEmpty(m.getMethod()));
            data2.put("description",StringUtils.trimToEmpty(m.getText()));
            data2.put("example",StringUtils.trimToEmpty(m.getExample()));
            list2.add(data2);
          }
          data.put("methods",list2);
          list.add(data);
        }
      }
      return new JSONArray(list);
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      Logger.error("unable to get rest doc",e2);
      throw new IOException("unable to get rest doc");
    }
  }
}


/*********************************************************************
 * $Log: RestDoc.java,v $
 * Revision 1.1  2010/11/02 00:56:31  willuhn
 * @N Umstellung des Webfrontends auf Velocity/Webtools
 *
 **********************************************************************/