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

import org.restlet.data.Status;
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

import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import MyOpenPlatform.core.baseServlet;

import com.example.contribuez.share.Book;
import com.googlecode.objectify.Query;


//______________________________________________________________________________________________
//BookRessource implemente l'interface REST de gestion des livres
//Pour autant les méthodes de gestion de la base de données restes dans la classe DAO

public class BookRessource extends baseServlet {

	//http:METHODE=PUT,URL=http://localhost:8888/rest/book/Leviathan/J.Lock
	@Post
	public void create() {
		Long rc=dao.addBook(new Book((String)getRequestAttributes().get("title"),
				(String)getRequestAttributes().get("autor")));
		
		if(rc==DAO.BOOK_ALREADY_EXIST)
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
	}
	
	
	//METHODE=DELETE,URL=http://localhost:8888/rest/book/hhoareau@gmail.com
	//METHODE=DELETE,URL=http://localhost:8888/rest/book
	@Delete
	public void remove() {
		String title=(String)getRequestAttributes().get("title");
		if(title!=null)
			dao.delBook(title);
		else
			dao.ofy().delete(dao.ofy().query(Book.class));
	}
	
	
	
	//METHODE=GET,URL=http://localhost:8888/rest/book/
	//METHODE=GET,URL=http://localhost:8888/rest/book/Leviathan
	@Get
	 public String getBook() {	
		String title=(String)getRequestAttributes().get("title");
		Query<Book> q=dao.ofy().query(Book.class);
		if(title!=null)q=q.filter("title",title);
		return(toJson(q.list()));
	}
	
}
