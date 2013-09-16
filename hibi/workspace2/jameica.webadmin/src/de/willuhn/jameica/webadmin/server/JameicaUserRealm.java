/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/server/JameicaUserRealm.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/04/12 13:35:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.server;

import java.security.Principal;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.security.UserRealm;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.Settings;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Jetty-UserRealm, um das Login mittels
 * Jameica-Masterpasswort abwickeln zu koennen.
 */
public class JameicaUserRealm implements UserRealm
{
  private Principal admin = new JameicaPrincipal("admin");

  /**
   * @see org.mortbay.jetty.security.UserRealm#authenticate(java.lang.String, java.lang.Object, org.mortbay.jetty.Request)
   */
  public Principal authenticate(String username, Object password, Request request)
  {
    if (!Settings.getUseAuth())
      return this.admin;
    
    if (username == null || username.length() == 0)
      return null;
    if (password == null)
      return null;
    
    String pw = password.toString();
    
    if (pw == null || pw.length() == 0)
      return null;
    
    try
    {
      // Den Usernamen vergleichen wir nicht.
      if (pw.equals(Application.getCallback().getPassword()))
        return this.admin;
    }
    catch (Exception e)
    {
      Logger.error("error while checking password, denying request",e);
    }
    
    Logger.warn("invalid password for user " + username);
    return null;
  }

  /**
   * @see org.mortbay.jetty.security.UserRealm#disassociate(java.security.Principal)
   */
  public void disassociate(Principal p)
  {
    // ignore
  }

  /**
   * @see org.mortbay.jetty.security.UserRealm#getName()
   */
  public String getName()
  {
    return "jameica.webadmin";
  }

  /**
   * @see org.mortbay.jetty.security.UserRealm#getPrincipal(java.lang.String)
   */
  public Principal getPrincipal(String name)
  {
    if (name == null || name.length() == 0)
      return null;
    if ("admin".equals(name))
      return this.admin;
    return null;
  }

  /**
   * @see org.mortbay.jetty.security.UserRealm#isUserInRole(java.security.Principal, java.lang.String)
   */
  public boolean isUserInRole(Principal p, String role)
  {
    if (p == null)
      return false;
    return (p.equals(this.admin));
  }

  /**
   * @see org.mortbay.jetty.security.UserRealm#logout(java.security.Principal)
   */
  public void logout(Principal p)
  {
    // ignore
  }

  /**
   * @see org.mortbay.jetty.security.UserRealm#popRole(java.security.Principal)
   */
  public Principal popRole(Principal p)
  {
    // DUMMY
    return p;
  }

  /**
   * @see org.mortbay.jetty.security.UserRealm#pushRole(java.security.Principal, java.lang.String)
   */
  public Principal pushRole(Principal p, String role)
  {
    return null;
  }

  /**
   * @see org.mortbay.jetty.security.UserRealm#reauthenticate(java.security.Principal)
   */
  public boolean reauthenticate(Principal p)
  {
    if (p == null)
      return false;
    return (p.equals(this.admin));
  }

  /**
   * Implementierung fuer den Jameica-Admin-User.
   */
  private class JameicaPrincipal implements Principal
  {
    private String name = null;
    
    /**
     * ct.
     * @param name
     */
    private JameicaPrincipal(String name)
    {
      this.name = name;
    }
    
    /**
     * @see java.security.Principal#getName()
     */
    public String getName()
    {
      return this.name;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
      if (obj == null || !(obj instanceof Principal))
        return false;
      return this.name.equals(((Principal)obj).getName());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
      return this.name.hashCode();
    }
    
  }
}


/*********************************************************************
 * $Log: JameicaUserRealm.java,v $
 * Revision 1.1  2007/04/12 13:35:17  willuhn
 * @N SSL-Support
 * @N Authentifizierung
 * @N Korrektes Logging
 *
 **********************************************************************/