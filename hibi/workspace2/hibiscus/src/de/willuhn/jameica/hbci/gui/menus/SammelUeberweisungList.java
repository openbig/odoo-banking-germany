/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/SammelUeberweisungList.java,v $
 * $Revision: 1.10 $
 * $Date: 2012/01/27 22:43:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.action.Print;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.Duplicate;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungExecute;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungExport;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungImport;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.TerminableMarkExecuted;
import de.willuhn.jameica.hbci.io.print.PrintSupportSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit SammelUeberweisungen gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class SammelUeberweisungList extends ContextMenu
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	/**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Sammel-Ueberweisungen.
	 */
	public SammelUeberweisungList()
	{
		addItem(new SingleItem(i18n.tr("�ffnen"), new SammelUeberweisungNew(),"document-open.png"));
    addItem(new ContextMenuItem(i18n.tr("Neue Sammel-�berweisung..."), new SNeu(),"text-x-generic.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("L�schen..."), new DBObjectDelete(),"user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new SingleItem(i18n.tr("Duplizieren..."), new Duplicate(),"edit-copy.png"));
    addItem(ContextMenuItem.SEPARATOR);
		addItem(new NotActiveMenuItem(i18n.tr("Jetzt ausf�hren..."), new SammelUeberweisungExecute(),"emblem-important.png"));
    addItem(new NotActiveMultiMenuItem(i18n.tr("Als \"ausgef�hrt\" markieren..."), new TerminableMarkExecuted(),"emblem-default.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Drucken..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new Print().handleAction(new PrintSupportSammelUeberweisung((SammelUeberweisung) context));
      }
    },"document-print.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),new SammelUeberweisungExport(),"document-save.png"));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new SammelUeberweisungImport(),"document-open.png"));
		
	}

	/**
	 * Ueberschreiben wir, um <b>grundsaetzlich</b> eine neue Sammel-Lastschrift
	 * anzulegen - auch wenn der Focus auf einer existierenden liegt.
   */
  private class SNeu extends SammelUeberweisungNew
	{
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
    	super.handleAction(null);
    }
	} 
	
  /**
   * Ueberschrieben, um zu pruefen, ob ein Array oder ein einzelnes Element markiert ist.
   */
  private class SingleItem extends CheckedContextMenuItem
  {
    /**
     * @param text
     * @param action
     * @param optionale Angabe eines Icons.
     */
    private SingleItem(String text, Action action, String icon)
    {
      super(text,action,icon);
    }
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof SammelUeberweisung[])
        return false;
      return super.isEnabledFor(o);
    }
  }

	/**
	 * Ueberschreiben wir, damit das Item nur dann aktiv ist, wenn die
	 * Sammel-Ueberweisung noch nicht ausgefuehrt wurde.
   */
  private class NotActiveMenuItem extends ContextMenuItem
	{
		
    /**
     * ct.
     * @param text anzuzeigender Text.
     * @param a auszufuehrende Action.
     * @param icon optionales Icon.
     */
    public NotActiveMenuItem(String text, Action a, String icon)
    {
      super(text, a, icon);
    }

	  /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
    	if (o == null || !(o instanceof SammelUeberweisung))
    		return false;
    	try
    	{
				SammelUeberweisung u = (SammelUeberweisung) o;
    		return !u.ausgefuehrt();
    	}
    	catch (Exception e)
    	{
    		Logger.error("error while enable check in menu item",e);
    	}
    	return false;
    }
	}

  /**
   * Liefert nur dann true, wenn alle uebergebenen Auftraege noch nicht
   * ausgefuehrt wurden.
   */
  private class NotActiveMultiMenuItem extends CheckedContextMenuItem
  {
    
    /**
     * ct.
     * @param text anzuzeigender Text.
     * @param a auszufuehrende Action.
     * @param icon optionales Icon.
     */
    public NotActiveMultiMenuItem(String text, Action a, String icon)
    {
      super(text, a, icon);
    }

    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o == null || (!(o instanceof Terminable) && !(o instanceof Terminable[])))
        return false;
      try
      {
        if (o instanceof Terminable)
          return !((Terminable)o).ausgefuehrt();

        Terminable[] t = (Terminable[]) o;
        for (int i=0;i<t.length;++i)
        {
          if (t[i].ausgefuehrt())
            return false;
        }
        return true;
      }
      catch (RemoteException e)
      {
        Logger.error("unable to check if terminable is already executed",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Pr�fen, ob Auftrag bereits ausgef�hrt wurde"),StatusBarMessage.TYPE_ERROR));
      }
      return false;
    }
  }

}
