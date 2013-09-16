/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/Sensor.java,v $
 * $Revision: 1.5 $
 * $Date: 2012/04/17 22:25:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices;

/**
 * Interface eines einzelnen Sensors.
 * @param <T> der Typ des Sensors.
 */
public class Sensor<T> implements UniqueItem, Cloneable
{
  /**
   * Default-Typ von Messwerten.
   */
  public final static Type TYPE_DEFAULT = Type.GAUGE;
  
  private T value     = null;
  private String name = null;
  private String uuid = null;
  private Type type   = TYPE_DEFAULT;
  
  private Class<? extends Serializer> serializer = StringSerializer.class;
  
  /**
   * Typ des Messwertes.
   * Siehe auch http://wiki.secitec.net/doku.php?id=tutorials:rrdtool
   * bzw. http://oss.oetiker.ch/rrdtool/tut/rrd-beginners.en.html
   */
  public enum Type
  {
    /**
     * Speichert keine Veränderungen pro Zeitraum sondern die aktuellen Werte, 
     * ohne irgendwelche Divisionen oder dergleichen.
     * Das ist der Default-Wert.
     */
    GAUGE,
    
    /**
     * Ansteigender Wert, der die Veränderungen über den Zeitraum zum vorherigen
     * Wert speichert, z.B. Traffic-Counter bei einem Router.
     */
    COUNTER,
    
    /**
     * Aehnlich zu COUNTER, nur werden auch negative Werte erlaubt
     * (Z.Bsp. Veränderung von Festplattenspeicher).
     */
    DERIVE,
    
    /**
     * Speichert ebenfalls die Veränderung über den Zeitraum, allerdings wird der
     * vorherige Wert als 0 angenommen. Es speichert also nur den derzeitigen
     * Wert, dividiert durch das Stepintervall.
     */
    ABSOLUTE
  }
  
  /**
   * Liefert einen sprechenden Namen fuer den Sensor.
   * @return sprechender Name des Sensors.
   */
  public String getName()
  {
    return this.name;
  }
  
  /**
   * Speichert einen sprechenden Namen fuer den Sensor.
   * @param name sprechender Name fuer den Sensor.
   */
  public void setName(String name)
  {
    this.name = name; 
  }

  /**
   * Liefert den Messwert.
   * @return der Messwert.
   */
  public T getValue()
  {
    return this.value;
  }
  
  /**
   * Speichert den Messwert.
   * @param value
   */
  public void setValue(T value)
  {
    this.value = value;
  }
  
  /**
   * Liefert den zu verwendenden Serializer fuer die Messwerte.
   * @return der zu verwendende Serializer fuer die Messwerte.
   */
  public Class<? extends Serializer> getSerializer()
  {
    return this.serializer;
  }
  
  /**
   * Speichert den Serializer.
   * @param serializer der Serializer.
   */
  public void setSerializer(Class<? extends Serializer> serializer)
  {
    this.serializer = serializer;
  }
  
  /**
   * @see de.willuhn.jameica.sensors.devices.UniqueItem#getUuid()
   */
  public String getUuid()
  {
    return this.uuid;
  }
  
  /**
   * Speichert die eindeutige ID fuer das Objekt.
   * Diese ID sollte sich niemals aendern, da sich sonst bereits
   * archivierte Messwerte nicht mehr diesem Objekt zuordnen lassen.
   * @param uuid die eindeutige ID fuer das Objekt.
   */
  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }
  
  /**
   * Liefert den Typ des Messwertes.
   * @return Typ des Messwertes.
   */
  public Type getType()
  {
    return this.type;
  }
  
  /**
   * Speichert den Typ des Messwertes.
   * @param type Typ des Messwertes.
   */
  public void setType(Type type)
  {
    this.type = type;
  }

  /**
   * @see java.lang.Object#clone()
   */
  @SuppressWarnings("javadoc")
  public Object clone()
  {
    try
    {
      Sensor clone = (Sensor) super.clone();
      // Da alle properties primitiv bzw. immutable sind, muessen wir
      // nichts manuell clonen - das macht alles Java selbst
      return clone;
    }
    catch (CloneNotSupportedException e)
    {
      throw new RuntimeException(e);
    }
  }

}


/**********************************************************************
 * $Log: Sensor.java,v $
 * Revision 1.5  2012/04/17 22:25:05  willuhn
 * @N 24h-Maximal- und -Minimal-Werte
 *
 * Revision 1.4  2011-09-13 09:08:34  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.3  2009/09/28 14:26:47  willuhn
 * @N Unterstuetzung fuer die anderen Sensor-Typen von RRD
 *
 * Revision 1.2  2009/09/08 10:38:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/
