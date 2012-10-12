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

import java.util.Date;
import javax.persistence.Id;
import com.google.api.client.util.Base64;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;


/**
 * Représentation d'un compte développeur
 * 
 * @author Hervé Hoareau
 */

@Indexed
public class Developer {
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -5352407197990187064L;
	
	@Id public String email;					//Email étant Id il est forcément unique
	public String token;						//Token généré pour l'usage des API
	@Unindexed public String password;			//Mot de passe du compte développeur
	@Unindexed public String bitcoin=null;			//Référence du wallet bitcoin
	@Unindexed public Integer credits; 			//Credit restant
	@Unindexed public Long dateCreation;		//Date de création du compte
	@Unindexed public String idPlan;			//Plan tarifaire et quota à appliquer
	@Unindexed public Long razDate;				//Date de réinitialisation du compteur

	
	
	public String getBitcoin() {
		return bitcoin;
	}
	public void setBitcoin(String bitcoin) {
		if(bitcoin!=null && bitcoin.length()>0)
			this.bitcoin = bitcoin;
	}
	public Long getRazDate() {
		return razDate;
	}
	public void setRazDate(Long razDate) {
		this.razDate = razDate;
	}
	public String getIdPlan() {return idPlan;}
	public void setIdPlan(String idPlan) {this.idPlan = idPlan;}
	public Long getDateCreation() {return dateCreation;}
	public void setDateCreation(Long dateCreation) {this.dateCreation = dateCreation;}

	/** Vérifie le nombre de crédit d'un compte développeur et décompte si il y en a suffisament
	 * 	@param req designe la requete en entre 
	 * @param tarif désigne le tarif nécéssaire
	 * @return 0 si le compte n'a pas suffisament de crédit / -1 si le compte n'est pas identifier / >0 le nombre de crédit restant
	 */
	public boolean chkCredits(int tarif){	
			if(getCredits()>tarif){
				setCredits(getCredits()-tarif);
				return(true);
			}
			else return(false);
	}
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String Email) {
		this.email = Email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Integer getCredits() {
		return credits;
	}
	public void setCredits(Integer credits) {
		this.credits = credits;
	}
	
	private String generateKey(String user,String pass){
		/*
		try {
			SecureRandom key=SecureRandom.getInstance("SHA1PRNG", "SUN");
			return(String.valueOf(0-key.nextLong()));
			
		} catch (NoSuchAlgorithmException e) {e.printStackTrace();
		} catch (NoSuchProviderException e) {e.printStackTrace();}
		return(null);
		*/
		
		Long now=new Date().getTime();
		return new String(Base64.encode((now.toString()+":"+user+":"+pass).getBytes()));
	}
	
	public Developer(){};
	
	public Developer(String email,String password,String bitcoin){
		this.setEmail(email);
		this.setPassword(password);
		this.setCredits(Integer.parseInt(System.getProperty("DEFAULT_CREDIT_START")));
		this.setIdPlan(System.getProperty("DEFAULT_OFFER_START"));
		
		this.setBitcoin(bitcoin); //Bitcoin address to receive money
		
		this.setToken(generateKey(email,password)); //Unique ID to access API
		Long now=new Date().getTime();
		this.setDateCreation(now);
		this.setRazDate(now);
	};	
}