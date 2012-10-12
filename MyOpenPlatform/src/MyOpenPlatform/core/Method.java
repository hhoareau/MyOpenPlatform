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


/*
 * La classe Book est uniquement un exemple métier d'un plateforme de service
 * le minimum à été placé dans cette classe
 */

@Unindexed
public class Method {
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -5352407197990187064L;
	
	@Id public String Id;				//Concaténation du plan tarifaire et de Url de l'API
	public String tarifs;				//Crédits consommés par pallier suivant la syntaxe <Volume,cout> 
	public Integer quota;				//Quota d'usage par jours
	
	public Method(){};
	
	/**
	 * Constructeur de la method
	 * @param url url sans le domaine de la nouvelle méthode
	 * @param idPlan nom du plan tarifaire applicable
	 * @param credits plan de consommation
	 * @param quotaByDay nombre max par jour
	 * @param quotaByMonth nombre max par mois
	 */
	public Method(String idMethod,String idPlan,String tarifs,Integer quota){
		this.setTarifs(tarifs);
		this.setQuota(quota);
		this.setId(idPlan+","+idMethod);
	}
	
	public String getUrl(){return(this.Id.split(",")[1]);}
	public String getPlan(){return(this.Id.split(",")[0]);}
	

	public String getId() {return Id;}
	public void setId(String id) {Id = id;}
	public String getTarifs() {return tarifs;}
	public void setTarifs(String tarifs) {this.tarifs = tarifs;}
	
	public String PresentTarifs(String sep){
		String rc="";
		Integer i=0,j=null;
		String s[]=this.tarifs.split(",");
		int k=0;
		for(k=0;k<s.length-1;k++){
			i=Integer.parseInt(s[k].split("=")[0]);
			j=Integer.parseInt(s[k+1].split("=")[0]);
			if(j>i)
				rc+="from "+i+" to "+j+" : "+s[k].split("=")[1]+" crédits "+sep;
			i=j;
		}
		rc+="above "+j+" : "+s[k].split("=")[1]+" crédits";
		
		return(rc.substring(0, rc.length()-sep.length()));
	}

	public Integer getQuota() {return quota;}
	public void setQuota(Integer quota) {this.quota = quota;}

	//Retourne le tarifs applicable en fonction du nombre de hits de l'API
	public Integer getCost(Integer hits) {
		Integer rc=0;
		for(String s:this.tarifs.split(",")){
			if(Integer.parseInt(s.split("=")[0])>hits)
				return rc;
			else
				rc=Integer.parseInt(s.split("=")[1]);
		}
		return rc;
	};	
	
	public String toString(){
		String rc=this.PresentTarifs(" - ")+" "+this.getUrl();
		return(rc);
	}
		
}