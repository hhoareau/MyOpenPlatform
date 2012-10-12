/*
 * Copyright (C) 2012 Hoareau Herv�

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package MyOpenPlatform.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import javax.servlet.http.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import com.google.api.client.util.Base64;

/**
 * Cette servlet est appell�e par facebook pour 
 * - transmettre l'email des inscrits via facebook
 * 
 * @author Herv� Hoareau
 */

@SuppressWarnings("serial")
public class facebookCallback extends HttpServlet {
	
	//Renforce la s�curit� entre le serveur et la plateforme de paiement
	//pour paypal voir https://www.paypalobjects.com/WEBSCR-640-20120914-1/fr_FR/FR/pdf/PP_OrderManagement_IntegrationGuide.pdf
	
	/**HmacSHA256 implementation
	 *  
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 * @see http://www.sergiy.ca/how-to-implement-facebook-oauth-2.0-app-authorization-process-in-java/
	 */
    private String hmacSHA256(String data, String key) {
        Mac mac;
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
			mac = Mac.getInstance("HmacSHA256");
			  mac.init(secretKey);
		       byte[] hmacData = mac.doFinal(data.getBytes("UTF-8"));
		       return new String(hmacData);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      return null;
        
    }	
	
	/*
	 * voir http://code.google.com/p/objectify-appengine/wiki/BestPractices#Utilisez_un_DAO
	 * sur la justification de l'usage d'un objet DAO statique
	 */
	protected static DAO_Developer dao=new DAO_Developer();
	protected static final Logger log = Logger.getLogger(facebookCallback.class.getName());
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)		throws IOException {
	
		/**D�codage de la r�ponse de facebook
		/*@see http://www.sergiy.ca/how-to-implement-facebook-oauth-2.0-app-authorization-process-in-java/
		 */
		String[] signedRequest = req.getParameter("signed_request").split("\\.", 2);
		//String sig = new String(Base64.decode(signedRequest[0].getBytes("UTF-8")));

		/*
	    if(!hmacSHA256(signedRequest[1], facebookCallback.FACEBOOK_SECRETKEY).equals(sig)) {
            log.warning("signature is not correct, possibly the data was tampered with");
            return;
        }*/
		
		String rep=new String(Base64.decode(signedRequest[1].getBytes("UTF-8")))+"}";
		log.warning("rep="+rep);
		//log.warning("sig="+sig);
		
		JsonNode js=new ObjectMapper().readTree(rep);
		String email=js.get("registration").get("email").asText();
		dao.putDeveloper(new Developer(email,"facebook",""));
		
		resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
		resp.sendRedirect("/index.html");
		resp.getWriter().println("Login registered");	
	}
}
