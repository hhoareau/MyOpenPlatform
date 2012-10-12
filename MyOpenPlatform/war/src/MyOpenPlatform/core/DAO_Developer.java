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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;

/**
 * Ensemble des méthodes liés à la gestion des comptes développeurs dans la base de données
 * 
 * @author Hervé Hoareau
 */

public class DAO_Developer extends DAOBase {

	protected static final Logger log = Logger.getLogger(DAO_Developer.class.getName());
	
	//Ensemble des constantes lié à la gestion des appels d'API
	//public static final int CALL_OK = 30;					//Le développeur peut utiliser l'API (aprés test des quotas et des crédits)
	//public static final int CREDITS_KO = 31;				//Pas suffisament de crédit pour appeller l'API
	//public static final String QUOTA_KO = "32";					//Quota d'appels dépassé
	//public static final String ERROR_DEVELOPER_UNKNOWN = "40"; 	//Developer inconnu
	//public static final String ERROR_BAD_SECRETKEY = "10";
	//public static final String ERROR_BAD_CAPTCHA="100";			
	
	//Adresse de la documentation des erreurs
	private static final String DOC_LINK = "http://myopenplatform.appspot.com/errorCode.html#"; 
		
		
	/**
	 * Enregistrement de l'ensemble des classes nécessaires pour la gestion des comptes développeurs
	 * @see http://code.google.com/p/objectify-appengine/wiki/BestPractices#Utilisez_un_DAO
	 */
	static {
		ObjectifyService.register(Developer.class);
		ObjectifyService.register(Method.class);
		ObjectifyService.register(MethodCall.class);
	}	

	
	public DAO_Developer(){}
	
	/**
	 * Fonction principale appelée à chaque utilisation d'une API, cette fonction
	 * 1. Identifie la méthode appellée
	 * 2. calcul le cout en fonction du plan tarifaire appliqué au développeur
	 * 3. Vérifie les quotas d'appels
	 * 3. Vérifie et décrémente le compteur du développeur
	 * 
	 * @param devCaller Développeur ayant solicité l'API 
	 * @param url url de la méthode invoqué par l'application du développeur
	 * @return Vrai si toutes les conditions sont ok (quota, crédit) pour déclencher le process métier, False sinon
	 */
	public String AddApiCall(Developer devCaller,String url){

		url=url.replace("/rest/", "");
		
		//Retrouve la méthode
		Method m=this.findMethod(url,devCaller.getIdPlan());
		//Récupération du nombre d'appel depuis le début du mois
		MethodCall mc=ofy().query(MethodCall.class).filter("idMethod",m.getId()).filter("idDeveloper",devCaller.getEmail()).get();
		if(mc==null){
			//Premier appel du développeur
			mc=new MethodCall(m.getId(),devCaller.getEmail());
		}
		
		/**
		 * Si le quota par jour n'a pas été réinitialiser depuis 24 heures
		 */
		Long now=new Date().getTime();
		if(devCaller.getRazDate()-now>24*3600*1000L){ 
			for(MethodCall call:ofy().query(MethodCall.class).filter("idDevelopeur", devCaller.getEmail()).list()) call.setHits(0);	
			devCaller.setRazDate(now);
		}
		
		//Controle que le nombre de hits opéré par le dévelopeur respect le quota
		if(mc.getHits()>m.getQuota())
			return(System.getProperty("ERROR_QUOTA_KO"));
		/*
		 * Contrôle des crédits
		 */
		Integer nbCredits=m.getCost(mc.getHits()); //Récupération du prix de la méthode en fonction du nombre d'appel
		if(devCaller.chkCredits(nbCredits)){
			mc.AddCall();				//Ajout d'un appel d'API dans l'historique
			ofy().put(mc);				//Enregistrement de l'historique
			putDeveloper(devCaller); 	//Le compte du développeur ayant été mise a jour, on l'enregistre dans la BDD
			return(System.getProperty("ERROR_CALL_OK"));
		}
		else return(System.getProperty("ERROR_CREDITS_KO"));
	}
	

	/**Recherche d'un compte developpeur dans la base
	 * 
	 * @param token est soit le token soit l'adresse email
	 * @param onlyToken à vrai si la recherche doit être limité au token
	 * @return null si le compte développeur n'est pas trouvé, le compte sinon
	 */
	public Developer findDeveloper(String token,boolean onlyToken) {
		if(token==null)return null;
		
		if(token.contains("@") && !onlyToken)
			return(ofy().query(Developer.class).filter("email",token).get());
		else
			return(ofy().query(Developer.class).filter("token",token).get());
	}
	
	
	/**
	 * Enregistrement du développeur dans la base
	 * @param d Developpeur à enregistrer
	 * @return Identifiant du développeur (email)
	 */
	public String putDeveloper(Developer d){
		ofy().put(d); //Enregistrement du compte développeur dans la base de données
		
		//TODO réintégrer la lecture de fichier type
		//String sFile=FileToString("/WEB-INF/welcomeletter.html","Token="+d.getToken()+",Email="+d.getEmail());
		
		String sFile="Congratulations <email> ! You've just created an account. Your token is <token>\n You must use Basic Authentification"
						+ " to use the <portalname> API\n";
						
		sFile=sFile.replace("<email>", d.getEmail()).replace("<token>",d.getToken())
				.replace("<portalname>",System.getProperty("PORTALNAME"));
		
		
		if(sFile!=null)SendMail(d.getEmail(),sFile,null);

		return(d.getEmail());
	}
	
	/**
	 * Recherche d'une méthode dans la base de données (la clé étant le plan tarifaire et l'url)
	 * @param url
	 * @param idPlan
	 * @return retourne la méthode trouvé
	 */
	private Method findMethod(String url,String idPlan) {
		if(url==null || idPlan==null)return null;
		return(ofy().get(Method.class,idPlan+","+url));
	}

	/**
	 * 
	 * @param email si contient null on efface tous les développeurs
	 * @return false si aucun développeur trouvé, sinon true
	 */
	public boolean delDeveloper(String email) {
		if(email==null){
			ofy().delete(ofy().query(Developer.class));
			ofy().delete(ofy().query(MethodCall.class));
			return true;
		} else {
			Developer d=findDeveloper(email,false);
			if(d!=null){
				ofy().delete(d);
				//Delete les appels de méthode du développeur supprimé
				ofy().delete(ofy().query(MethodCall.class).filter("idDeveloper", d.getEmail()));
				return(true);
			}
			else return(false);			
		}
	}

	
	public void addMethod(String plan,String url,String tarifs,Integer quotaByDay){
		ofy().put(new Method(url,plan,tarifs,quotaByDay));
	}

	/**
	 * Effacement de l'ensemble des méthodes (fonction à usage interne)
	 */
	
	public void razMethod() {
		ofy().delete(ofy().query(MethodCall.class));
		ofy().delete(ofy().query(Method.class));
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
	public Response setErrorCode(String code,String doc,Response response){
		response.setStatus(Status.SERVER_ERROR_INTERNAL);
		response.setEntity(getErrorCode(code,doc), MediaType.TEXT_PLAIN);
		return(response);
	}
	
	public HttpServletResponse setErrorCode(String code,String doc,HttpServletResponse response){
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,getErrorCode(code,doc));
		return(response);
	}
	
	public String getErrorCode(String code,String doc){	
		String message=null;
		if(code==System.getProperty("ERROR_CREDITS_KO"))				message="Insuficient credit";
		if(code==System.getProperty("ERROR_QUOTA_KO"))				message="Insuficient quota";
		
//		if(code==DAO_Developer.QUOTA_KO)				message="Insuficient quota";
//		if(code==DAO_Developer.ERROR_DEVELOPER_UNKNOWN)	message="Developer unkown";
//		if(code==DAO_Developer.ERROR_BAD_SECRETKEY)		message="Bad secret key";
//		if(code==DAO_Developer.ERROR_BAD_CAPTCHA)		message="Bad captcha";
		
		if(message=="")message="Error code : "+code;
		
		if(doc==null)doc=""+code;
		String rc=("{\"code\":"+code+",\"message\":\""+message+"\",\"doc\":\""+this.DOC_LINK+"\\"+doc);
		log.warning("Code erreur : "+rc);
		return(rc);
	}
	


}
