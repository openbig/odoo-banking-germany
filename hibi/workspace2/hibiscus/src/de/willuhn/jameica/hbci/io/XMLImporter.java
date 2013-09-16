/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/XMLImporter.java,v $
 * $Revision: 1.8 $
 * $Date: 2012/03/28 22:47:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.serialize.ObjectFactory;
import de.willuhn.datasource.serialize.Reader;
import de.willuhn.datasource.serialize.XmlReader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Importer fuer das Hibiscus-eigene XML-Format.
 */
public class XMLImporter implements Importer
{

  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {

    if (is == null)
      throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgew�hlt"));
    
    if (format == null)
      throw new ApplicationException(i18n.tr("Kein Datei-Format ausgew�hlt"));

    final ClassLoader loader = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader();
    Reader reader = null;
    try
    {
      reader = new XmlReader(is, new ObjectFactory() {
        public GenericObject create(String type, String id, Map values) throws Exception
        {
          AbstractDBObject object = (AbstractDBObject) Settings.getDBService().createObject(loader.loadClass(type),null);
          // object.setID(id); // Keine ID angeben, da wir die Daten neu anlegen wollen
          Iterator i = values.keySet().iterator();
          while (i.hasNext())
          {
            String name = (String) i.next();
            object.setAttribute(name,values.get(name));
          }
          return object;
        }
      
      });
      
      if (monitor != null)
        monitor.setStatusText(i18n.tr("Lese Datei ein"));

      int created = 0;
      int error   = 0;

      DBObject object = null;
      while ((object = (DBObject) reader.read()) != null)
      {
        if (monitor != null)
        {
          monitor.log(i18n.tr("Datensatz {0}", "" + (created+1)));
          if (created > 0 && created % 10 == 0) // nur geschaetzt
            monitor.addPercentComplete(1);
        }

        try
        {
          object.store();
          created++;
          try
          {
            Application.getMessagingFactory().sendMessage(new ImportMessage(object));
          }
          catch (Exception ex)
          {
            Logger.error("error while sending import message",ex);
          }
        }
        catch (ApplicationException ae)
        {
          monitor.log("  " + ae.getMessage());
          error++;
        }
        catch (Exception e)
        {
          Logger.error("unable to import line",e);
          monitor.log("  " + i18n.tr("Fehler beim Import des Datensatzes: {0}",e.getMessage()));
          error++;
        }
      }
      monitor.setStatusText(i18n.tr("{0} Datens�tze erfolgreich importiert, {1} fehlerhafte �bersprungen", new String[]{""+created,""+error}));
      monitor.setPercentComplete(100);
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled");
      throw new ApplicationException(i18n.tr("Import abgebrochen"));
    }
    catch (Exception e)
    {
      Logger.error("error while reading file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Import der XML-Datei"));
    }
    finally
    {
      if (reader != null)
      {
        try
        {
          reader.close();
        }
        catch (IOException e)
        {
          Logger.error("error while closing inputstream",e);
        }
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("Hibiscus-Format");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!GenericObject.class.isAssignableFrom(objectType))
      return null; // Import fuer alles moeglich, was von GenericObject abgeleitet ist
    
    // BUGZILLA 700
    if (SammelTransfer.class.isAssignableFrom(objectType))
      return null; // Keine Sammel-Auftraege - die muessen gesondert behandelt werden.

    // BUGZILLA 1149
    if (Umsatz.class.isAssignableFrom(objectType))
      return null; // Keine Umsaetze - die muessen gesondert behandelt werden.

    if (UmsatzTyp.class.isAssignableFrom(objectType))
      return null; // Keine Kategorien - die muessen gesondert behandelt werden.

    IOFormat f = new IOFormat() {
      public String getName()
      {
        return XMLImporter.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[] {"*.xml"};
      }
    };
    return new IOFormat[] { f };
  }
}

/*******************************************************************************
 * $Log: XMLImporter.java,v $
 * Revision 1.8  2012/03/28 22:47:18  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.7  2011/12/04 22:06:55  willuhn
 * @N BUGZILLA 1149 - Umsaetze beim XML-Import einem beliebigen Konto zuordenbar
 *
 * Revision 1.6  2010/06/01 21:57:31  willuhn
 * @N "XML-Format" in "Hibiscus-Format" umbenannt - das "XML" verwirrte User und brachte sie zu der Annahme, man koenne da beliebige XML-Dateien importieren ;)
 * @R binaeres "Hibiscus-Format" (via ObjectInputStream/ObjectOutputStream) entfernt - war ohnehin schon seit Jahren deaktiviert und obsolet - das XML-Format kann das besser
 *
 * Revision 1.5  2010/04/16 12:20:51  willuhn
 * @B Parent-ID beim Import von Kategorien beruecksichtigen und neu mappen
 *
 * Revision 1.4  2009/10/05 17:12:03  willuhn
 * @B Import-Message wurde doppelt verschickt
 *
 * Revision 1.3  2009/02/13 14:17:01  willuhn
 * @N BUGZILLA 700
 *
 * Revision 1.2  2008/02/13 23:44:27  willuhn
 * @R Hibiscus-Eigenformat (binaer-serialisierte Objekte) bei Export und Import abgeklemmt
 * @N Import und Export von Umsatz-Kategorien im XML-Format
 * @B Verzaehler bei XML-Import
 *
 * Revision 1.1  2008/01/22 13:34:45  willuhn
 * @N Neuer XML-Import/-Export
 *
 ******************************************************************************/