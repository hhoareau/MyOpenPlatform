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

import java.util.Map;

import org.restlet.data.Form;
import org.restlet.data.Status;

import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import com.google.appengine.labs.repackaged.com.google.common.collect.ImmutableMap;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

/**
 * this function is used to
 * - create a new developer account
 * - add some new credits on a existing account
 * - set the bitcoin address to an existing account
 *  
 *  @author Hervé Hoareau
 */
public class DeveloperRessource extends baseServlet {

	private String addDeveloper(String email,String password,String plan,String credits,String bitcoin){
		String rc="";		
		
		Developer d=dao.findDeveloper(email,false);
		if(d==null){ 												//Si on ne trouve pas le développeur
			if(password!=null) 							//et si l'on a fourni un password 
				d=new Developer(email,password,plan); 	//C'est qu'il s'agit d'une création
			else
				rc=System.getProperty("MSG_PASSWORD_REQUIRED");
		}else
			if(!d.getPassword().equals(password))
				rc=System.getProperty("ERROR_INCORRECT_PASSWORD");
		
			
		if(d!=null && rc.length()==0 ){																					//Si le développeur à été trouvé ou créer
			if(credits!=null)																		//et si la variable de crédits est précisée
				d.setCredits(d.getCredits()+Integer.parseInt(credits)); //On lui crédite les crédits proposés				
	
			if(bitcoin!=null)d.setBitcoin(bitcoin);
			
			if(dao.addDeveloper(d)!=null){
				rc=toJson(d);
			}
		}
		return rc;
	}
	
	
	//METHOD=POST,http://localhost:8888/rest/developer/paul.dudule@gmail.com/hh/10
	//METHOD=POST,http://localhost:8888/rest/developer/paul.dudule@gmail.com/pd
	@Post
	public String create() {
		
		String email=(String) getRequestAttributes().get("email");
		String password=(String) getRequestAttributes().get("password");
		String plan=(String) getRequestAttributes().get("plan");
		String bitcoin=(String) getRequestAttributes().get("bitcoin"); //bitcoin adresse
		String credits=(String) getRequestAttributes().get("credits");
		
		setStatus(Status.SUCCESS_CREATED);
		
		return(addDeveloper(email,password,plan,credits,bitcoin));
	}
	
	/**
	 * To create a new developer account from the "subscribe.jsp" form
	 * @param f the form contain the information account
	 */
	@Post("form")
	public void subscribe(Form f) {
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
    			
    			rc=addDeveloper(email,password,null,null,bitcoin);
    		} setResponse(ts.setError("ERROR_BAD_CAPTCHA",null,getResponse()));
        } else
        	setResponse(ts.setSuccess(addDeveloper(email,password,null,null,bitcoin),this.getResponse()));	
        
        this.setStatus(Status.REDIRECTION_TEMPORARY);
        this.setLocationRef(System.getProperty("REDIRECT_AFTER_SUBSCRIPTION"));    
	}
	
	/**
	 * Remove developer account
	 * METHOD=DELETE,http://localhost:8888/admin/developer/paul.dudule@gmail.com
	 * METHOD=DELETE,https://myopenplatform.appspot.com/admin/developer/paul.dudule@gmail.com
	 * METHOD=DELETE,https://myopenplatform.appspot.com/admin/developer/
	 */
	@Delete
	public void remove() {
		String email=(String) getRequestAttributes().get("email");
		String rc="";
		if(dao.delDeveloper(email)){
			if(email==null)
				setResponse(ts.setSuccess("ok: all developer accounts deleted",this.getResponse()));
			else
				setResponse(ts.setSuccess("ok: "+email+" delete",this.getResponse()));
		} else
			setResponse(ts.setError("ERROR_DEVELOPER_UNKNOWN", null,this.getResponse()));
	}
	
	
	/**
	 * METHOD=GET,http://localhost:8888/rest/developer/paul.dudule@gmail.com/pd
	 * @return a json structure of a developer or null
	 */
	@Get
	public void getDeveloper() {
		String email=(String) getRequestAttributes().get("email");
		String alt=this.getQueryValue("alt");if(alt==null)alt="json";
		String password=(String)getRequestAttributes().get("password");
		
		Developer d=dao.findDeveloper(email, false);
		if(d==null){setResponse(ts.setError("ERROR_DEVELOPER_UNKNOWN", null,this.getResponse()));return;}
		
		if(d.getPassword().equals(password)){
			if(alt.equals("html"))
				setResponse(ts.setSuccess(dao.ShowDeveloper(d), this.getResponse()));
			else
				setResponse(ts.setSuccess(toFormat(d,alt),this.getResponse()));
		}
		else setResponse(ts.setError("ERROR_INCORRECT_PASSWORD", null,this.getResponse()));
			
	}


	


	
}
