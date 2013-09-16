/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/crypto/RSAEngine.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/02/08 18:27:53 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security.crypto;

import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Verwendet direkt RSA zum Verschluesseln sowie den Public- und Private-Key.
 * Diese Implementierung ist daher nur fuer sehr kleine Datenmengen geeignet.
 * Also zum Beispiel fuer Passwoerter.
 */
public class RSAEngine implements Engine
{
  /**
   * @see de.willuhn.jameica.security.crypto.Engine#encrypt(java.io.InputStream, java.io.OutputStream)
   */
  public void encrypt(InputStream is, OutputStream os) throws Exception
  {
    Logger.debug("creating cipher");
    Cipher cipher = Cipher.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
    cipher.init(Cipher.ENCRYPT_MODE,Application.getSSLFactory().getPublicKey());

    Logger.debug("encrypting data");
    int size = cipher.getBlockSize();
    Logger.debug("using block size (in bytes): " + size);
    byte[] buf = new byte[size];
    int read = 0;
    do
    {
      read = is.read(buf);
      if (read > 0)
        os.write(cipher.doFinal(buf,0,read));
    }
    while (read != -1);
  }

  /**
   * @see de.willuhn.jameica.security.crypto.Engine#decrypt(java.io.InputStream, java.io.OutputStream)
   */
  public void decrypt(InputStream is, OutputStream os) throws Exception
  {
    Logger.debug("creating cipher");
    Cipher cipher = Cipher.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
    cipher.init(Cipher.DECRYPT_MODE,Application.getSSLFactory().getPrivateKey());

    int size = cipher.getBlockSize();
    Logger.debug("using block size (in bytes): " + size);

    Logger.debug("decrypting data");
    byte[] buf = new byte[size];
    int read = 0;
    do
    {
      read = is.read(buf);
      if (read > 0)
      {
        os.write(cipher.doFinal(buf,0,read));
      }
    }
    while (read != -1);
  }

}



/**********************************************************************
 * $Log: RSAEngine.java,v $
 * Revision 1.1  2011/02/08 18:27:53  willuhn
 * @N Code zum Ver- und Entschluesseln in neue Crypto-Engines ausgelagert und neben der bisherigen RSAEngine eine AES- und eine PBEWithMD5AndDES-Engine implementiert
 *
 **********************************************************************/