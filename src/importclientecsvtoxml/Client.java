/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package importclientecsvtoxml;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;

public class Client  {
    private String dni;
    private String firstname;
    private String lastname;
    TreeMap <Long,String> localPhones;
    TreeMap <Long,String> internationalPhones;
    HashMap <String,String> emails;
    
    public Client(String dni,String firstname,String lastname) throws Exception {
        if (!verificaDNI(dni)) throw new Exception("DNI number not valid");
        Comparator comparePhones=new PhoneComparator();
        this.dni=dni;
        this.firstname=firstname;
        this.lastname=lastname;
        localPhones=new TreeMap <>(comparePhones);
        internationalPhones=new TreeMap <>(comparePhones);
        emails=new HashMap <> ();
    }
    
    public void addLocalPhone(String phone) throws Exception {
        try { 
            localPhones.put(Long.parseLong(phone),phone);
        } catch(NumberFormatException e) {
            throw new Exception("The Phone number "+phone+" is not valid");
        }
    }
    
    public void addInternationalPhone(String phone) throws Exception {
        String number=phone.substring(1);
        if (phone.charAt(0)!='+') 
                throw new Exception("The Phone number "+phone+" is not valid");
        try {
            internationalPhones.put(Long.parseLong(number),phone);
        } catch(NumberFormatException e) {
            throw new Exception("The Phone number "+phone+" is not valid");
        }
    }
    
    public void addEmail(String email) {
        emails.put(email.toLowerCase(),email);
    }
    
    public boolean verificaDNI(String dni) {
        return true;
    }
    
    public static Client scanCSVLine(String line) throws Exception, ScanException {
        Client cl=null;
        String[] fields=line.split(",");
        String f;
        
        if (fields.length < 3) throw new ScanException("Faltan datos");
        cl=new Client(fields[0].trim(),cleanQuotes(fields[1]),cleanQuotes(fields[2]));
        for(int idx=3;idx<fields.length;idx++) {
            f=fields[idx].trim();
            if (isEmail(f)) {
                cl.addEmail(f);
            } else {
                if (isPhoneNumber(f)) {
                    f=f.replace("(","").replace(")","");
                    if (isLocalPhoneNumber(f)) {
                        cl.addLocalPhone(f);
                    } else 
                        cl.addInternationalPhone(f);
                }
                else throw new ScanException("Unknown field "+f+" scanning "+line);
            }
        }
        return cl;
    }
    
    public String toString() {
        return dni+": "+firstname+" "+lastname;
    }
    
    public String toXMLString() {
        StringBuilder xml=new StringBuilder("<datos_cliente>");
        xml.append("<id>").append(dni).append("</id>");
        xml.append("<nombre>").append(firstname).append("</nombre>");
        xml.append("<apellidos>").append(lastname).append("</apellidos>");
        xml.append("<telefonos>");
        for(String tel:localPhones.values()) {
            xml.append("<telefono>").append(tel).append("</telefono>");
        }
        for(String tel:internationalPhones.values()) {
            xml.append("<telefono>").append(tel).append("</telefono>");
        }
        xml.append("</telefonos>");
        xml.append("<mails>");
        for(String mail:emails.values()) {
            xml.append("<mail>").append(mail).append("</mail>");
        }
        xml.append("</mails>");
        xml.append("</datos_cliente>");
        return xml.toString();
    }
    
    public Document toXMLDocument() {
        return null;
    }
    
    
    private static String cleanQuotes(String str) throws ScanException {
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find()) throw new ScanException("Error in quoted String "+str);
        return matcher.group(1).trim();
    }
    
    private static boolean isEmail(String field) {
        Pattern pattern=Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", 
                                         Pattern.CASE_INSENSITIVE);
        Matcher matcher=pattern.matcher(field);
        return matcher.find();
    }
        
    private static boolean isPhoneNumber(String field) {
        Pattern pattern=Pattern.compile("^\\+?(?:\\([0-9]+\\))?(?:[0-9]?){6,14}[0-9]$");
        Matcher matcher=pattern.matcher(field);
        return matcher.find();
    }

    private static boolean isLocalPhoneNumber(String field) {
        if (field.charAt(0)=='+') return false;
        return true;
    }


}
