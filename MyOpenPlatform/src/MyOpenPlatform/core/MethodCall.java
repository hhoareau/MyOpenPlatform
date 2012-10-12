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


import javax.persistence.Id;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;


/*
 * La classe Book est uniquement un exemple métier d'un plateforme de service
 * le minimum à été placé dans cette classe
 */

@Indexed
public class MethodCall {
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -5352407197990187064L;
	
	@Id public Long Id;  										//Identifiant interne des livres
	public String idMethod;								//title des ouvrages, souvent utilisé comme clé de recherche
	public String idDeveloper;							//Id du développeur
	@Unindexed public Integer hits;					//Nombre de hits
	@Unindexed public String lastCode;			//Last errorCode 
	
	
	
	public MethodCall(){};
	
	public MethodCall(String idMethod,String idDeveloper){
		this.setHits(0);
		this.setIdDeveloper(idDeveloper);
		this.setIdMethod(idMethod);
	}
	
	
	
	
	public String getLastCode() {return lastCode;}
	public void setLastCode(String lastCode) {this.lastCode = lastCode;}
	
	public void AddCall(){hits++;}
	
	public Long getId() {return Id;}
	public void setId(Long id) {Id = id;}
	
	public String getIdMethod() {return idMethod;}
	public void setIdMethod(String idMethod) {this.idMethod = idMethod;}
	
	public String getIdDeveloper() {return idDeveloper;}
	public void setIdDeveloper(String idDeveloper) {this.idDeveloper = idDeveloper;}
	
	public Integer getHits() {return hits;}
	public void setHits(Integer hits) {this.hits = hits;};	
	
	
}