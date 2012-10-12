package MyOpenPlatform.core;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.LocalVerifier;

/**
 * Generic class to several tools like error functions
 * @author Studio
 *
 */
public class Tools {
	/**Fonctions permettant de retourné des codes d'erreur correctement documenté
	 * 
	 * @param code contient le code d'erreur interne
	 * @param message contient message explicite
	 * @param doc permet de faire référence à une documentation en ligne pour le code, si null on utilise un lien prédéfinie avec le code d'erreur en signet
	 *
	 * @return retourne un chaine de caractère dans le formalisme json
	 * 
	 * @see http://www.restlet.org/documentation/2.0/firstResource
	 * @author Hervé Hoareau 
	 */
	public Response setError(String code,String doc,Response r){
		r.setStatus(Status.SERVER_ERROR_INTERNAL);
		String errorCode=System.getProperty(code);if(errorCode==null)errorCode=code;
		r.setEntity(getErrorCode(errorCode,doc), MediaType.TEXT_PLAIN);
		return(r);
	}
	
	/**
	 * A Function to simplify the response return 
	 * @param value
	 * @param r
	 * @return
	 */
	public Response setSuccess(String value,Response r) {
		r.setStatus(Status.SUCCESS_OK);
		r.setEntity(value, MediaType.TEXT_PLAIN);
		return(r);
	}

	
	public String getErrorCode(String code,String doc){	
		String message=System.getProperty("error"+code);
		if(message==null)message="Error code : "+code;
		if(doc==null)doc=(System.getProperty("ERROR_DOC_LINK")+"#"+code);
		
		String rc=("{\"code\":"+code+",\"message\":\""+message+"\",\"doc\":\""+doc+"\"}");
		return(rc);
	}
	
	
	
	/**
	 * Envoi d'un email depuis le serveur
	 * @param dest contient le destinataire du mail
	 * @param msgBody contiendra le corps du mail au format texte
	 * @param subject contiendra le sujet du mail
	 * @return retourne vrai si le mail à bien été expédié
	 * @author Hervé Hoareau 
	 */
	protected boolean SendMail(String dest,String msgBody,String subject){
		Properties props = new Properties();
	    Session session = Session.getDefaultInstance(props, null);
	
	    try {
	        Message msg = new MimeMessage(session);
	        msg.setFrom(new InternetAddress(System.getProperty("PLATFORM_ADDRESS"), "Example.com Admin"));
	        msg.addRecipient(Message.RecipientType.TO,new InternetAddress(dest, "Dear Developer"));
	        
	        msg.setSubject(subject);
	        msg.setText(msgBody);
	        Transport.send(msg);
	        
	        return true;
	
	    } catch (AddressException e) {
	        // ...
	    } catch (MessagingException e) {
	        // ...
	    } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return false;
	}


	
}
