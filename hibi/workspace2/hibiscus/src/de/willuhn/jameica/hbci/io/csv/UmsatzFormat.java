/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/csv/UmsatzFormat.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/03/16 13:43:56 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import java.util.Date;
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.ser.DateSerializer;
import de.willuhn.jameica.hbci.io.ser.DefaultSerializer;
import de.willuhn.jameica.hbci.io.ser.ExtendedUsageSerializer;
import de.willuhn.jameica.hbci.io.ser.Serializer;
import de.willuhn.jameica.hbci.io.ser.UmsatzTypSerializer;
import de.willuhn.jameica.hbci.io.ser.ValueSerializer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung des CSV-Formats fuer den Import von Kontoauszuegen.
 */
public class UmsatzFormat implements Format<Umsatz>
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private ImportListener listener        = null;
  private Profile profile                = null;
  
  /**
   * @see de.willuhn.jameica.hbci.io.csv.Format#getDefaultProfile()
   */
  public synchronized Profile getDefaultProfile()
  {
    if (this.profile == null)
    {
      this.profile = new Profile();
      this.profile.setSkipLines(1);
      this.profile.setVersion(0);
      
      Serializer ts = new DefaultSerializer();
      Serializer vs = new ValueSerializer();
      Serializer ds = new DateSerializer();
      
      List<Column> list = this.profile.getColumns();
      int i = 3; // wir fangen bei Spalte 4 an, weil die ersten 3 Spalten von Hibiscus
                 // zwar exportiert werden (Kontonummer, BLZ, Name des eigenen Kontos),
                 // diese Information beim Import aber nicht benoetigt wird (kriegen
                 // wir ueber den Kontext. Man koennte natuerlich auch bei Spalte
                 // 0 anfangen, wir wollen ja aber, dass wenigstens die von Hibiscus
                 // erzeugten CSV-Dateien 1:1 wieder importiert werden koennen, ohne
                 // dass der User das Profil anpassen muss.
      
      list.add(new Column("gegenkontoNummer",i18n.tr("Gegenkonto"),i++,ts));
      list.add(new Column("gegenkontoBLZ",   i18n.tr("Gegenkonto BLZ"),i++,ts));
      list.add(new Column("gegenkontoName",  i18n.tr("Gegenkonto Inhaber"),i++,ts));
      list.add(new Column("betrag",i18n.tr("Betrag"),i++,vs));
      list.add(new Column("valuta",i18n.tr("Valuta"),i++,ds));
      list.add(new Column("datum",i18n.tr("Datum"),i++,ds));
      list.add(new Column("zweck",i18n.tr("Verwendungszweck"),i++,ts));
      list.add(new Column("zweck2",i18n.tr("Verwendungszweck 2"),i++,ts));
      list.add(new Column("saldo",i18n.tr("Saldo"),i++,vs));
      list.add(new Column("primanota",i18n.tr("Primanota"),i++,ts));
      list.add(new Column("customerRef",i18n.tr("Kundenreferenz"),i++,ts));
      list.add(new Column("umsatzTyp",i18n.tr("Kategorie"),i++,new UmsatzTypSerializer()));
      list.add(new Column("kommentar",i18n.tr("Notiz"),i++,ts));
      list.add(new Column("weitereVerwendungszwecke",i18n.tr("Weitere Verwendungszwecke"),i++,new ExtendedUsageSerializer()));

      // wird von Hibiscus nicht mit im CSV exportiert, kann aber importiert werden
      list.add(new Column("art",i18n.tr("Art der Buchung"),i++,ts));
    
    }
    return this.profile;
  }

  /**
   * @see de.willuhn.jameica.hbci.io.csv.Format#getType()
   */
  public Class<Umsatz> getType()
  {
    return Umsatz.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.io.csv.Format#getImportListener()
   */
  public ImportListener getImportListener()
  {
    if (this.listener == null)
    {
      this.listener = new ImportListener(){
        
        /**
         * @see de.willuhn.jameica.hbci.io.csv.ImportListener#beforeStore(de.willuhn.jameica.hbci.io.csv.ImportEvent)
         */
        public void beforeStore(ImportEvent event)
        {
          try
          {
            Object data = event.data;
            if (data == null || !(data instanceof Umsatz))
              return;

            Umsatz u = (Umsatz) data;

            // Hibiscus verlangt, dass Valuta UND Buchungsdatum vorhanden sind.
            // Oft ist es aber so, dass nur eines der beiden Fehler in der CSV-Datei
            // existiert. Da beide Werte meistens ohnehin identisch sind, uebernehmen
            // wir den einen jeweils in den anderen, falls einer von beiden fehlt.
            Date dd = u.getDatum();
            Date dv = u.getValuta();
            if (dd == null) u.setDatum(dv);
            if (dv == null) u.setValuta(dd);
            

            // Wir fuegen hier noch das Konto ein, falls es angegeben ist
            Object context = event.context;
            if (context != null && (context instanceof Konto))
              u.setKonto((Konto)context);
          }
          catch (Exception e)
          {
            Logger.error("error while assigning account",e);
          }
        }
        
      };
    }
    return this.listener;
  }
}



/**********************************************************************
 * $Log: UmsatzFormat.java,v $
 * Revision 1.2  2010/03/16 13:43:56  willuhn
 * @N CSV-Import von Ueberweisungen und Lastschriften
 * @N Versionierbarkeit von serialisierten CSV-Profilen
 *
 * Revision 1.1  2010/03/16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 **********************************************************************/