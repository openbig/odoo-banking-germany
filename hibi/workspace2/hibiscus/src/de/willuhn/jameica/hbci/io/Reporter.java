/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Reporter.java,v $
 * $Revision: 1.6 $
 * $Date: 2012/03/28 22:47:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Kapselt den Export von Daten im PDF-Format.
 */
public class Reporter
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private List<PdfPCell> headers = new ArrayList<PdfPCell>();
  private List<Integer> widths = new ArrayList<Integer>();

  private OutputStream out = null;
  private Document rpt = null;
  private PdfPTable table = null;

  private int maxRecords = 0;
  private int currRecord = 0;

  private ProgressMonitor monitor = null;

  /**
   * ct.
   * @param out
   * @param monitor
   * @param title
   * @param subtitle
   * @param maxRecords
   * @throws DocumentException
   */
  public Reporter(OutputStream out, ProgressMonitor monitor, String title, String subtitle, int maxRecords) throws DocumentException
  {
    this.out = out;
    this.monitor = monitor;
    this.maxRecords = maxRecords;
    this.rpt = new Document();

    PdfWriter writer = PdfWriter.getInstance(rpt, out);
    rpt.setMargins(80, 30, 20, 45); // links, rechts, oben, unten

    if (this.monitor != null)
    {
      this.monitor.setStatusText(i18n.tr("Erzeuge Liste"));
      this.monitor.addPercentComplete(1);
    }

    Manifest mf = Application.getPluginLoader().getManifest(HBCI.class);
    rpt.addAuthor(i18n.tr("{0} - Version {1}", mf.getName(), mf.getVersion().toString()));
    rpt.addTitle(subtitle);

    HeaderFooter hf = new HeaderFooter();
    writer.setPageEvent(hf);
    hf.setFooter(i18n.tr("{0} | {1} | erstellt am {2}              Seite:  ", title, subtitle, HBCI.LONGDATEFORMAT.format(new Date())));

    rpt.open();
    try
    {
      URL url = mf.getClassLoader().getResource("icons/hibiscus-icon-64x64.png");
      Image image = Image.getInstance(url);
      image.scaleAbsolute(32, 32);
      rpt.add(image);
    }
    catch (Exception e)
    {
      Logger.error("unable to add hibiscus icon, will be ignored", e);
    }

    Paragraph pTitle = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
    pTitle.setAlignment(Element.ALIGN_CENTER);
    rpt.add(pTitle);

    Paragraph psubTitle = new Paragraph(subtitle, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10));
    psubTitle.setAlignment(Element.ALIGN_CENTER);
    rpt.add(psubTitle);
  }

  /**
   * Fuegt einen neuen Absatz hinzu.
   * @param p
   * @throws DocumentException
   */
  public void add(Paragraph p) throws DocumentException
  {
    rpt.add(p);
  }

  /**
   * Fuegt der Tabelle einen neuen Spaltenkopf hinzu.
   * @param text
   * @param align
   * @param width
   * @param color
   */
  public void addHeaderColumn(String text, int align, int width, BaseColor color)
  {
    headers.add(getDetailCell(text, align, color));
    widths.add(new Integer(width));
  }

  /**
   * Fuegt eine neue Spalte hinzu.
   * @param cell
   */
  public void addColumn(PdfPCell cell)
  {
    table.addCell(cell);
  }

  /**
   * Rueckt den Monitor weiter.
   */
  public void setNextRecord()
  {
    currRecord++;
    if (monitor != null)
      monitor.setPercentComplete(currRecord / maxRecords * 100);
  }

  /**
   * Erzeugt den Tabellen-Header.
   * @throws DocumentException
   */
  public void createHeader() throws DocumentException
  {
    table = new PdfPTable(headers.size());
    int[] w = new int[headers.size()];
    for (int i = 0; i < headers.size(); i++)
      w[i] = widths.get(i).intValue();
    table.setWidths(w);
    table.setWidthPercentage(100);
    table.setSpacingBefore(10);
    table.setSpacingAfter(0);
    for (int i = 0; i < headers.size(); ++i)
    {
      table.addCell(headers.get(i));
    }
    table.setHeaderRows(1);
  }

  /**
   * Schliesst den Report.
   * @throws IOException
   * @throws DocumentException
   */
  public void close() throws IOException, DocumentException
  {
    try
    {
      if (monitor != null)
      {
        monitor.setPercentComplete(100);
        monitor.setStatusText(i18n.tr("PDF-Export beendet"));
      }
      rpt.add(table);
      rpt.close();
    }
    finally
    {
      // Es muss sichergestellt sein, dass der OutputStream
      // immer geschlossen wird
      IOUtil.close(this.out);
    }
  }

  /**
   * Erzeugt eine Zelle der Tabelle.
   * @param text der anzuzeigende Text.
   * @param align die Ausrichtung.
   * @param backgroundcolor die Hintergundfarbe.
   * @return die erzeugte Zelle.
   */
  public PdfPCell getDetailCell(String text, int align, BaseColor backgroundcolor)
  {
    PdfPCell cell = new PdfPCell(new Phrase(notNull(text), FontFactory.getFont(FontFactory.HELVETICA, 8)));
    cell.setHorizontalAlignment(align);
    cell.setBackgroundColor(backgroundcolor);
    return cell;
  }

  /**
   * Erzeugt eine Zelle der Tabelle.
   * @param text der anzuzeigende Text.
   * @param align die Ausrichtung.
   * @return die erzeugte Zelle.
   */
  public PdfPCell getDetailCell(String text, int align)
  {
    return getDetailCell(text, align, BaseColor.WHITE);
  }

  /**
   * Erzeugt eine Zelle der Tabelle.
   * @param value die Zahl.
   * @return die erzeugte Zelle.
   */
  public PdfPCell getDetailCell(Double value)
  {
    return getDetailCell(value.doubleValue());
  }

  /**
   * Erzeugt eine Zelle fuer die uebergebene Zahl.
   * @param value die Zahl.
   * @return die erzeugte Zelle.
   */
  public PdfPCell getDetailCell(double value)
  {
    Font f = FontFactory.getFont(FontFactory.HELVETICA, 8f, Font.NORMAL, value >= 0 ? BaseColor.BLACK : BaseColor.RED);
    PdfPCell cell = new PdfPCell(new Phrase(HBCI.DECIMALFORMAT.format(value), f));
    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    return cell;
  }

  /**
   * Gibt einen Leerstring aus, falls der Text null ist.
   * @param text der Text.
   * @return der Text oder Leerstring - niemals null.
   */
  public String notNull(String text)
  {
    return text == null ? "" : text;
  }
  
  
  /**
   * Ersatz fuer die HeaderFooter-Klasse, die es bis iText 1.x gab. Wird zur Zeit
   * nur fuer den Footer gebraucht.
   */
  private class HeaderFooter extends PdfPageEventHelper
  {
    private String footer  = null;
    private int pagenumber = 0;

    /**
     * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
     */
    public void onOpenDocument(PdfWriter writer, Document document)
    {
    }

    /**
     * @see com.itextpdf.text.pdf.PdfPageEventHelper#onChapter(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document, float, com.itextpdf.text.Paragraph)
     */
    public void onChapter(PdfWriter writer, Document document, float paragraphPosition, Paragraph title)
    {
    }

    /**
     * @see com.itextpdf.text.pdf.PdfPageEventHelper#onStartPage(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
     */
    public void onStartPage(PdfWriter writer, Document document)
    {
      this.pagenumber++;
    }

    /**
     * Speichert den Footer-Text.
     * @param footer der Footer-Text.
     */
    public void setFooter(String footer)
    {
      this.footer = footer;
    }

    /**
     * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
     */
    public void onEndPage(PdfWriter writer, Document document)
    {
      Rectangle rect = document.getPageSize();
      switch (writer.getPageNumber() % 2)
      {
      case 0:
        // ColumnText.showTextAligned(writer.getDirectContent(),
        // Element.ALIGN_RIGHT, header[0], rect.getRight(), rect.getTop(), 0);
        break;
      case 1:
        // ColumnText.showTextAligned(writer.getDirectContent(),
        // Element.ALIGN_LEFT,
        // header[1], rect.getLeft(), rect.getTop(), 0);
        break;
      }
      float left = rect.getLeft() + document.leftMargin();
      float right = rect.getRight() - document.rightMargin();
      float bottom = rect.getBottom() + document.bottomMargin();
      PdfContentByte pc = writer.getDirectContent();
      pc.setColorStroke(BaseColor.BLACK);
      pc.setLineWidth(0.5f);
      pc.moveTo(left, bottom - 5);
      pc.lineTo(right, bottom - 5);
      pc.stroke();
      pc.moveTo(left, bottom - 25);
      pc.lineTo(right, bottom - 25);
      pc.stroke();

      ColumnText.showTextAligned(pc,Element.ALIGN_CENTER,new Phrase(footer + " " + pagenumber, FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL)), (left + right) / 2,bottom - 18, 0);
    }
  }

}
