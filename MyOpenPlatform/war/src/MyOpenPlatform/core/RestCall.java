package MyOpenPlatform.core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;


import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;
import java.io.IOException;

//Classe utiliser pour effectuer les appels REST en mode GET ou POST aux API SFR 
public class RestCall  {
		
	private static final Logger log = Logger.getLogger(RestCall.class.getName());
	
	
	//Constructeur
	public RestCall(String urlServer,String params) {				
			
		try {			
			URL url = new URL(urlServer);
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                
			log.warning("Appel de "+url+" avec param="+params);
            
			if(params!=null){
	            connection.setRequestProperty("Content-Length", ""+params.getBytes().length);
	            connection.setUseCaches(false);
	            connection.setDoOutput(true);
	            connection.setDoInput(true);
	            connection.setRequestMethod("POST");
	            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
	            writer.writeBytes(params);
	            writer.flush();
	            writer.close();
            } else {
            	connection.setRequestMethod("GET");	
            }
            	
            //Delai de réponse fixé à 30 secondes
            connection.setReadTimeout(30*1000);
            connection.connect();

            //Récuperation de la réponse
	        BufferedReader buffer=new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line="";
			StringWriter writer=new StringWriter();
			while ( null!=(line=buffer.readLine()))writer.write(line);
	        
			int reponseCode=connection.getResponseCode();
	        if (reponseCode == HttpURLConnection.HTTP_OK) {     	
				if(reponseCode==200)					
					onSuccess(writer.toString());       		
				else 
					onFailure(reponseCode);
	        }
	        else onFailure(reponseCode);        
    } 
		catch (MalformedURLException e) {} 
		catch (IOException e) {
    }	
		
	}
	
	//Est appellée quand le serveur retourn 200, méthode doit être surchargée
	public void onSuccess(String rep) {}; 
	
	//Idem que onSuccess pour les cas d'échec
	public void onFailure(int reponseCode) {}	
}
