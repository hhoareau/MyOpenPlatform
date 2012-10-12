var first;

$(document).ready(function(){
	first=Date.now(); 
	main();	
 });
 
 function query(method,urlapi,param,key,f){
	 $.ajax({
		  type: method,
		  url: urlapi+"?alt=json",
		  data: param,
		  beforeSend: function (xhr) {
			  if(key!=null)
				  xhr.setRequestHeader('Authorization', 'Basic '+key);
			  },
	 	  
		  success:function(data){
			  f(data);
		  },
	 	  
		  error:function(XHR, textStatus, errorThrown){
	 			  alert(XHR.status + " - " + XHR.responseText + " - " + textStatus);
	 		  }
	 });
 }
 
 
 function api(method,url,param,token,f){ 
	 sleep(100);
	 query(method,"http://localhost:8888/api/v1/"+url,param,token,f);
	 //query(method,"https://myopenplatform.appspot.com/api/v1/"+url,param,token,f);
 }

 function adminApi(method,s,param,f){
	 sleep(500);
	 query(method,"http://localhost:8888/api/admin/v1/"+s,param,null,f)
	 //query(method,"https://myopenplatform.appspot.com/api/admin/v1/"+s,param,null,f)
}

 function sleep(millis){
 var date = new Date();
 var curDate = null;
 do { curDate = new Date(); } 
 while(curDate-date < millis);
 } 
 
 function testApi(token){
	 api("DELETE","book","",token,function(){
		 for(i=1;i<=60;i++){
			 url="book/titre"+i+"/auteur"+i;
			 api("POST",url,"",token,function(rep){
				if(rep.indexOf("60")>0)	 
				 $('.container').append("rep="+rep+" a"+(Date.now()-first)+"<br>");
			 });		 
		 }		 
	 });
 }
 
 
 function main(){
	 adminApi("DELETE","method","",function(){
		 adminApi("DELETE","developer","",function(){
			 adminApi("POST","method/newbook/standard/0=0,10=3,100=2/500","",function(){
				 adminApi("POST","method/getbook/standard/0=0,10=1,1000=2/1000","",function(){
					 adminApi("POST","method/deletebook/standard/0=0,100=1,1000=2/1000","",function(){			 
						 adminApi("POST","developer/hhoareau@gmail.com/hh/1000/standard","",function(rep){
							 token=JSON.parse(rep).token;
							 testApi(token);							 
						 });	 
					 });
				 });
			 });			 
		 });
	 });
 }