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

package com.example.contribuez.servlet;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;

import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.LocalVerifier;

import MyOpenPlatform.core.DAO_Developer;
import MyOpenPlatform.core.Developer;
import MyOpenPlatform.core.DeveloperRessource;
import MyOpenPlatform.core.MethodRessource;


/*
 * La classe RestletDispatch assure le routage entre les URL des requetes
 * client et les classes du serveur
 * 
 * Documentation complémentaire sur 
 * http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/326-restlet.html
 * http://wiki.restlet.org/docs_2.1/13-restlet/275-restlet/252-restlet.html
 *  
 */
public class RestletDispatch extends Application {
	
	protected static ChallengeAuthenticator guard;
	protected static DAO_Developer dao;
	Router router;
	
	public void add(String path, Class<? extends ServerResource> class1){
		 guard.setNext(class1);
		 router.attach(path, guard);
	}
	
	 @Override
	 public synchronized Restlet createInboundRoot() {
		 //La classe DevRouter est une version modifiée de la classe Router
		 //assurant le controle des acces aux API par les comptes développeurs
		 dao=new DAO_Developer();
		 router = new Router(getContext());
		 
		 /**
		  * Version modifiée de la classe Router assurant le controle des acces aux API par les comptes développeurs :
		  * 
		  * Documentation complémentaire sur = 
		  * @see http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/326-restlet.html
		  * @see http://wiki.restlet.org/docs_2.1/13-restlet/275-restlet/252-restlet.html
		  */
		 guard=new ChallengeAuthenticator(getContext(),ChallengeScheme.HTTP_BASIC,"OPA"){
			 /**
			  * méthode appelé à chaque call d'API
			  * @see org.restlet.routing.Filter#doHandle(org.restlet.Request, org.restlet.Response)
			  */
			 @Override protected int doHandle(Request request,Response response){
				 String idDeveloper=request.getChallengeResponse().getIdentifier();
				 Developer d=dao.findDeveloper(idDeveloper, false);
				 if(d==null)return Filter.STOP;
				 
				 //Controle et mise a jour du compte développeur
				 String rc=dao.AddApiCall(d,request.getResourceRef().getBaseRef().getPath());
				 
				 if(rc==System.getProperty("ERROR_CALL_OK"))
					 return(super.doHandle(request, response)); //Execution de la méthode métier
				 else { 
					 response=dao.setErrorCode(rc,null,response); 
					 return(Filter.STOP); 
				 }	 			 
			 }
		 };
		 
		 /**
		  * Adaptation de LocalVerifier pour vérifier du demandeur dans la base des comptes
		  * @see http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/46-restlet/112-restlet.html 
		  */
		 final LocalVerifier verifier=new LocalVerifier(){
			    @Override
			    public char[] getLocalSecret(String identifier) {   
			    	Developer d=dao.findDeveloper(identifier, false);
			        if(d!=null)return(d.getPassword().toCharArray());
			        return null;
			    }
		 };
		 guard.setVerifier(verifier);
		 
		/**Ensemble des URL pour l'exemple d'exposition d'un catalogue de livre
		 * Versionning : @see http://blog.apigee.com/taglist/versioning
		 * Parameter : @see  
		 */
		 String version="/v"+System.getProperty("API_VERSION"); 
		 add(version+"/book",BookRessource.class);
		 add(version+"/book/{title}",BookRessource.class);
		 add(version+"/book/{title}/{autor}",BookRessource.class);
		 
		 //Ajouter ici l'ensemble des URL propres à vos services
		 //router.attach(..., ...class);
		 //router.attach(..., ...class);
		 	 	 
		 String developerVersion="/admin/v"+System.getProperty("APIDEVELOPER_VERSION");
		 router.attach(developerVersion+"/developer", DeveloperRessource.class);
		 router.attach(developerVersion+"/developer/{email}/{password}", DeveloperRessource.class); //{password} est un autre mappage
		 router.attach(developerVersion+"/developer/{email}/{password}/{credits}/{plan}", DeveloperRessource.class);
			
		 router.attach(developerVersion+"/method/{url}/{plan}/{tarifs}/{quota}", MethodRessource.class);
		 router.attach(developerVersion+"/method",MethodRessource.class);
		 
		 return router;
	 }


	
	
	
}
