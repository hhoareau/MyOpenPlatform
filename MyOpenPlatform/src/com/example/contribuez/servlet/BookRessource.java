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



import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;


import MyOpenPlatform.core.baseServlet;

import com.example.contribuez.share.Book;
import com.googlecode.objectify.Query;


//______________________________________________________________________________________________
//BookRessource implemente l'interface REST de gestion des livres
//Pour autant les méthodes de gestion de la base de données restes dans la classe DAO

public class BookRessource extends baseServlet {

	//http:METHODE=POST,URL=http://localhost:8888/api/v1/book/Leviathan/J.Lock
	@Post
	public String create() {
		
		if(chkApiCall("newbook")){
			Book b=dao.addBook(new Book((String)getRequestAttributes().get("title"),
													(String)getRequestAttributes().get("autor")));
			
			if(b!=null){
				addCall();
				return(this.toJson(b));
			}
			
			setResponse(ts.setError("Book already existe",null, getResponse()));
		}

		return null;		
	}
	



	//METHODE=DELETE,URL=http://localhost:8888/rest/book/hhoareau@gmail.com
	//METHODE=DELETE,URL=http://localhost:8888/rest/book
	@Delete
	public void remove() {
		if(!chkApiCall("deletebook"))return;
		
		String title=(String)getRequestAttributes().get("title");
		if(title!=null)
			dao.delBook(title);
		else
			dao.ofy().delete(dao.ofy().query(Book.class));
		
		addCall();
	}
	
	
	
	//METHODE=GET,URL=http://localhost:8888/rest/book/
	//METHODE=GET,URL=http://localhost:8888/rest/book/Leviathan
	@Get
	public String getBook() {	
		if(!chkApiCall("getbook"))return null;
			
		String title=(String)getRequestAttributes().get("title");
		Query<Book> q=dao.ofy().query(Book.class);
		if(title!=null){
			q=q.filter("title",title);
			addCall();
		}
		return(toJson(q.list()));
	}
	
}
