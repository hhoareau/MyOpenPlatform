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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.*;

import org.apache.jasper.util.Enumerator;
import org.restlet.Context;

import com.example.contribuez.servlet.DAO;
import com.sun.xml.internal.fastinfoset.Encoder;



/**
 * Cette classe est appelée par les services de paiement pour confirmer l'achat
 * Ainsi le rechargement peut avoir lieu ici
 * 
 * cette fonction est appellée par les différentes solutions de paiement (Google Checkout, Paypal), la reconnaissance
 * de la plateforme se faisant par lecture des paramètres passés
 * 
 *  pour paypal voir : @see https://cms.paypal.com/us/cgi-bin/?cmd=_render-content&content_ID=developer/e_howto_admin_IPNIntro et 
 *  le guide complet : @see https://www.paypalobjects.com/WEBSCR-640-20120914-1/fr_FR/FR/pdf/PP_OrderManagement_IntegrationGuide.pdf
 *  un tutoriel en français est disponible ici : @see https://www.paypal.com/fr/cgi-bin/webscr?cmd=p/acc/ipn-info-outside
 *  
 *  pour paypal il est nécessaire d'activer l'appel de cette methode callback pour les transactions ici 
 *  @see https://www.paypal.com/cgi-bin/customerprofileweb?cmd=_profile-ipn-notify
 *  
 *  Les parametres envoyés par paypal sont les suivants :
 *  com.example.contribuez.servlet.callback doPost: {residence_country=[Ljava.lang.String;@170a650, shipping_discount=[Ljava.lang.String;@b6f7f5, address_city=[Ljava.lang.String;@5113f0, payer_id=[Ljava.lang.String;@f41393, first_name=[Ljava.lang.String;@314585, shipping=[Ljava.lang.String;@cb1edc, btn_id=[Ljava.lang.String;@1570f5c, txn_id=[Ljava.lang.String;@b18494, quantity=[Ljava.lang.String;@773d03, receiver_email=[Ljava.lang.String;@46a09b, custom=[Ljava.lang.String;@da5bc0, payment_date=[Ljava.lang.String;@1bdbf9d, option_name1=[Ljava.lang.String;@6f19d5, address_country_code=[Ljava.lang.String;@91520, charset=[Ljava.lang.String;@4a0ac5, payment_gross=[Ljava.lang.String;@1092447, address_zip=[Ljava.lang.String;@12cd8d4, ipn_track_id=[Ljava.lang.String;@14f5021, discount=[Ljava.lang.String;@15da7d, tax=[Ljava.lang.String;@bb6255, item_name=[Ljava.lang.String;@34f445, address_name=[Ljava.lang.String;@90ed81, last_name=[Ljava.lang.String;@d8c3ee, item_number=[Ljava.lang.String;@1277a30, shipping_method=[Ljava.lang.String;@c707c1, verify_sign=[Ljava.lang.String;@ce3b62, insurance_amount=[Ljava.lang.String;@19ccb73, address_country=[Ljava.lang.String;@f12b72, business=[Ljava.lang.String;@15b6aad, address_status=[Ljava.lang.String;@b890dc, payment_status=[Ljava.lang.String;@12e7cb6, protection_eligibility=[Ljava.lang.String;@fd9b97, transaction_subject=[Ljava.lang.String;@1f1e666, payer_email=[Ljava.lang.String;@4d40df, notify_version=[Ljava.lang.String;@1de041e, txn_type=[Ljava.lang.String;@e05ad6, payer_status=[Ljava.lang.String;@16bb7d9, mc_gross=[Ljava.lang.String;@f346dc, mc_currency=[Ljava.lang.String;@1b14530, option_selection1=[Ljava.lang.String;@135877f, address_state=[Ljava.lang.String;@152b6f5, pending_reason=[Ljava.lang.String;@169a50b, handling_amount=[Ljava.lang.String;@d297c0, secret=[Ljava.lang.String;@1e8e5a7, payment_type=[Ljava.lang.String;@13d422d, address_street=[Ljava.lang.String;@c3d062}
 *  
 *  
 *  
 *  Cette classe permet également de receptionner des bitcoin
 *  @see https://en.bitcoin.it/wiki/Merchant_Howto
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
		Enumeration e=req.getParameterNames();
		while(e.hasMoreElements()){
			String sParameter=(String) e.nextElement();
			try {
				rc=rc+sParameter+"="+URLDecoder.decode(req.getParameter(sParameter), "UTF-8")+"&";
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
		 * Ci-dessous l'implémentation de récéption des paiements paypal
		 * @see https://www.paypalobjects.com/WEBSCR-640-20120914-1/fr_FR/FR/pdf/PP_OrderManagement_IntegrationGuide.pdf
		 */
		//la notification vient de paypal
			String secret=req.getParameter("secret");
			if(secret.equals(System.getProperty("paypal_secretkey"))){
				//Identification du développeur par l'adresse email du compte paypal
				final Developer d=dao.findDeveloper(req.getParameter("payer_email"), false);
				if(d==null)
					resp=dao.setErrorCode(System.getProperty("ERROR_DEVELOPER_UNKNOWN"),null,resp);
				else
					if(req.getParameter("payment_status").equals("Completed")){
						//Extrait le nombre de crédits en fonction de l'item
						String item=req.getParameter("option_selection1"); //Idenfication du pack de crédits
						
						int i=CREDIT_PACKAGE.indexOf(item+"=")+item.length()+1;
						String sCredits=(String) CREDIT_PACKAGE.substring(i, callback.CREDIT_PACKAGE.indexOf(",",i));
						final int newCredits=Integer.parseInt(sCredits)*Integer.parseInt(req.getParameter("quantity"));
						
						//Paypal require à post request with all the parameters with cmd=_notify-validate
						String param=this.paramToString(req)+"&cmd=_notify-validate";
						new RestCall(System.getProperty("paypal_callback"),param){
							public void onSuccess(String rep) {
								log.warning("succes du POST paypal rep="+rep);
								
								d.setCredits(d.getCredits()+newCredits); //Add credits to the developer account
								dao.putDeveloper(d); //update account in database
							}; 
							public void onFailure(int reponseCode) {}
						};
					}	
			}
		
		
		resp.setContentType("text/plain");
		resp.getWriter().println(rc);	
	}
}
