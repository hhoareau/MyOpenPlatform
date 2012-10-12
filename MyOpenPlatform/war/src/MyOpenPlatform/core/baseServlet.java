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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.resource.ServerResource;

import backup.Seller_Info;
import com.example.contribuez.servlet.DAO;


/**
 * Cette classe regroupe l'ensemble des méthodes utilisables par les classes
 * implémentant la gestion REST des objets de la base de données
 * 
 * @author Hervé Hoareau
 */

public class baseServlet extends ServerResource {
 
	/**Données nécessaire au paiement sur google checkout
	 * @see http://support.google.com/checkout/sell/bin/answer.py?hl=en&answer=42963
	 */
	Seller_Info seller = new Seller_Info();
	
	/*
	 * voir http://code.google.com/p/objectify-appengine/wiki/BestPractices#Utilisez_un_DAO
	 * sur la justification de l'usage d'un objet DAO statique
	 */
	protected static DAO dao;
	
	public baseServlet(){
		dao=new DAO();
	}
	
	/**
	 * Convertit un objet au format JSON
	 * @param objet : contient n'importe quel objet ou liste d'objets à convertire
	 * @return String contenant l'objet au format json (voir http://wiki.fasterxml.com/ObjectMapper)
	 * @author Hervé Hoareau 
	 */
	protected String toJson(Object v){
		String rc=null;
		try {
			if(v!=null)rc=new ObjectMapper().writeValueAsString(v);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rc;
	}
	
	
	/**Lecture d'un fichier texte avec remplacement de champs type et retour dans une String
	 * 
	 * @param path contient le chemin du fichier (sur GAE le fichier doit être stocké dans le répertoire webinf
	 * @param liste des champs types à remplacer, le format étant <nom_du_champs>=<valeur>,<nom_du_champs>=<valeur>
	 * 
	 * @return String contenant le fichier aprés remplacement des champs types
	 * 
	 * @author Hervé Hoareau 
	 * 
	 */

	protected String FileToString(String path,String repString){
		try {
			FileInputStream is = new FileInputStream(path);
			
			byte[] buffer=new byte[is.available()];
			is.read(buffer, 0,is.available());		
			String rc=new String(buffer,"UTF-8");
			
			for(String rep:repString.split(","))
				rc=rc.replace("<"+rep.split("=")[0].toLowerCase()+">",rep.split("=")[1]);
			
			return(rc);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return(null);
	}
	
}
