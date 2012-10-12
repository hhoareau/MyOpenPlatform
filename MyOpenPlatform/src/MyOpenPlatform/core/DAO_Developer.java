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

import java.util.logging.Logger;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;

/**
 * Ensemble des méthodes liés à la gestion des comptes développeurs dans la base de données
 * 
 * @author Hervé Hoareau
 */

public class DAO_Developer extends DAOBase {

	protected static final Logger log = Logger.getLogger(DAO_Developer.class.getName());
		
		
	/**
	 * Enregistrement de l'ensemble des classes nécessaires pour la gestion des comptes développeurs
	 * @see http://code.google.com/p/objectify-appengine/wiki/BestPractices#Utilisez_un_DAO
	 */
	static {
		ObjectifyService.register(Developer.class);
		ObjectifyService.register(Method.class);
		ObjectifyService.register(MethodCall.class);
		ObjectifyService.register(Transaction.class);
	}	

	
	public DAO_Developer(){
		super();
		ObjectifyService.begin();
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
	public String addDeveloper(Developer d){
		ofy().put(d); //Enregistrement du compte développeur dans la base de données
		//TODO il semble que le temps d'attente de création du développeur soit trop long
		
		//TODO réintégrer la lecture de fichier type
		//String sFile=FileToString("/WEB-INF/welcomeletter.html","Token="+d.getToken()+",Email="+d.getEmail());
		
		String sFile="Congratulations <email> ! \n\n Your account on <portalname> is opened. Your password to loggin in it is <password> and "
						+ "your token to use with the API is <token>\n You must use Basic Authentification protocol.\n\n" 
						+ "we offer <credit_account> credits to develope and test your application for free. "
						+ "If you need more credits, you can buy here : <buy_page>\n\n"
						+ "The <portalname> team.\n<platform_support>\n";
						
		sFile=sFile.replace("<email>", d.getEmail())
				.replace("<token>",d.getToken())
				.replace("<credit_account>", d.getCredits().toString())
				.replace("<buy_page>", System.getProperty("buying_page"))
				.replace("<portalname>",System.getProperty("PORTALNAME"))
				.replace("<platform_support>",System.getProperty("PLATFORM_SUPPORT"))
				.replace("<password>",d.getPassword());
		
		String subject="Your <portalname> account is created";
		subject=subject.replace("<portalname>",System.getProperty("PORTALNAME"));
		
		
		if(sFile!=null)new Tools().SendMail(d.getEmail(),sFile,subject);

		return(d.getEmail());
	}
	
	/**
	 * Recherche d'une méthode dans la base de données (la clé étant le plan tarifaire et l'url)
	 * @param url
	 * @param idPlan
	 * @return retourne la méthode trouvé
	 */
	public Method findMethod(String url,String idPlan) {
		if(url==null || idPlan==null)return null;
		return(ofy().get(Method.class,idPlan+","+url));
	}

	/**
	 * Delete a developer account
	 * @param email si contient null on efface tous les développeurs
	 * @return false si aucun développeur trouvé, sinon true
	 */
	public boolean delDeveloper(String email) {
		if(email==null){
			//Delete all developer accounts with MethodCall attached
			ofy().delete(ofy().query(Developer.class));
			ofy().delete(ofy().query(MethodCall.class));
			return true;
		} else {
			Developer d=findDeveloper(email,false);
			if(d!=null){
				ofy().delete(ofy().query(MethodCall.class).filter("idDeveloper", d.getEmail())); 				//Delete method called by the developer
				ofy().delete(d);
				
				return(true);
			}
			else return(false);			
		}
	}

	/**
	 * Add a new method to expose
	 * @param plan 	name of the tariff plan to use for this method
	 * @param url method path without version
	 * @param tarifs credit cost of one hit 	
	 * @param quotaByDay hits quota by day
	 */
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

	public Object findTransaction(String txn_id) {
		return ofy().query(Transaction.class).filter("idTransaction",txn_id).get();
	}

	
	public String ShowDeveloper(Developer d){
		String rc=d.toString();
		
		for(Method m:ofy().query(Method.class).list())
			if(m.getPlan().equals(d.getIdPlan()))rc+="\n"+m.toString();
		
		return(rc);
	}
	
	
	
	
	


}
