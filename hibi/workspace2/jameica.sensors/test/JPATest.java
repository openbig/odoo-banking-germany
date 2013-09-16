/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/test/JPATest.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/09/13 09:08:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.willuhn.jameica.sensors.beans.Device;
import de.willuhn.jameica.sensors.beans.Sensor;
import de.willuhn.jameica.sensors.beans.Value;
import de.willuhn.jameica.sensors.devices.StringSerializer;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Testet das Schreiben von Messwerten in die Datenbank.
 */
public class JPATest
{
  private static EntityManager em = null;
  
  /**
   * Initialisiert den Test.
   * @throws Exception
   */
  @BeforeClass
  public static void setUp() throws Exception
  {
    Logger.setLevel(Level.INFO);

    Map params = new HashMap();

    params.put("hibernate.connection.driver_class","com.mysql.jdbc.Driver");
    params.put("hibernate.connection.url","jdbc:mysql://server:3306/jameica_sensors?useUnicode=Yes&characterEncoding=ISO8859_1");
    params.put("hibernate.connection.username","jameica_sensors");
    params.put("hibernate.connection.password","jameica_sensors");
    params.put("hibernate.dialect","org.hibernate.dialect.MySQLDialect");
    params.put("hibernate.show_sql","true");
    params.put("hibernate.hbm2ddl.auto","create"); // ,update,validate");
    EntityManagerFactory ef = Persistence.createEntityManagerFactory("jameica_sensors",params);
    em = ef.createEntityManager();
  }

  /**
   * Beendet den Test.
   * @throws Exception
   */
  @AfterClass
  public static void tearDown() throws Exception
  {
    if (em != null)
      em.close();
  }
  
  
  /**
   * Legt ein Device und dazu jeweils einen Sensor sowie einen Messwert an.
   * @throws Exception
   */
  @Test
  public void test001() throws Exception
  {
    EntityTransaction tx = null;

    try
    {
      tx = em.getTransaction();
      tx.begin();

      System.out.println(",---- TEST 1");
      Device d = new Device();
      d.setUuid("unit.test.device");
      em.persist(d);
      
      Sensor s = new Sensor();
      s.setUuid("unit.test.sensor.1");
      s.setSerializer(StringSerializer.class.getName());
      d.getSensors().add(s);
      em.persist(s);
      
      Value v = new Value();
      v.setDate(new Date());
      v.setValue("foo");
      s.getValues().add(v);
      em.persist(v);
      
      tx.commit();
    }
    catch (PersistenceException pe)
    {
      pe.printStackTrace();
      if (tx != null && tx.isActive())
        tx.rollback();
      throw pe;
    }
    System.out.println("`----");
  }

  /**
   * Prueft, ob das Device existiert und einen Sensor besitzt.
   * @throws Exception
   */
  @Test
  public void test002() throws Exception
  {
    System.out.println(",---- TEST 2");
    Query q = em.createQuery("from Device where uuid = ?");
    q.setParameter(1,"unit.test.device");
    Device d = (Device) q.getSingleResult();
    Assert.assertEquals(d.getSensors().size(),1);
    System.out.println("`----");
  }

  /**
   * Fuegt einen neuen Messwert hinzu.
   * @throws Exception
   */
  @Test
  public void test003() throws Exception
  {
    System.out.println(",---- TEST 3");
    Query q = em.createQuery("from Sensor where uuid = ?");
    q.setParameter(1,"unit.test.sensor.1");
    Sensor s = (Sensor) q.getSingleResult();

    Value v = new Value();
    v.setDate(new Date());
    v.setValue("bar");
    s.getValues().add(v);
    
    EntityTransaction tx = null;
    try
    {
      tx = em.getTransaction();
      tx.begin();
      em.persist(v);
      tx.commit();
    }
    catch (PersistenceException pe)
    {
      pe.printStackTrace();
      if (tx != null && tx.isActive())
        tx.rollback();
      throw pe;
    }
    System.out.println("`----");
  }

  /**
   * Prueft, ob der Sensor jetzt zwei Messwerte hat.
   * @throws Exception
   */
  @Test
  public void test004() throws Exception
  {
    System.out.println(",---- TEST 4");
    Query q = em.createQuery("from Sensor where uuid = ?");
    q.setParameter(1,"unit.test.sensor.1");
    Sensor s = (Sensor) q.getSingleResult();
    Assert.assertEquals(s.getValues().size(),2);
    System.out.println("`----");
  }
}


/**********************************************************************
 * $Log: JPATest.java,v $
 * Revision 1.4  2011/09/13 09:08:34  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.3  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 * Revision 1.2  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
 * Revision 1.1  2009/08/20 18:07:43  willuhn
 * @N Persistierung funktioniert rudimentaer
 *
 **********************************************************************/
