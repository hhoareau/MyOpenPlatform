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
 * Ensemble des m�thodes li�s � la gestion des comptes d�veloppeurs dans la base de donn�es
 * 
 * @author Herv� Hoareau
 */

public class DAO_Developer extends DAOBase {

	protected static final Logger log = Logger.getLogger(DAO_Developer.class.getName());
	
	//Ensemble des constantes li� � la gestion des appels d'API
	//public static final int CALL_OK = 30;					//Le d�veloppeur peut utiliser l'API (apr�s test des quotas et des cr�dits)
	//public static final int CREDITS_KO = 31;				//Pas suffisament de cr�dit pour appeller l'API
	//public static final String QUOTA_KO = "32";					//Quota d'appels d�pass�
	//public static final String ERROR_DEVELOPER_UNKNOWN = "40"; 	//Developer inconnu
	//public static final String ERROR_BAD_SECRETKEY = "10";
	//public static final String ERROR_BAD_CAPTCHA="100";			
	
	//Adresse de la documentation des erreurs
	private static final String DOC_LINK = "http://myopenplatform.appspot.com/errorCode.html#"; 
		
		
	/**
	 * Enregistrement de l'ensemble des classes n�cessaires pour la gestion des comptes d�veloppeurs
	 * @see http://code.google.com/p/objectify-appengine/wiki/BestPractices#Utilisez_un_DAO
	 */
	static {
		ObjectifyService.register(Developer.class);
		ObjectifyService.register(Method.class);
		ObjectifyService.register(MethodCall.class);
	}	

	
	public DAO_Developer(){}
	
	/**
	 * Fonction principale appel�e � chaque utilisation d'une API, cette fonction
	 * 1. Identifie la m�thode appell�e
	 * 2. calcul le cout en fonction du plan tarifaire appliqu� au d�veloppeur
	 * 3. V�rifie les quotas d'appels
	 * 3. V�rifie et d�cr�mente le compteur du d�veloppeur
	 * 
	 * @param devCaller D�veloppeur ayant solicit� l'API 
	 * @param url url de la m�thode invoqu� par l'application du d�veloppeur
	 * @return Vrai si toutes les conditions sont ok (quota, cr�dit) pour d�clencher le process m�tier, False sinon
	 */
	public String AddApiCall(Developer devCaller,String url){

		url=url.replace("/rest/", "");
		
		//Retrouve la m�thode
		Method m=this.findMethod(url,devCaller.getIdPlan());
		//R�cup�ration du nombre d'appel depuis le d�but du mois
		MethodCall mc=ofy().query(MethodCall.class).filter("idMethod",m.getId()).filter("idDeveloper",devCaller.getEmail()).get();
		if(mc==null){
			//Premier appel du d�veloppeur
			mc=new MethodCall(m.getId(),devCaller.getEmail());
		}
		
		/**
		 * Si le quota par jour n'a pas �t� r�initialiser depuis 24 heures
		 */
		Long now=new Date().getTime();
		if(devCaller.getRazDate()-now>24*3600*1000L){ 
			for(MethodCall call:ofy().query(MethodCall.class).filter("idDevelopeur", devCaller.getEmail()).list()) call.setHits(0);	
			devCaller.setRazDate(now);
		}
		
		//Controle que le nombre de hits op�r� par le d�velopeur respect le quota
		if(mc.getHits()>m.getQuota())
			return(System.getProperty("ERROR_QUOTA_KO"));
		/*
		 * Contr�le des cr�dits
		 */
		Integer nbCredits=m.getCost(mc.getHits()); //R�cup�ration du prix de la m�thode en fonction du nombre d'appel
		if(devCaller.chkCredits(nbCredits)){
			mc.AddCall();				//Ajout d'un appel d'API dans l'historique
			ofy().put(mc);				//Enregistrement de l'historique
			putDeveloper(devCaller); 	//Le compte du d�veloppeur ayant �t� mise a jour, on l'enregistre dans la BDD
			return(System.getProperty("ERROR_CALL_OK"));
		}
		else return(System.getProperty("ERROR_CREDITS_KO"));
	}
	

	/**Recherche d'un compte developpeur dans la base
	 * 
	 * @param token est soit le token soit l'adresse email
	 * @param onlyToken � vrai si la recherche doit �tre limit� au token
	 * @return null si le compte d�veloppeur n'est pas trouv�, le compte sinon
	 */
	public Developer findDeveloper(String token,boolean onlyToken) {
		if(token==null)return null;
		
		if(token.contains("@") && !onlyToken)
			return(ofy().query(Developer.class).filter("email",token).get());
		else
			return(ofy().query(Developer.class).filter("token",token).get());
	}
	
	
	/**
	 * Enregistrement du d�veloppeur dans la base
	 * @param d Developpeur � enregistrer
	 * @return Identifiant du d�veloppeur (email)
	 */
	public String putDeveloper(Developer d){
		ofy().put(d); //Enregistrement du compte d�veloppeur dans la base de donn�es
		
		//TODO r�int�grer la lecture de fichier type
		//String sFile=FileToString("/WEB-INF/welcomeletter.html","Token="+d.getToken()+",Email="+d.getEmail());
		
		String sFile="Congratulations <email> ! You've just created an account. Your token is <token>\n You must use Basic Authentification"
						+ " to use the <portalname> API\n";
						
		sFile=sFile.replace("<email>", d.getEmail()).replace("<token>",d.getToken())
				.replace("<portalname>",System.getProperty("PORTALNAME"));
		
		
		if(sFile!=null)SendMail(d.getEmail(),sFile,null);

		return(d.getEmail());
	}
	
	/**
	 * Recherche d'une m�thode dans la base de donn�es (la cl� �tant le plan tarifaire et l'url)
	 * @param url
	 * @param idPlan
	 * @return retourne la m�thode trouv�
	 */
	private Method findMethod(String url,String idPlan) {
		if(url==null || idPlan==null)return null;
		return(ofy().get(Method.class,idPlan+","+url));
	}

	/**
	 * 
	 * @param email si contient null on efface tous les d�veloppeurs
	 * @return false si aucun d�veloppeur trouv�, sinon true
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
				//Delete les appels de m�thode du d�veloppeur supprim�
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
	 * Effacement de l'ensemble des m�thodes (fonction � usage interne)
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
	 * @return retourne vrai si le mail � bien �t� exp�di�
	 * @author Herv� Hoareau 
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


	/**Fonctions permettant de retourn� des codes d'erreur correctement document�
	 * 
	 * @param code contient le code d'erreur interne
	 * @param message contient message explicite
	 * @param doc permet de faire r�f�rence � une documentation en ligne pour le code, si null on utilise un lien pr�d�finie avec le code d'erreur en signet
	 *
	 * @return retourne un chaine de caract�re dans le formalisme json
	 * 
	 * @see http://www.restlet.org/documentation/2.0/firstResource
	 * @author Herv� Hoareau 
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
