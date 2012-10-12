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

package com.example.contribuez.share;


import javax.persistence.Id;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;


/**
 * La classe Book est un exemple de ce que pourrait être
 * le service métier exposée
 * le minimum à été placé dans cette classe pour illustrer
 * l'usage de la BigTable de Google (http://fr.wikipedia.org/wiki/BigTable)*
 * 
 * @author Hervé Hoareau
 * @version 1.0
 */
@Indexed
public class Book {
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -5352407197990187064L;
	
	@Id public Long Id;  				//Identifiant interne des livres
	public String title;				//title des ouvrages, souvent utilisé comme clé de recherche
	@Unindexed public String autor;		//Auteur de l'ouvrage, aucune recherche ne peut être effectué
										//sur ce champs qui est noté unindexed afin d'économiser de la 
										//place sur BigTable
	
	//Getters & Setters
	public String getTitle() {return title;}
	public void setTitle(String title) {this.title = title;}
	public String getAutor() {return autor;}
	public void setAutor(String autor) {this.autor = autor;}

	
	public Book(){};
	
	public Book(String title,String autor){
		this.setAutor(autor);
		this.setTitle(title);
	};	
}