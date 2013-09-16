/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts.table;

import org.eclipse.swt.widgets.Table;

import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.TablePart;

/**
 * Gemeinsames Interface fuer Features, die zu einer Tabelle nachgeruestet werden koennen.
 */
public interface Feature
{
  /**
   * Liste der Events, auf die ein Feature reagieren kann.
   */
  public enum Event
  {
    /**
     * Wird ausgeloest, wenn die paint()-Funktion der Tabelle aufgerufen wird.
     */
    PAINT,
  }
  
  /**
   * Hilfsklasse mit Meta-Informationen fuer das Feature.
   */
  public class Context
  {
    /**
     * Die SWT-Tabelle selbst.
     */
    public Table control;
    
    /**
     * Das Context-Menu der Tabelle.
     */
    public ContextMenu menu;
    
    /**
     * Die Tabelle selbst.
     */
    public TablePart table;
  }
  
  /**
   * Liefert true, wenn das Feature auf das angegebene Event reagieren soll.
   * @param e das Event.
   * @return true, wenn es auf das angegebene Event reagieren soll.
   */
  public boolean onEvent(Event e);
  
  /**
   * Wird aufgerufen, wenn das angegebene Event ausgeloest wurde.
   * @param e das Event.
   * @param ctx Context-Infos.
   */
  public void handleEvent(Event e, Context ctx);
}



/**********************************************************************
 * $Log$
 **********************************************************************/