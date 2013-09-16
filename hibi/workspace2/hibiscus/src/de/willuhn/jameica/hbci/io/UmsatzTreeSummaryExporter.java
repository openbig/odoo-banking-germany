/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/UmsatzTreeSummaryExporter.java,v $
 * $Revision: 1.4 $
 * $Date: 2010/12/12 23:16:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPCell;

import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Exporter fuer einen Tree von Umsaetzen im PDF-Format.
 * Hierbei werden nur die Summen der einzelnen Kategorien exportiert.
 */
public class UmsatzTreeSummaryExporter extends AbstractUmsatzTreeExporter
{
  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    if (objects == null || !(objects instanceof UmsatzTree[]))
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie die zu exportierenden Ums�tze aus"));

    UmsatzTree[] t = (UmsatzTree[]) objects;
    if (t.length == 0)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie die zu exportierenden Ums�tze aus"));

    UmsatzTree tree = t[0];
    List list = tree.getUmsatzTree();
    
    Reporter reporter = null;
    try
    {
      reporter = new Reporter(os, monitor, i18n.tr("Umsatzkategorien"), this.getSubTitle(tree), list.size());

      reporter.addHeaderColumn(i18n.tr("Kategorie"), Element.ALIGN_CENTER, 130,BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn(i18n.tr("Betrag"), Element.ALIGN_CENTER, 30,BaseColor.LIGHT_GRAY);
      reporter.createHeader();

      // Iteration ueber die Kategorien
      for (int i=0;i<list.size(); ++i)
      {
        UmsatzTreeNode ug = (UmsatzTreeNode) list.get(i);

        PdfPCell cell = reporter.getDetailCell((String) ug.getAttribute("name"), Element.ALIGN_LEFT);
        reporter.addColumn(cell);
        reporter.addColumn(reporter.getDetailCell((Double) ug.getAttribute("betrag")));
        
        reporter.setNextRecord();
      }
      if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_DONE);
    }
    catch (Exception e)
    {
      if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      Logger.error("error while creating report", e);
      throw new ApplicationException(i18n.tr("Fehler beim Erzeugen der Auswertung: {0}",e.getMessage()), e);
    }
    finally
    {
      if (reporter != null)
      {
        try
        {
          reporter.close();
        }
        catch (Exception e)
        {
          Logger.error("unable to close report",e);
        }
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("PDF-Format: Summen der Hauptkategorien");
  }
}
