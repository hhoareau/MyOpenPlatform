/*
 * Copyright (C) 2012 Hoareau Hervé

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
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.logging.Logger;
import javax.servlet.http.*;
import com.example.contribuez.servlet.DAO;


/**
 * Cette classe est appelée par les services de paiement pour confirmer l'achat
 * Ainsi le rechargement peut avoir lieu ici
 * 
 * cette fonction est appellée par les différentes solutions de paiement (Google Checkout, Paypal), la reconnaissance
 * de la plateforme se faisant par lecture des paramètres passés
 * 
 * 
 * 
 * PAYPAL payment
 * 
 * pour paypal voir : @see https://cms.paypal.com/us/cgi-bin/?cmd=_render-content&content_ID=developer/e_howto_admin_IPNIntro et 
 * le guide complet : @see https://www.paypalobjects.com/WEBSCR-640-20120914-1/fr_FR/FR/pdf/PP_OrderManagement_IntegrationGuide.pdf
 * un tutoriel en français est disponible ici : @see https://www.paypal.com/fr/cgi-bin/webscr?cmd=p/acc/ipn-info-outside
 *  
 * You must declared the callback payment web service to paypal :
 * @see https://www.paypal.com/cgi-bin/customerprofileweb?cmd=_profile-ipn-notify
 * with this url : https://<your_domain>.appspot.com/callback?secret=<your paypal_secret key in appengine-web.xml
 * for example we use : https://myopenplatform.appspot.com/callback?secret=myopenplatform
 * the  https://myopenplatform.appspot.com/callback must be declared as a servlet in your web.xml file for appengine.
 *   
 * 
 *  
 *  BITCOIN payment
 *  
 *  Cette classe permet également de receptionner des bitcoins  @see https://en.bitcoin.it/wiki/Merchant_Howto
 *  dans cette version, je propose l'usage de http://bitping.net/
 *  Il necessite de générer des adresses de réception des bitcoin via https://github.com/downloads/jackjack-jj/pywallet/PWI_0.0.3.exe
 *  
 * @author Hervé Hoareau
 *
 */

public class callback extends HttpServlet {

	private static final long serialVersionUID = 5657373030317049439L;
	protected final static String CREDIT_PACKAGE=System.getProperty("paypal_credit_package");
		
	/*
	 * voir http://code.google.com/p/objectify-appengine/wiki/BestPractices#Utilisez_un_DAO
	 * sur la justification de l'usage d'un objet DAO statique
	 */
	protected static DAO dao;
	protected static final Logger log = Logger.getLogger(callback.class.getName());
	
	private String paramToString(HttpServletRequest req) {
       
		String rc="";
		
		@SuppressWarnings("rawtypes")
		Enumeration e=req.getParameterNames();
		while(e.hasMoreElements()){
			String sParameter=(String) e.nextElement();
			rc=rc+sParameter+"="+URLEncoder.encode(req.getParameter(sParameter))+"&";
		}
		
        return rc.substring(0, rc.length()-1);       
    }
	
	
	public void doGet(final HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		resp.setHeader("Location",System.getProperty("buying_page"));
		resp.getWriter().print("Redirection");
	};
	
	/**
	 * Méthode appelée par PayPal ou BitCoin
	 */
	public void doPost(final HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String rc="";
		log.warning("parametres="+paramToString(req));
		
		String origine=req.getRemoteAddr(); //Variable nécessaire pour identifier l'origine du callback
		log.warning("origine platforme="+origine);
		
		dao=new DAO();
		
		/**
		 *Bitcoin payments from BitPing.Net
		 * @see exemple http://bitping.net/member_api.php
		 */
		if(origine.contains(System.getProperty("bitcoin_platform"))){
			String address=req.getParameter("address");
			String amount=req.getParameter("amount");
			String btc=req.getParameter("btc");
			String confirmations=req.getParameter("confirmations");
			String txhash=req.getParameter("txhash");
			String block=req.getParameter("block");
			String sig=req.getParameter("signature");
			
			MessageDigest md=null;
			try {
				md=MessageDigest.getInstance("SHA1");
				md.update(new String(address+amount+btc+confirmations+txhash+block+System.getProperty("bitcoin_secretkey")).getBytes());
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(md.digest()==sig.getBytes()){
				log.warning("Ajout des crédits");
				//TODO mettre en place l'ajout des crédits
			}
		}

		/**
		 * Callback treatment for PayPal payment
		 * @see https://www.paypalobjects.com/WEBSCR-640-20120914-1/fr_FR/FR/pdf/PP_OrderManagement_IntegrationGuide.pdf
		 */
		//la notification vient de paypal
		String secret=req.getParameter("secret");
		final String quantity=req.getParameter("quantity");
	
		if(secret.equals(System.getProperty("paypal_secretkey"))){
			if(req.getParameter("address_country")!=null){

				String payment_status=req.getParameter("payment_status");
				String payer_email=req.getParameter("payer_email");
				final String txn_id =req.getParameter("txn_id");
				
				final Developer d=dao.findDeveloper(payer_email, false); //Developer identification by payer_email send by paypal
				if(d==null)
					resp.setStatus(resp.SC_INTERNAL_SERVER_ERROR,System.getProperty("ERROR_DEVELOPER_UNKNOWN"));
				else
					if(req.getParameter("payment_status").equals("Completed")){
						//Extrait le nombre de crédits en fonction de l'item
						final String item=req.getParameter("option_selection1"); //Idenfication du pack de crédits
						if(item!=null){
							/**
							 * Paypal require à post request with all the parameters with cmd=_notify-validate
							 * there is a lot of problem about the request of PAYPAL. Paypal seems to be very sensible
							 * about the parameters end the encoding.
							 * @see http://www.webmasterworld.com/ecommerce/4292847.htm
							 */
							
							String param="cmd=_notify-validate&"+this.paramToString(req);
							
							new RestCall(System.getProperty("paypal_callback"),param){ //POST call
								@Override public void onSuccess(String rep) {
									log.info("succes du POST paypal rep="+rep);
									
									if(dao.findTransaction(txn_id)!=null){
										log.warning("this transaction has already used");
									} else {
										int i=CREDIT_PACKAGE.indexOf(item+"=")+item.length()+1; //Find item in database
										String sCredits=(String) CREDIT_PACKAGE.substring(i, callback.CREDIT_PACKAGE.indexOf(",",i));									
										int newCredits=Integer.parseInt(sCredits)*Integer.parseInt(quantity);
														
										d.setCredits(d.getCredits()+newCredits); 										//Add credits to the developer account
										dao.ofy().put(new Transaction(d.getEmail(),"paypal",txn_id)); 	//Add the transaction in the log
										dao.ofy().put(d); 																				//Update developer account in database									
									}
									
								}; 
						
								@Override public void onFailure(int reponseCode) {
									log.warning("failure du POST paypal rep="+reponseCode);
								}
								
							};						
						} else log.warning("Article unknown");
					}
			}
		}
		
		resp.setContentType("text/plain");
		resp.getWriter().println(rc);	
	}
}
