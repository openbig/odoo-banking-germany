/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/BackupCreate.java,v $
 * $Revision: 1.7 $
 * $Date: 2012/01/25 21:49:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBObjectNode;
import de.willuhn.datasource.serialize.Writer;
import de.willuhn.datasource.serialize.XmlWriter;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.server.AuslandsUeberweisungImpl;
import de.willuhn.jameica.hbci.server.DBPropertyImpl;
import de.willuhn.jameica.hbci.server.DBReminderImpl;
import de.willuhn.jameica.hbci.server.DauerauftragImpl;
import de.willuhn.jameica.hbci.server.HibiscusAddressImpl;
import de.willuhn.jameica.hbci.server.KontoImpl;
import de.willuhn.jameica.hbci.server.LastschriftImpl;
import de.willuhn.jameica.hbci.server.NachrichtImpl;
import de.willuhn.jameica.hbci.server.ProtokollImpl;
import de.willuhn.jameica.hbci.server.SammelLastBuchungImpl;
import de.willuhn.jameica.hbci.server.SammelLastschriftImpl;
import de.willuhn.jameica.hbci.server.SammelUeberweisungBuchungImpl;
import de.willuhn.jameica.hbci.server.SammelUeberweisungImpl;
import de.willuhn.jameica.hbci.server.TurnusImpl;
import de.willuhn.jameica.hbci.server.UeberweisungImpl;
import de.willuhn.jameica.hbci.server.UmsatzImpl;
import de.willuhn.jameica.hbci.server.UmsatzTypImpl;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Action zum Erstellen eines Komplett-Backups im XML-Format.
 */
public class BackupCreate implements Action
{
  /**
   * Dateformat, welches fuer den Dateinamen genutzt wird.
   */
  public static DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    FileDialog fd = new FileDialog(GUI.getShell(),SWT.SAVE);
    fd.setFilterPath(System.getProperty("user.home"));
    fd.setOverwrite(true);
    fd.setFileName("hibiscus-backup-" + DATEFORMAT.format(new Date()) + ".xml");
    fd.setFilterExtensions(new String[]{"*.xml"});
    fd.setText("Bitte w�hlen Sie die Datei, in der das Backup gespeichert wird");
    String f = fd.open();
    if (f == null || f.length() == 0)
      return;
    
    final File file = new File(f);
    Application.getController().start(new BackgroundTask() {
      private boolean cancel = false;
    
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
       */
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        Writer writer = null;
        try
        {
          Logger.info("creating xml backup to " + file.getAbsolutePath());

          writer = new XmlWriter(new BufferedOutputStream(new FileOutputStream(file)));

          monitor.setStatusText(i18n.tr("Speichere Turnus-Informationen"));
          backup(TurnusImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          
          monitor.setStatusText(i18n.tr("Speichere Umsatz-Kategorien"));
          backupTree(UmsatzTypImpl.class,writer,monitor);
          monitor.addPercentComplete(5);


          monitor.setStatusText(i18n.tr("Speichere Adressbuch"));
          backup(HibiscusAddressImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Konten und Systemnachrichten"));
          backup(KontoImpl.class,writer,monitor);
          backup(NachrichtImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Ums�tze"));
          backup(UmsatzImpl.class,writer,monitor);
          monitor.addPercentComplete(20);
          
          monitor.setStatusText(i18n.tr("Speichere Dauerauftr�ge"));
          backup(DauerauftragImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Lastschriften"));
          backup(LastschriftImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere �berweisungen"));
          backup(UeberweisungImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere SEPA-�berweisungen"));
          backup(AuslandsUeberweisungImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Sammel-Lastschriften"));
          backup(SammelLastschriftImpl.class,writer,monitor);
          backup(SammelLastBuchungImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Sammel-�berweisungen"));
          backup(SammelUeberweisungImpl.class,writer,monitor);
          backup(SammelUeberweisungBuchungImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Properties"));
          backup(DBPropertyImpl.class,writer,monitor);
          monitor.addPercentComplete(10);

          monitor.setStatusText(i18n.tr("Speichere Reminder"));
          backup(DBReminderImpl.class,writer,monitor);
          monitor.addPercentComplete(2);

          // Die Protokolle zum Schluss.
          monitor.setStatusText(i18n.tr("Speichere Protokolle"));
          backup(ProtokollImpl.class,writer,monitor);
          monitor.addPercentComplete(20);
          
          // Die Versionstabelle wird nicht mit kopiert
          
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText("Backup erstellt");
          monitor.setPercentComplete(100);
        }
        catch (Exception e)
        {
          throw new ApplicationException(e.getMessage());
        }
        finally
        {
          if (writer != null)
          {
            try
            {
              writer.close();
              Logger.info("backup created");
            }
            catch (Exception e) {/*useless*/}
          }
        }
      }
    
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
       */
      public boolean isInterrupted()
      {
        return this.cancel;
      }
    
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
       */
      public void interrupt()
      {
        this.cancel = true;
      }
    
    });
  }
  
  /**
   * Hilfsfunktion.
   * @param type der Typ der zu speichernden Objekte.
   * @param writer der Writer.
   * @param monitor der Monitor.
   * @throws Exception
   */
  private static void backup(Class<? extends DBObject> type, Writer writer, ProgressMonitor monitor) throws Exception
  {
    DBIterator list = Settings.getDBService().createList(type);
    list.setOrder("order by id");
    long count = 1;
    while (list.hasNext())
    {
      GenericObject o = null;
      try
      {
        o = list.next();
        writer.write(o);
        if (count++ % 200 == 0)
          monitor.addPercentComplete(1);
      }
      catch (Exception e)
      {
        Logger.error("error while writing object " + BeanUtil.toString(o) + " - skipping",e);
        monitor.log("  " + i18n.tr("{0} fehlerhaft ({1}), �berspringe",new String[]{BeanUtil.toString(o),e.getMessage()}));
      }
    }
  }

  /**
   * Hilfsfunktion zum rekursiven Sichern von Baeumen.
   * Und zwar so, dass sie sich anschliessend korrekt wieder importieren lassen.
   * @param type der Knoten-Typ des Baumes.
   * @param writer der Writer.
   * @param monitor der Monitor.
   * @throws Exception
   */
  private static void backupTree(Class<? extends DBObjectNode> type, Writer writer, ProgressMonitor monitor) throws Exception
  {
    // Wir fangen auf der obersten Ebene an, anschliessen gehen wir in die Rekursion.
    DBIterator root = Settings.getDBService().createList(type);
    root.addFilter("parent_id is null");
    root.setOrder("order by id");

    while (root.hasNext())
    {
      GenericObjectNode o = null;
      try
      {
        o = (GenericObjectNode)root.next();
        backupNode(o,writer,monitor);
        monitor.addPercentComplete(1);
      }
      catch (Exception e)
      {
        Logger.error("error while writing object " + BeanUtil.toString(o) + " - skipping",e);
        monitor.log("  " + i18n.tr("{0} fehlerhaft ({1}), �berspringe",new String[]{BeanUtil.toString(o),e.getMessage()}));
      }
    }
  }
  
  /**
   * Sichert den Knoten und dessen Kinder.
   * @param der Knoten.
   * @param writer der Writer.
   * @param monitor der Monitor.
   * @throws Exception
   */
  private static void backupNode(GenericObjectNode node, Writer writer, ProgressMonitor monitor) throws Exception
  {
    try
    {
      // erst der Knoten selbst
      writer.write(node);
      
      // und jetzt rekursiv die Kinder
      GenericIterator children = node.getChildren();
      if (children != null)
      {
        while (children.hasNext())
        {
          backupNode((GenericObjectNode) children.next(),writer,monitor);
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("error while writing object " + BeanUtil.toString(node) + " - skipping",e);
      monitor.log("  " + i18n.tr("{0} fehlerhaft ({1}), �berspringe",new String[]{BeanUtil.toString(node),e.getMessage()}));
    }
  }
  

}


/*********************************************************************
 * $Log: BackupCreate.java,v $
 * Revision 1.7  2012/01/25 21:49:26  willuhn
 * @B Die Reminder fehlten im Backup
 *
 * Revision 1.6  2010-07-21 16:11:13  willuhn
 * @C Diagnose-Backup aktualisiert (SEPA-Ueberweisungen und DBProperties fehlten, Umsatz-Kategorien muessen in der richtigen Reihenfolge exportiert werden)
 *
 * Revision 1.5  2010/06/08 11:27:59  willuhn
 * @N SWT besitzt jetzt selbst eine Option im FileDialog, mit der geprueft werden kann, ob die Datei ueberschrieben werden soll oder nicht
 *
 * Revision 1.4  2010/03/03 11:00:19  willuhn
 * @N Erst Status-Code setzen und dann erst den Text - sonst wird der Text nicht gruen gefaerbt
 *
 * Revision 1.3  2008/12/14 23:18:35  willuhn
 * @N BUGZILLA 188 - REFACTORING
 *
 * Revision 1.2  2008/09/02 18:14:25  willuhn
 * @N Diagnose-Backup erweitert
 *
 * Revision 1.1  2008/01/22 13:34:45  willuhn
 * @N Neuer XML-Import/-Export
 *
 **********************************************************************/