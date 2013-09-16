/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/PluginListPart.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/06/02 13:02:26 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.internal.action.PluginInstall;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.PluginCacheMessageConsumer;
import de.willuhn.jameica.messaging.PluginMessage;
import de.willuhn.jameica.messaging.PluginMessage.Event;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Komponente, die die Plugins in einer huebsch formatierten Liste mit
 * Buttons zum Managen anzeigt.
 */
public class PluginListPart implements Part
{
  private MessageConsumer mc = new MyMessageConsumer();
  private Map<String,PluginDetailPart> parts = new HashMap<String,PluginDetailPart>();
  
  private ScrolledContainer scrolled = null;

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite comp) throws RemoteException
  {
    I18N i18n = Application.getI18n();

    this.scrolled = new ScrolledContainer(comp,1);

    // In dem Cache stehen jetzt auch noch die frisch installierten drin, die
    // aber noch nicht aktiv sind
    Map<String,Manifest> cache = PluginCacheMessageConsumer.getCache();
    Iterator<String> it = cache.keySet().iterator();
    while (it.hasNext())
    {
      String name = it.next();
      Manifest mf = cache.get(name);
      PluginDetailPart part = new PluginDetailPart(mf);
      parts.put(name,part);
      part.paint(scrolled.getComposite());
    }
    
    // Groesse berechnen
    this.scrolled.update();
    
    Container container = new SimpleContainer(comp);
    container.addText(i18n.tr("Nur Plugins im Benutzer-Ordner (oder im System-Ordner, wenn er beschreibbar ist) k�nnen aktualisiert oder deinstalliert werden.\n" +
    		                      "Bitte starten Sie Jameica nach der Installation bzw. Deinstallation eines Plugins neu."),true,Color.COMMENT);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(new Button(i18n.tr("Neues Plugin installieren..."),new PluginInstall(),null,false,"emblem-package.png"));
    buttons.paint(comp);


    Application.getMessagingFactory().registerMessageConsumer(this.mc);
    comp.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
    
  }

  /**
   * Wird benachrichtigt, wenn ein Plugin deinstalliert wurde und loescht es aus
   * der Liste
   */
  private class MyMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{PluginMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      final PluginMessage m = (PluginMessage) message;
      final Manifest mf = m.getManifest();
      
      if (mf == null)
        return;

      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          try
          {
            // Das Manifest erstmal entfernen
            Iterator<String> i = parts.keySet().iterator();
            while (i.hasNext())
            {
              String name = i.next();
              if (mf.getName().equals(name))
              {
                PluginDetailPart part = parts.get(name);
                part.dispose();
              }
            }

            Event event = m.getEvent();

            // Neu einfuegen. Aber nur, wenn es installiert/aktualisiert wurde
            if (event == Event.INSTALLED || event == Event.UPDATED)
            {
              PluginDetailPart part = new PluginDetailPart(mf);
              parts.put(mf.getName(),part);
              part.paint(scrolled.getComposite());
            }

            // Layout aktualisieren
            scrolled.update();
          }
          catch (Exception e)
          {
            Logger.error("unable to update plugin list",e);
          }
        }
      });
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
    
  }
}



/**********************************************************************
 * $Log: PluginListPart.java,v $
 * Revision 1.2  2011/06/02 13:02:26  willuhn
 * @N MessageConsumer fuer die Plugin-Install-Benachrichtigungen ausgelagert, damit die Messages auch von jameica.update empfangen werden koennen
 *
 * Revision 1.1  2011-06-02 12:15:16  willuhn
 * @B Das Handling beim Update war noch nicht sauber
 *
 * Revision 1.3  2011-06-01 21:20:02  willuhn
 * @N Beim Deinstallieren die Navi und Menupunkte des Plugins deaktivieren
 * @N Frisch installierte aber noch nicht aktive Plugins auch dann anzeigen, wenn die View verlassen wird
 *
 * Revision 1.2  2011-06-01 17:52:03  willuhn
 * @C Weisser Hintergrund: Der des Parent funktionierte unter Windows nicht richtig. Das gab haessliche graue Flaechen.
 *
 * Revision 1.1  2011-06-01 17:35:58  willuhn
 * @N Ergonomischere Verwaltung der Plugins
 *
 **********************************************************************/