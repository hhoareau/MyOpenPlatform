/*
 * Copyright (C) 2012 Hoareau Herv�

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

import java.util.Map;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import com.google.appengine.labs.repackaged.com.google.common.collect.ImmutableMap;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

/**
 * Classe n�cessaire � la gestion des comptes d�velopeurs en REST
 * 
 * @author Herv� Hoareau
 *
 */
public class DeveloperRessource extends baseServlet {

	//METHOD=PUT,http://localhost:8888/rest/developer/paul.dudule@gmail.com/hh/10
	//METHOD=PUT,http://localhost:8888/rest/developer/paul.dudule@gmail.com/pd
	@Put
	public String create() {
		
		String email=(String) getRequestAttributes().get("email");
		String bitcoin=(String) getRequestAttributes().get("bitcoin");
		String password=(String) getRequestAttributes().get("password");
		
		setStatus(Status.SUCCESS_CREATED);
		return(addDeveloper(email,password,bitcoin));
	}
	
	@Post("form")
	public String subscribe(Form f) {
		String remoteAddr = this.getRootRef().getHostDomain();
        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
              
        String secretKey=System.getProperty("secretkey_captcha");
        reCaptcha.setPrivateKey(secretKey);

        String rc="";
        String email=f.getValues("email");
		String password=f.getValues("password");
		String bitcoin=f.getValues("bitcoin");
        String challenge = f.getValues("recaptcha_challenge_field");
        String uresponse = f.getValues("recaptcha_response_field");
        
        if(remoteAddr.indexOf("localhost")==-1){
            ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
    		if (reCaptchaResponse.isValid()){
    				
    			setStatus(Status.REDIRECTION_PERMANENT);
    			Map<String,String> mp=ImmutableMap.of("gmail.com","gmail.google.com","yahoo.com","mail.yahoo.com","hotmail.com","hotmail.com");
    			this.setLocationRef("http://"+mp.get(email.split("@")[1]));
    			
    			rc=addDeveloper(email,password,bitcoin);
    		} rc=dao.getErrorCode(System.getProperty("ERROR_BAD_CAPTCHA"),null);
        } else
        	rc=addDeveloper(email,password,bitcoin);	
        
        this.setStatus(Status.REDIRECTION_TEMPORARY);
        this.setLocationRef(System.getProperty("REDIRECT_AFTER_SUBSCRIPTION"));
        
        return(rc);
	}
	
	//METHOD=DELETE,http://localhost:8888/rest/developer/paul.dudule@gmail.com
	@Delete
	public void remove() {
		String email=(String) getRequestAttributes().get("email");
		String rc="";
		if(dao.delDeveloper(email))
			rc="ok : "+email+" delete";
		else
			rc=dao.getErrorCode(System.getProperty("ERROR_DEVELOPER_UNKNOWN"), null);
	}
	
	//METHOD=GET,http://localhost:8888/rest/developer/paul.dudule@gmail.com
	@Get
	public String getDeveloper() {
		String email=(String) getRequestAttributes().get("email");
		return toJson(dao.findDeveloper(email, false));
	}


	private String addDeveloper(String email,String password,String bitcoin){
		String rc="";
		String credits=(String) getRequestAttributes().get("credits");		
		String plan=(String) getRequestAttributes().get("plan");
			
		Developer d=dao.findDeveloper(email,false);
		if(d==null) 									//Si on ne trouve pas le d�veloppeur
			if(password!=null) 							//et si l'on a fourni un password 
				d=new Developer(email,password,plan); 	//C'est qu'il s'agit d'une cr�ation
			else
				rc=System.getProperty("MSG_PASSWORD_REQUIRED");
			
		if(d!=null){																					//Si le d�veloppeur � �t� trouv� ou cr�er
			if(credits!=null)																		//et si la variable de cr�dits est pr�cis�e
				d.setCredits(d.getCredits()+Integer.parseInt(credits)); //On lui cr�dite les cr�dits propos�s				
	
			d.setBitcoin(bitcoin);
			
			if(dao.putDeveloper(d)!=null){
				rc=toJson(d);
			}
		}
		return rc;
	}
	
}
