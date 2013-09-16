/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/MT940UmsatzExporter.java,v $
 * $Revision: 1.13 $
 * $Date: 2012/03/06 21:44:26 $
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.ext.ExportSaldoExtension;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Exportiert Umsaetze im MT940-Format.
 */
public class MT940UmsatzExporter implements Exporter
{
  /**
   * MT940-Zeichensatz.
   * Ist eigentlich nicht noetig, weil Swift nur ein Subset von ISO-8859
   * zulaesst, welches so klein ist, dass es im Wesentlichen US-ASCII ist
   * und damit der Zeichensatz so ziemlich egal ist. Aber wir tolerieren
   * die Umlaute wenigstens beim Import.
   */
  public final static String CHARSET  = "iso-8859-1";

  private final static String NL      = "\r\n";
  
  private final static DateFormat DF_YYMMDD = new SimpleDateFormat("yyMMdd");
  private final static DateFormat DF_MMDD   = new SimpleDateFormat("MMdd");
  
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(Object[] objects, IOFormat format,OutputStream os, final ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    // BUGZILLA 1250
    DecimalFormat df = (DecimalFormat) HBCI.DECIMALFORMAT.clone();
    df.setGroupingUsed(false);
    
    OutputStreamWriter out = null;
    
    try
    {
      out = new MyOutputStreamWriter(os);

      double factor = 1;
      if (monitor != null)
      {
        factor = ((double)(100 - monitor.getPercentComplete())) / objects.length;
        monitor.setStatusText(i18n.tr("Exportiere Daten"));
      }

      Boolean b         = (Boolean) Exporter.SESSION.get(ExportSaldoExtension.KEY_SALDO_HIDE);
      boolean showSaldo = (b == null || !b.booleanValue());
      
      for (int i=0;i<objects.length;++i)
      {
        if (monitor != null)  
        	monitor.setPercentComplete((int)((i) * factor));
        
        if (objects[i] == null || !(objects[i] instanceof Umsatz))
          continue;

        Object name = BeanUtil.toString(objects[i]);
        
        if (name != null && monitor != null)
          monitor.log(i18n.tr("Speichere Datensatz {0}",name.toString()));
        
    		Umsatz u    = (Umsatz) objects[i];
    		Konto k     = u.getKonto();
    		String curr = k.getWaehrung();

        out.write(NL + ":20:Hibiscus" + NL);
    		out.write(":25:" + k.getBLZ() + "/" + k.getKontonummer() + curr + NL);
    		
    		if (showSaldo)
    		{
          //(Schlusssaldo - Umsatzbetrag) > 0 -> Soll-Haben-Kennung f�r den Anfangssaldo = C
          //(Credit), sonst D (Debit)
          double anfangsSaldo = u.getSaldo() - u.getBetrag();
          
          //Anfangssaldo aus dem Schlusssaldo ermitteln sowie Soll-Haben-Kennung
          //Valuta Datum des Kontosaldos leider nicht verf�gbar, deswegen wird Datum der Umsatzwertstellung genommen
          out.write(":60F:");
          out.write(anfangsSaldo >= 0.0d ? "C" : "D");
          out.write(DF_YYMMDD.format(u.getDatum()) + curr + df.format(anfangsSaldo).replace("-","") + NL);
    		}

        out.write(":61:" + DF_YYMMDD.format(u.getValuta()) + DF_MMDD.format(u.getDatum()));

        // Soll-Haben-Kennung f�r den Betrag ermitteln
    		double betrag = u.getBetrag();
        out.write(betrag >= 0.0d ? "CR" : "DR");
        out.write(df.format(betrag).replace("-",""));
    		
        String ref = StringUtils.trimToNull(u.getCustomerRef());
    		out.write("NTRF" + (ref != null ? ref : "NONREF") + NL);

    		String gvcode = u.getGvCode();
    		
      	// Fallback, wenn wir keinen GV-Code haben. Das trifft u.a. bei Alt-Umsaetzen
    		// auf, als Hibiscus das Feld noch nicht unterstuetzte.
    		if (StringUtils.trimToNull(gvcode) == null)
      		gvcode = betrag >= 0.0d? "051" : "020";
    		
    		out.write(":86:" + gvcode + "?00" + StringUtils.trimToEmpty(u.getArt()) + "?10" + StringUtils.trimToEmpty(u.getPrimanota()));
    		
    		//Verwendungszweck
    		String[] lines = VerwendungszweckUtil.toArray(u);
    		for (int m=0;m<lines.length;++m)
    		{
      		// in MT940 sind nur max. 10 Zeilen zugelassen. Die restlichen muessen wir
    		  // ignorieren. Siehe FinTS_3.0_Messages_Finanzdatenformate_2010-08-06_final_version.pdf
    		  // (Seite 179, strukturierte Belegung des Feldes 86)
    		  if (m > 9)
    		    break;
          out.write("?2" + Integer.toString(m) + lines[m]);
    		}

        String blz = StringUtils.trimToNull(u.getGegenkontoBLZ());
        String kto = StringUtils.trimToNull(u.getGegenkontoNummer());
        String nam = StringUtils.trimToNull(u.getGegenkontoName());
        String add = StringUtils.trimToNull(u.getAddKey());
        if (blz != null) out.write("?30" + blz);
        if (kto != null) out.write("?31" + kto);
        if (nam != null) out.write("?32" + nam);
        if (add != null) out.write("?34" + add);

        out.write(NL);
    		

        if (showSaldo)
        {
          out.write(":62F:");
          //Soll-Haben-Kennung f�r den Schlusssaldo ermitteln
          double schlussSaldo = u.getSaldo();
          out.write(schlussSaldo >= 0.0d ? "C" : "D");
          out.write(DF_YYMMDD.format(u.getDatum()) + curr + df.format(schlussSaldo).replace("-","") + NL);
        }
    		
    		out.write("-" + NL);
      }
    }
    catch (IOException e)
    {
      Logger.error("unable to write MT940 file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Export der Daten. " + e.getMessage()));
    }
    finally
    {
      IOUtil.close(out);
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!Umsatz.class.equals(objectType))
      return null;

    return new IOFormat[]{new IOFormat() {
      public String getName()
      {
        return i18n.tr("Swift MT940-Format");
      }
    
      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[]{"sta"};
      }
    }};
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("MT940-Format");
  }

  
  /**
   * Ableitung von OutputStreamWriter, um die Umlaute umzuschreiben.
   */
  private class MyOutputStreamWriter extends OutputStreamWriter
  {
    private String[] search  = new String[]{"�", "�", "�", "�", "�", "�", "�"};
    private String[] replace = new String[]{"UE","OE","AE","ue","oe","ae","ss"};
    private boolean doReplace = true;
    
    /**
     * ct.
     * @param out
     * @throws UnsupportedEncodingException
     */
    public MyOutputStreamWriter(OutputStream out) throws UnsupportedEncodingException
    {
      super(out,CHARSET);
      
      // Umlaute ersetzen. Sind gemaess "FinTS_3.0_Messages_Finanzdatenformate_2010-08-06_final_version.pdf"
      // in SWIFT nicht zulaessig. Wir machen das mal konfigurierbar. Dann kann es
      // der User bei Bedarf deaktivieren
      doReplace = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings().getBoolean("export.mt940.replaceumlauts",true);
    }

    /**
     * @see java.io.Writer#write(java.lang.String)
     */
    public void write(String str) throws IOException
    {
      if (doReplace)
        str = StringUtils.replaceEach(str,search,replace);
      super.write(str);
    }
  }
}


/*********************************************************************
 * $Log: MT940UmsatzExporter.java,v $
 * Revision 1.13  2012/03/06 21:44:26  willuhn
 * @C code-cleanup
 *
 * Revision 1.12  2011-07-25 17:17:19  willuhn
 * @N BUGZILLA 1065 - zusaetzlich noch addkey
 *
 * Revision 1.11  2011-07-25 14:42:41  willuhn
 * @N BUGZILLA 1065
 *
 * Revision 1.10  2011-06-23 07:37:28  willuhn
 * @N Ersetzen der Umlaute beim MT940-Export abschaltbar
 * @N Beim MT940-Import explizit mit ISO-8859 lesen - ist zwar eigentlich nicht noetig, weil da per Definition keine Umlaute enthalten sein duerfen - aber wir sind ja tolerant ;)
 *
 * Revision 1.9  2011-06-09 08:50:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2011-06-09 08:40:33  willuhn
 * @B BUGZILLA 669 - GV-Code fehlte in Feld :86:
 *
 * Revision 1.7  2011-06-07 10:07:50  willuhn
 * @C Verwendungszweck-Handling vereinheitlicht/vereinfacht - geht jetzt fast ueberall ueber VerwendungszweckUtil
 *
 * Revision 1.6  2011-02-28 10:36:54  willuhn
 * @R t o d o  entfernt
 *
 * Revision 1.5  2011-01-12 18:03:14  willuhn
 * @B Tag :20: (Auftragsreferenz-Nr.) fehlte. Konnte sonst nicht von HBCI4Java (sprich Hibiscus MT940-Import) wieder gelesen werden. Und das waer schon maechtig doof, wenn Hibiscus die eigenen Exports nicht lesen kann ;)
 *
 * Revision 1.4  2011-01-12 17:46:30  willuhn
 * @B Zeiger im Array fehlte
 *
 * Revision 1.3  2011-01-12 17:39:46  willuhn
 * @B "-" entfernen
 *
 * Revision 1.2  2011-01-12 17:37:43  willuhn
 * @C MT940-Import und -Export sollten den gleichen Namen tragen
 *
 * Revision 1.1  2011-01-05 00:10:11  willuhn
 * @N BUGZILLA 669 - MT940-Exporter fuer Umsaetze - basierend auf dem Code von Andre. Noch zu testen!
 *
 **********************************************************************/