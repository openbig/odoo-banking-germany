/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/RDHNewDump.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/01/17 17:32:33 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh;

import java.io.CharConversionException;
import java.io.FileInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Kleine dreckige Testklasse, um eine HBCI4Java-Schluesseldiskette
 * zu entschluesseln und den XML-Inhalt unverschluesselt auf die
 * Console zu schreiben. Das Tool dient nur zu Debugging-Zwecken,
 * um mal nachschauen zu koennen, was genau in der Schluesseldiskette
 * drin steht.
 * Der Code ist aus "HBCIPassportRDHNew" (HBCI4Java) zusammenkopiert.
 * Aufruf:
 * 
 * java -cp hbci4java-....jar \
 *   de.willuhn.jameica.hbci.passports.rdh.RDHNewDump \
 *   <Schluesseldatei> <Passwort>
 * 
 */
public class RDHNewDump
{

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception
  {
    if (args == null || args.length != 2)
    {
      System.err.println("Usage:");
      System.err.println("java -cp hbci4java-....jar \\" +
                         "\n  de.willuhn.jameica.hbci.passports.rdh.RDHNewDump \\" + 
                         "\n  <Schluesseldatei> <Passwort>\n");
      System.exit(1);
    }
    
    final byte[] CIPHER_SALT={(byte)0x26,(byte)0x19,(byte)0x38,(byte)0xa7,(byte)0x99,(byte)0xbc,(byte)0xf1,(byte)0x55};
    final int CIPHER_ITERATIONS=987;

    SecretKeyFactory fac=SecretKeyFactory.getInstance("PBEWithMD5AndDES");
    PBEKeySpec keyspec=new PBEKeySpec(args[1].toCharArray());
    SecretKey passportKey=fac.generateSecret(keyspec);
    keyspec.clearPassword();

    PBEParameterSpec paramspec=new PBEParameterSpec(CIPHER_SALT,CIPHER_ITERATIONS);
    Cipher cipher=Cipher.getInstance("PBEWithMD5AndDES");
    cipher.init(Cipher.DECRYPT_MODE,passportKey,paramspec);

    DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
    dbf.setValidating(false);
    DocumentBuilder db=dbf.newDocumentBuilder();

    Element root = null;
    CipherInputStream ci = null;
    try
    {
      ci = new CipherInputStream(new FileInputStream(args[0]),cipher);
      root=db.parse(ci).getDocumentElement();
    }
    catch (CharConversionException e1)
    {
      System.out.println("Passwort falsch (JDK 1.5+)");
    }
    catch (SAXException e)
    {
      System.out.println("Passwort falsch (bis JDK 1.4)");
      return;
    }
    finally
    {
      if (ci != null)
        ci.close();
    }

    TransformerFactory tfac=TransformerFactory.newInstance();
    Transformer tform=tfac.newTransformer();
    
    tform.setOutputProperty(OutputKeys.METHOD,"xml");
    tform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"no");
    tform.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
    tform.setOutputProperty(OutputKeys.INDENT,"yes");
    
    tform.transform(new DOMSource(root),new StreamResult(System.out));
  }

}


/*********************************************************************
 * $Log: RDHNewDump.java,v $
 * Revision 1.2  2011/01/17 17:32:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.3  2007/07/11 17:21:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2007/07/11 17:21:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2007/07/11 17:19:39  willuhn
 * @N Tool, um den XML-Inhalt einer Schluesseldiskette auf der Konsole auszugeben
 *
 **********************************************************************/