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

import MyOpenPlatform.core.DAO_Developer;
import com.example.contribuez.share.Book;
import com.googlecode.objectify.ObjectifyService;

/*
 * Cette classe implémente l'ensemble des méthodes de lecture / écritures dans
 * la base de données de Google. Elle utilise en particulier le framework
 * Objectify (http://code.google.com/p/objectify-appengine/wiki/IntroductionToObjectify)
 * pour simplifier les accès.
 * 
 * Voir documentation sur :
 * Objectify : http://code.google.com/p/objectify-appengine/wiki/IntroductionToObjectify
 * Objectify et GAE : http://www.ibm.com/developerworks/java/library/j-javadev2-13/index.html
 * 
 * Le choix de passer par un objet DAO : http://code.google.com/p/objectify-appengine/wiki/BestPractices#Utilisez_un_DAO
 * 
 */

public class DAO extends DAO_Developer {

	public DAO() {}

	static {	
		ObjectifyService.register(Book.class);
	}

	public static final Long BOOK_ALREADY_EXIST = -1L;
	public static final Long BOOK_NOT_EXIST = -1L;
	

	public Book findBook(String title) {
		return ofy().query(Book.class).filter("title",title).get();
	}

	public Long addBook(Book b) {
		if(findBook(b.getTitle())==null)
			return ofy().put(b).getId();
		else
			return(BOOK_ALREADY_EXIST); //Retour si le livre était déjà présent
	}

	public void delBook(String title) {
		Book b=findBook(title);
		if(b!=null)ofy().delete(b);
	}


}
