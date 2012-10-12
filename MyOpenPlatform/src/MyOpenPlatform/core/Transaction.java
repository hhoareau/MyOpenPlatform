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
import com.googlecode.objectify.annotation.Unindexed;


/**
 * Class to contain the transaction log
 * 
 * @author Herve Hoareau
 *
 */
@Unindexed
public class Transaction {
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -5352407197990187064L;
	
	@Id public String  email;					//Developer identifier
	public String payment_platform;		//Must be paypal or bitping 
	public String idTransaction;				//Transaction id
	
	public Transaction(){};
	
	/**
	 * Transaction constructor
	 * @param email
	 * @param payment_platform
	 * @param transaction_id
	 */
	public Transaction(String email,String payment_platform,String transaction_id){
		this.setEmail(email);
		this.setPayment_platform(payment_platform);
		this.setIdTransaction(transaction_id);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPayment_platform() {
		return payment_platform;
	}

	public void setPayment_platform(String payment_platform) {
		this.payment_platform = payment_platform;
	}

	public String getIdTransaction() {
		return idTransaction;
	}

	public void setIdTransaction(String idTransaction) {
		this.idTransaction = idTransaction;
	}
	
	
	
	
}