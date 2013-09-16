/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.input;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;

/**
 * Ist zustaendig fuer Eingabefelder des Typs "Select" aka "Combo".
 * Wird die Combo-Box mit einer Liste von GenericObjects erzeugt,
 * dann wird dasPrimaer-Attribut eines jeden Objektes angezeigt.
 * @author willuhn
 */
public class SelectInput extends AbstractInput
{
  // Fachdaten
  private List list           = null;
  private Object preselected  = null;
  private String attribute    = null;
  
  // SWT-Daten
  private Combo combo         = null;
  private boolean enabled     = true;
  private boolean editable    = false;
  private String pleaseChoose = null;


  /**
   * Erzeugt eine neue Combo-Box und schreibt die Werte der uebergebenen Liste rein.
   * Um Jameica von spezifischem Code aus de.willuhn.datasource zu befreien,
   * sollte kuenftig besser der generische Konstruktor <code>List</code>,<code>Object</code>
   * verwendet werden. Damit kann die Anwendung spaeter auch auf ein anderes Persistierungsframework
   * umgestellt werden.
   * @param list Liste von Objekten.
   * @param preselected das Object, welches vorselektiert sein soll. Optional.
   * @throws RemoteException
   */
  public SelectInput(GenericIterator list, GenericObject preselected) throws RemoteException
  {
    this((List)(list != null ? PseudoIterator.asList(list) : null),preselected);
  }
  
  /**
   * Erzeugt die Combox-Box mit Beans oder Strings.
   * @param list Liste der Objekte.
   * @param preselected das vorausgewaehlte Objekt.
   */
  public SelectInput(Object[] list, Object preselected)
  {
    this((List) (list != null ? Arrays.asList(list) : null),preselected);
  }

  /**
   * Erzeugt die Combox-Box mit Beans oder Strings.
   * @param list Liste der Objekte.
   * @param preselected das vorausgewaehlte Objekt.
   */
  public SelectInput(List list, Object preselected)
  {
    super();
    this.list        = list;
    this.preselected = preselected;
  }

	/**
	 * Aendert nachtraeglich das vorausgewaehlte Element.
   * @param preselected neues vorausgewaehltes Element.
   */
  public void setPreselected(Object preselected)
  {
    this.preselected = preselected;
    
    if (this.combo == null || this.combo.isDisposed() || this.list == null)
      return;
    
    if (this.preselected == null)
      this.combo.select(0);

    boolean havePleaseChoose = this.pleaseChoose != null && this.pleaseChoose.length() > 0;
    int size = this.list.size();
    for (int i=0;i<size;++i)
    {
      int pos = havePleaseChoose ? (i+1) : i;
      Object value = this.combo.getData(Integer.toString(pos));
      if (value == null) // Fuer den Fall, dass die equals-Methode von preselected nicht mit null umgehen kann
        continue;

      try
      {
        if (BeanUtil.equals(preselected,value))
        {
          this.combo.select(pos);
          return;
        }
      }
      catch (RemoteException re)
      {
        Logger.error("unable to compare objects",re);
        return;
      }
    }
  }
	
  /**
   * Optionale Angabe eines Textes, der an Position 1 angezeigt werden soll.
   * Bei Auswahl dieses Elements, wird null zurueckgeliefert.
   * @param choose Anzuzeigender "Bitte w�hlen..."-Text.
   */
  public void setPleaseChoose(String choose)
  {
    this.pleaseChoose = choose;
  }
  
  /**
   * Legt den Namen des Attributes fest, welches von den Objekten angezeigt werden
   * soll. Bei herkoemmlichen Beans wird also ein Getter mit diesem Namen aufgerufen. 
   * Wird kein Attribut angegeben, wird bei Objekten des Typs <code>GenericObject</code>
   * der Wert des Primaer-Attributes angezeigt, andernfalls der Wert von <code>toString()</code>.
   * @param name Name des anzuzeigenden Attributes (muss im GenericObject
   * via getAttribute(String) abrufbar sein).
   */
  public void setAttribute(String name)
	{
		if (name != null)
			this.attribute = name;
	}

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
    if (this.combo != null)
      return this.combo;

    this.combo = GUI.getStyleFactory().createCombo(getParent(),this.editable ? SWT.NONE : SWT.READ_ONLY);
    this.combo.setVisibleItemCount(15); // Patch von Heiner
    this.combo.setEnabled(enabled);

    // Daten in die Liste uebernehmen
    applyList();
    
    return this.combo;
  }
  
  /**
   * Uebernimmt die Liste mit den Daten in das Control
   */
  private void applyList()
  {
    if (this.combo == null || this.combo.isDisposed())
      return;

    // Erstmal alles aus der Liste entfernen
    this.combo.removeAll();

    int selected             = -1;
    boolean havePleaseChoose = false;

    // Haben wir einen "bitte waehlen..."-Text?
    if (this.pleaseChoose != null && this.pleaseChoose.length() > 0)
    {
      this.combo.add(this.pleaseChoose);
      havePleaseChoose = true;
    }

    if (this.list != null)
    {
      try
      {
        int size = this.list.size();
        for (int i=0;i<size;++i)
        {
          Object object = this.list.get(i);

          if (object == null)
            continue;

          // Anzuzeigenden Text ermitteln
          String text = format(object);
          if (text == null)
            continue;
          this.combo.add(text);
          this.combo.setData(Integer.toString(havePleaseChoose ? i+1 : i),object);
          
          // Wenn unser Objekt dem vorausgewaehlten entspricht, und wir noch
          // keines ausgewaehlt haben merken wir uns dessen Index
          if (selected == -1 && this.preselected != null)
          {
            if (BeanUtil.equals(object,this.preselected))
            {
              selected = i;
              if (havePleaseChoose)
                selected++;
            }
          }
        }
      }
      catch (RemoteException e)
      {
        this.combo.removeAll();
        this.combo.add(Application.getI18n().tr("Fehler beim Laden der Daten..."));
        Logger.error("unable to create combo box",e);
      }
    }

    this.combo.select(selected > -1 ? selected : 0);

    // BUGZILLA 550
    if (this.editable && this.preselected != null && !this.list.contains(this.preselected) && (this.preselected instanceof String))
      this.combo.setText((String)this.preselected);
  }
  
  /**
   * Ersetzt den Inhalt der Selectbox komplett gegen die angegebene Liste.
   * @param list die neue Liste der Daten.
   */
  public void setList(List list)
  {
    this.list = list;
    this.applyList();
  }
  
  /**
   * Liefert die komplette Liste der Fachobjekte in der Liste.
   * @return die komplette Liste der Fachobjekte in der Liste.
   */
  public List getList()
  {
    return this.list;
  }

  /**
   * Formatiert die Bean passend fuer die Anzeige in der Combo-Box.
   * @param bean die Bean.
   * @return String mit dem anzuzeigenden Wert.
   */
  protected String format(Object bean)
  {
    if (bean == null)
      return null;
    try
    {
      if (this.attribute == null || this.attribute.length() == 0)
        return BeanUtil.toString(bean);

      Object value = BeanUtil.get(bean,this.attribute);
      return value == null ? null : value.toString();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to format object",re);
      return null;
    }
  }

  /**
   * Liefert das ausgewaehlte GenericObject.
   * Folglich kann der Rueckgabewert direkt nach GenericObject gecastet werden.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    if (this.combo == null || this.combo.isDisposed())
      return this.preselected;

    if (this.editable)
      return this.combo.getText();
    
    int selected = this.combo.getSelectionIndex();
    if (selected == -1)
      return null;
    
    return this.combo.getData(Integer.toString(selected));
  }

	/**
	 * Liefert den derzeit angezeigten Text zurueck.
   * @return Text.
   */
  public String getText()
	{
    if (this.combo == null || this.combo.isDisposed())
      return null;
		return combo.getText();
	}

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    if (this.combo == null || this.combo.isDisposed())
      return;
    
    combo.setFocus();
  }


  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
    setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
    setEnabled(true);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (combo != null && !combo.isDisposed())
      combo.setEnabled(enabled);
  }
  
  /**
   * Markiert die Combo-Box als editierbar. Wenn diese
   * Option aktiviert ist, wird jedoch in <code>getValue()</code>
   * generell der angezeigte Text zurueckgeliefert statt des
   * Fachobjektes. Hintergrund: Normalerweise wird die Combo-Box
   * ja mit einer Liste von Fachobjekten/Beans gefuellt.
   * Abhaengig von der Auswahl wird dann das zugehoerige
   * dahinterstehende Objekt zurueckgeliefert. Bei Freitext-Eingabe
   * existiert jedoch kein solches. Daher wird in diesem Fall
   * der eingebene Text zurueckgeliefert.
   * @param editable
   */
  public void setEditable(boolean editable)
  {
    this.editable = editable;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object o)
  {
    this.setPreselected(o);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    return enabled;
  }

	/**
	 * @see de.willuhn.jameica.gui.input.AbstractInput#update()
	 */
  protected void update() throws OperationCanceledException
  {
    // Wir machen hier nichts. 
  }
}

