/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/TextMessage.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/07/18 17:12:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.io.Serializable;
import java.util.Date;

/**
 * Eine Standard-Textnachricht mit Text und Titel.
 */
public class TextMessage implements Message, Serializable
{
  private Date date    = new Date();
  private String text  = null;
  private String title = null;
  
  /**
   * ct.
   * Parameterloser Konstruktor fuer Bean-Konformitaet.
   * Damit laesst sich die Message serialisieren.
   */
  public TextMessage()
  {
  }
  
  /**
   * ct.
   * @param text
   */
  public TextMessage(String text)
  {
    this(null,text);
  }
  
  /**
   * ct.
   * @param title
   * @param text
   */
  public TextMessage(String title, String text)
  {
    this.title = title;
    this.text  = text;
  }
  
  /**
   * Liefert das Erstellungs-Datum.
   * @return das Erstellungs-Datum.
   */
  public Date getDate()
  {
    return this.date;
  }
  
  /**
   * Liefert den Text.
   * @return der Text.
   */
  public String getText()
  {
    return this.text;
  }
  
  /**
   * Liefert den Titel.
   * @return der Titel.
   */
  public String getTitle()
  {
    return this.title;
  }
  
  /**
   * Speichert das Erstellungs-Datum.
   * @param date das Erstellungsdatum.
   */
  public void setDate(Date date)
  {
    this.date = date;
  }

  /**
   * Speichert den Text.
   * @param text der Text.
   */
  public void setText(String text)
  {
    this.text = text;
  }

  /**
   * Speichert den Titel.
   * @param title der Titel.
   */
  public void setTitle(String title)
  {
    this.title = title;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    if (title == null || title.length() == 0)
      return "[" + getDate().toString() + "] " + getText();
    return "[" + getDate().toString() + "][" + title + "] " + getText();
  }

}


/*********************************************************************
 * $Log: TextMessage.java,v $
 * Revision 1.2  2008/07/18 17:12:22  willuhn
 * @N ReminderPopupAction zum Anzeigen von Remindern als Popup
 * @C TextMessage serialisierbar
 *
 * Revision 1.1  2007/06/05 11:45:09  willuhn
 * @N Benamte Message-Queues. Ermoeglicht kaskadierende und getrennt voneinander arbeitende Queues sowie das Zustellen von Nachrichten, ohne den Nachrichtentyp zu kennen
 *
 **********************************************************************/