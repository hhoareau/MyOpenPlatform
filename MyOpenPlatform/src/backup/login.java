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

package backup;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.restlet.resource.Put;

import MyOpenPlatform.core.baseServlet;



//Test du servlet : 
// https://contribuez.appspot.com/contribuez/test?id=12

/* Pour ajouter un developpeur il suffit d'invoquer :
 * https://contribuez.appspot.com/admin/setdeveloper?email=hhoareau@gmail.com&password=hh4271
 * http://localhost:8888/admin/setdeveloper?email=hhoareau@gmail.com&password=hh4271
 * 
 * pour ajouter des crédits on invoque
 * https://contribuez.appspot.com/admin/setdeveloper?email=hhoareau@gmail.com&credit=10
 * http://localhost:8888/admin/setdeveloper?email=hhoareau@gmail.com&credit=10
 * 
 * Pour supprimer un developpeur
 * https://contribuez.appspot.com/admin/setdeveloper?email=hhoareau@gmail.com&delete
 * http://localhost:8888/admin/setdeveloper?email=hhoareau@gmail.com&delete
 * 
 * 
 */


@SuppressWarnings("serial")
public class login extends baseServlet {
	
	  @Put
	    public void login(){
	        String token1 = null;
            //JWT_Handler handler = new JWT_Handler(ISSUER, SIGNING_KEY);
	        JWT_Handler handler = new JWT_Handler("","");
            
            try {token1=handler.getJWT();
			} catch (InvalidKeyException e) {e.printStackTrace();
			} catch (SignatureException e) {e.printStackTrace();
			}
            
            HashMap<String,Object> attributes = new HashMap<String,Object>();
            attributes.put("token",token1);
            super.getRequest().setAttributes(attributes);
            

            /*
            RequestDispatcher dispatcher = super.getRequest().getRequestDispatcher("index.jsp?token=" + token1);
       
            if (dispatcher != null)
				try {
					dispatcher.forward(req, resp);
				} catch (ServletException e) {e.printStackTrace();}
				*/
	  }
}
