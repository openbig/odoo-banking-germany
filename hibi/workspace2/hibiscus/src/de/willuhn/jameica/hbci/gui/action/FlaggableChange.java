/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/FlaggableChange.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/09/15 00:23:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Flaggable;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Setz oder entfermnt die genannten Flags in ein oder mehreren Objekten.
 */
public class FlaggableChange implements Action
{
  private int flags   = 0;
  private boolean add = true;
  
  /**
   * ct.
   * @param flags die zu setzenden Flags.
   * @param add true, wenn Flags hinzugefuegt werden sollen. Andernfalls werden sie entfernt.
   */
  public FlaggableChange(int flags, boolean add)
  {
    this.flags = flags;
    this.add   = add;
  }

  /**
   * Erwartet ein Objekt vom Typ <code>Flaggable</code> oder <code>Flaggable[]</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen oder mehrere Datens�tze aus"));

    if (!(context instanceof Flaggable) && !(context instanceof Flaggable[]))
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen oder mehrere Datens�tze aus"));

    Flaggable[] objects = null;
    
    if (context instanceof Flaggable)
      objects = new Flaggable[]{(Flaggable) context};
    else
      objects = (Flaggable[]) context;

    if (objects.length == 0)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen oder mehrere Datens�tze aus"));

    try
    {
      objects[0].transactionBegin();
      for (int i=0;i<objects.length;++i)
      {
        int current = objects[i].getFlags();
        boolean have = (current & this.flags) != 0;
        if (this.add && !have)
          objects[i].setFlags(current | this.flags);
        else if (!this.add && have)
          objects[i].setFlags(current ^ this.flags);
        objects[i].store();
        Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(objects[i]));
      }
      objects[0].transactionCommit();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("�nderungen gespeichert"), StatusBarMessage.TYPE_SUCCESS));
    }
		catch (Exception e)
		{
	    try {
	      objects[0].transactionRollback();
	    }
	    catch (Exception e1) {
	      Logger.error("unable to rollback transaction",e1);
	    }
	    
	    if (e instanceof ApplicationException)
	      throw (ApplicationException) e;

	    Logger.error("error while setting flags",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern der �nderungen"));
		}
  }
}


/**********************************************************************
 * $Log: FlaggableChange.java,v $
 * Revision 1.1  2009/09/15 00:23:34  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.2  2009/04/05 21:00:36  willuhn
 * @B BUGZILLA 715
 *
 * Revision 1.1  2009/02/04 23:06:24  willuhn
 * @N BUGZILLA 308 - Umsaetze als "geprueft" markieren
 *
 **********************************************************************/