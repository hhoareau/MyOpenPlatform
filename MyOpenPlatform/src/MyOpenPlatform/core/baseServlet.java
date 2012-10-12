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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

import backup.Seller_Info;
import com.example.contribuez.servlet.DAO;


/**
 * Cette classe regroupe l'ensemble des méthodes utilisables par les classes
 * implémentant la gestion REST des objets de la base de données
 * 
 * @author Hervé Hoareau
 */

public class baseServlet extends ServerResource {
 
	protected static final Logger log = Logger.getLogger(baseServlet.class.getName());
	
	/**Données nécessaire au paiement sur google checkout
	 * @see http://support.google.com/checkout/sell/bin/answer.py?hl=en&answer=42963
	 */
	Seller_Info seller = new Seller_Info();
	
	/*
	 * voir http://code.google.com/p/objectify-appengine/wiki/BestPractices#Utilisez_un_DAO
	 * sur la justification de l'usage d'un objet DAO statique
	 */
	protected static DAO dao;
	protected static Tools ts;
	protected int hitCost;						//Cost for each call
	protected Developer devCaller; 	//Caller of the Api
	protected MethodCall mc;
	protected int cost;
	
	public baseServlet(){
		dao=new DAO();
		ts=new Tools();
	}
	
	public void addCall(){
		if(cost>0){
			//log.warning("Credit="+devCaller.getCredits()+" cost="+cost);
			devCaller.setCredits(devCaller.getCredits()-cost);
			dao.ofy().put(devCaller);
		}
		
		mc.AddCall();
		dao.ofy().put(mc);
	}
	
	
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
	public boolean chkApiCall(String idMethod){
		devCaller=dao.findDeveloper(this.getChallengeResponse().getRawValue(),true);
		
		//To find the plan of an API, we redraw the prefixe and the version
		Method m=dao.findMethod(idMethod,devCaller.getIdPlan());		//Find the method in the database
		
		//Get the hit number of a specific method
		mc=dao.ofy().query(MethodCall.class).filter("idMethod",m.getId()).filter("idDeveloper",devCaller.getEmail()).get();
		if(mc==null){
			//Premier appel du développeur
			mc=new MethodCall(m.getId(),devCaller.getEmail());
		}
		
		/**
		 * Si le quota par jour n'a pas été réinitialiser depuis 24 heures
		 */
		Long now=new Date().getTime();
		if(devCaller.getRazDate()-now>24*3600*1000L){ 
			for(MethodCall call:dao.ofy().query(MethodCall.class).filter("idDevelopeur", devCaller.getEmail()).list()) call.setHits(0);	
			devCaller.setRazDate(now);
		}
		
		//Controle que le nombre de hits opéré par le dévelopeur respect le quota
		if(mc.getHits()>m.getQuota()){
			setResponse(ts.setError("ERROR_QUOTA_KO", null, getResponse()));
			return false;
		}
			

		//Contrôle des crédits 
		cost=m.getCost(mc.getHits()); //Récupération du prix de la méthode en fonction du nombre d'appel
		if(cost==0 || devCaller.chkCredits(cost)){
			//setResponse(ts.setSuccess("ERROR_CALL_OK", getResponse()));
			return true;
		}else{
			setResponse(ts.setError("ERROR_CREDITS_KO", null, getResponse()));
			return false;
		}
	}
	
	
	
	
	
	/**
	 * Convertit un objet au format JSON
	 * @param objet : contient n'importe quel objet ou liste d'objets à convertire
	 * @return String contenant l'objet au format json (voir http://wiki.fasterxml.com/ObjectMapper)
	 * @author Hervé Hoareau 
	 */
	protected String toJson(Object v){
		String rc=null;
		try {
			if(v!=null)rc=new ObjectMapper().writeValueAsString(v);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rc;
	}
	

	
	protected String toFormat(Object o, String format) {

		if(format==null || format.equals("json"))return(toJson(o));
		if(format.equals("xml"))return(toXml(o));
		if(format.equals("html"))return(o.toString());
		
		return null;
	}
	
	
	private String toXml(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	/**Lecture d'un fichier texte avec remplacement de champs type et retour dans une String
	 * 
	 * @param path contient le chemin du fichier (sur GAE le fichier doit être stocké dans le répertoire webinf
	 * @param liste des champs types à remplacer, le format étant <nom_du_champs>=<valeur>,<nom_du_champs>=<valeur>
	 * 
	 * @return String contenant le fichier aprés remplacement des champs types
	 * 
	 * @author Hervé Hoareau 
	 * 
	 */

	protected String FileToString(String path,String repString){
		try {
			FileInputStream is = new FileInputStream(path);
			
			byte[] buffer=new byte[is.available()];
			is.read(buffer, 0,is.available());		
			String rc=new String(buffer,"UTF-8");
			
			for(String rep:repString.split(","))
				rc=rc.replace("<"+rep.split("=")[0].toLowerCase()+">",rep.split("=")[1]);
			
			return(rc);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return(null);
	}
	
	

	
	
	public HttpServletResponse setErrorCode(String code,String doc,HttpServletResponse response){
		//response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,getErrorCode(code,doc));
		return(response);
	}
	
	
}
