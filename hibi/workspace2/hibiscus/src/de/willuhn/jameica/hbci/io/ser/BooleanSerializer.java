/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/ser/BooleanSerializer.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/16 00:44:18 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.ser;

import java.io.IOException;

/**
 * Implementierung eines Serializers fuer Boolean-Werte.
 */
public class BooleanSerializer extends DefaultSerializer<Boolean>
{
  /**
   * @see de.willuhn.jameica.hbci.io.ser.Serializer#unserialize(java.lang.Object, java.lang.String)
   */
  public Boolean unserialize(Object context, String value) throws IOException
  {
    return Boolean.valueOf(value);
  }

}



/**********************************************************************
 * $Log: BooleanSerializer.java,v $
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