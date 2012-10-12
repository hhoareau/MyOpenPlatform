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
import org.restlet.Restlet;
import MyOpenPlatform.core.DevRouter;


/**
 * this class is necessary to organize the URI and the class of the object to expose
 * RestletDispatch is called when the first developer use your API
 * @see http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/326-restlet.html
 * @see http://wiki.restlet.org/docs_2.1/13-restlet/275-restlet/252-restlet.html
 *  
 *  @author Herve Hoareau
 */

public class RestletDispatch extends Application  {
	 
	 public synchronized Restlet createInboundRoot() {
		 DevRouter r=new DevRouter(getContext()){
			 @Override protected void myRoutes(){		
				 	/**
					 * Add your API hear with addRestAPI function
					 * Syntaxe : Path,Class of the ressource
					 */
					addRestAPI("/book",BookRessource.class);
					addRestAPI("/book/{title}",BookRessource.class);
					addRestAPI("/book/{title}/{autor}",BookRessource.class);
					
					 //Ajouter ici l'ensemble des URL propres à vos services
					 //router.attach(..., ...class);
					 //router.attach(..., ...class);					
				}		 
		 };

		 return r;
	 }
	
		
}
