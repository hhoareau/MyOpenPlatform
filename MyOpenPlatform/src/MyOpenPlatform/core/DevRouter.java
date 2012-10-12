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

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.LocalVerifier;


/**
 * DevRouter is a personnalisation of the router class to integrate the admin routes
 * 
 * @see http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/326-restlet.html
 * @see http://wiki.restlet.org/docs_2.1/13-restlet/275-restlet/252-restlet.html
 *  
 */
public abstract class DevRouter extends Router {
	
	protected ChallengeAuthenticator guard=null;
	
	public DevRouter(Context ctx){
		
		super(ctx);
		
		final DAO_Developer dao=new DAO_Developer();
		
		 
		 /**
		  * Version modifiée de la classe Router assurant le controle des acces aux API par les comptes développeurs :
		  * 
		  * Documentation complémentaire sur = 
		  * @see http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/326-restlet.html
		  * @see http://wiki.restlet.org/docs_2.1/13-restlet/275-restlet/252-restlet.html
		  */
		 guard=new ChallengeAuthenticator(ctx,ChallengeScheme.HTTP_BASIC,"OPA"){
			 
			
			 /**
			  * méthode appelé à chaque call d'API
			  * @see org.restlet.routing.Filter#doHandle(org.restlet.Request, org.restlet.Response)
			  */
			 /*
			  * 
			 Developer devCaller=null;
			 MethodCall mc=null;
			
			 @Override protected int doHandle(Request request, Response response){
			
				 String idDeveloper=request.getChallengeResponse().getIdentifier();
				 devCaller=dao.findDeveloper(idDeveloper, false);
				 if(devCaller==null)return Filter.STOP;
				 
				  
				 
				 //Controle et mise a jour du compte développeur
				 //mc=chkApiCall(devCaller,request.getResourceRef().getBaseRef().getPath());
				 
				 if(mc.getLastCode().equals("ERROR_CALL_OK"))
					 return(super.doHandle(request, response)); //Execution de la méthode métier
				 else { 
					 response=new Tools().setError(mc.getLastCode(),null,response); 
					 return(Filter.STOP); 
				 }	
			 }
			  */
			 
		 };
		 
		 myRoutes();
		 
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
		 
		 String developerVersion="/admin/v"+System.getProperty("APIDEVELOPER_VERSION");
		 attach(developerVersion+"/developer", DeveloperRessource.class);
		 attach(developerVersion+"/developer/{email}", DeveloperRessource.class); //see http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/326-restlet.html
		 attach(developerVersion+"/developer/{email}/{password}", DeveloperRessource.class); // Ex : https://myopenplatform.appspot.com/api/admin/v1/developer/hhoareau@gmail.com/hh
		 attach(developerVersion+"/developer/{email}/{password}/{credits}/{plan}", DeveloperRessource.class);
			
		 attach(developerVersion+"/method/{url}/{plan}/{tarifs}/{quota}", MethodRessource.class); //Ex : https://myopenplatform.appspot.com/api/admin/v1/method/book/standard/0=0,3=1,6=2/10
		 attach(developerVersion+"/method",MethodRessource.class);
	}
	
	/**
	 * addRestAPI sets the route for the API and add the Basic protection for each URL
	 * @see http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/326-restlet/376-restlet.html
	 * @param path path of your API like /book/{author}
	 * @param class1 class to contain the data
	 */
	public void addRestAPI(String path, Class<? extends ServerResource> class1){
		guard.setNext(class1);
		
		/**
		 * When we expose API in a Restful mode, we need a versioning strategy that is exposed here
		 * @see http://blog.apigee.com/taglist/versioning
		 */
		String version="/v"+System.getProperty("API_VERSION");
		attach(version+path, guard);
	}
	
	
	
	protected abstract void myRoutes();
		
}
