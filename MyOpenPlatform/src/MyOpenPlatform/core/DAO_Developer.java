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

import java.util.logging.Logger;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;

/**
 * Ensemble des m�thodes li�s � la gestion des comptes d�veloppeurs dans la base de donn�es
 * 
 * @author Herv� Hoareau
 */

public class DAO_Developer extends DAOBase {

	protected static final Logger log = Logger.getLogger(DAO_Developer.class.getName());
		
		
	/**
	 * Enregistrement de l'ensemble des classes n�cessaires pour la gestion des comptes d�veloppeurs
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
	public String addDeveloper(Developer d){
		ofy().put(d); //Enregistrement du compte d�veloppeur dans la base de donn�es
		//TODO il semble que le temps d'attente de cr�ation du d�veloppeur soit trop long
		
		//TODO r�int�grer la lecture de fichier type
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
	 * Recherche d'une m�thode dans la base de donn�es (la cl� �tant le plan tarifaire et l'url)
	 * @param url
	 * @param idPlan
	 * @return retourne la m�thode trouv�
	 */
	public Method findMethod(String url,String idPlan) {
		if(url==null || idPlan==null)return null;
		return(ofy().get(Method.class,idPlan+","+url));
	}

	/**
	 * Delete a developer account
	 * @param email si contient null on efface tous les d�veloppeurs
	 * @return false si aucun d�veloppeur trouv�, sinon true
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
	 * Effacement de l'ensemble des m�thodes (fonction � usage interne)
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
