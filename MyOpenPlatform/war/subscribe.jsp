<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- The HTML 4.01 Transitional DOCTYPE declaration-->
<!-- above set at the top of the file will set     -->
<!-- the browser's rendering engine into           -->
<!-- "Quirks Mode". Replacing this declaration     -->
<!-- with a "Standards Mode" doctype is supported, -->
<!-- but may lead to some differences in layout.   -->
<!-- 
Cette page contient le code minimum pour illustrer une inscription sur le portail développeur
Suite à l'inscription un email est envoyé contenant le code d'authentification d'usage des API
-->

<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
<%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Subscription</title>
  </head>

  <body>
  
 
  
    <h1>Sign up</h1>
	
<TABLE BORDER=0>
<TR>
	<TD>
	
<FORM method=POST action="/api/admin/v1/developer">
<TABLE BORDER=0>
<TR>
	<TD>Email</TD>
	<TD>
	<INPUT type=text name="email">
	</TD>
</TR>

<TR>
	<TD>Password</TD>
	<TD>
	<INPUT type=password name="password">
	</TD>
</TR>

<TR>
	<TD>BitCoin</TD>
	<TD>
	<INPUT type=text name="bitcoin">
	</TD>
</TR>

<TR>
	<TD>Humain ?</TD>
	<TD>
	 <%
          ReCaptcha c = ReCaptchaFactory.newReCaptcha(System.getProperty("captcha_publickey"),
        		  						System.getProperty("captcha_secretkey"), false);
          out.print(c.createRecaptchaHtml(null, null));
        %>
	</TD>
</TR>

<TR>
	<TD COLSPAN=2>
	<INPUT type="submit" value="Sign up">
	</TD>
</TR>

 </TABLE>
 </FORM>
 
 </TD>

 
<TD width=400>
<!-- To have a client id, you must create an facebook app here : https://developers.facebook.com/apps 
the redirection url must be declared in your web.xml file.
if you don't want a facebook registration, you can delete the iframe 
-->
 <iframe src="https://www.facebook.com/plugins/registration?
		client_id=342901602470132&
        fields=name,email&
        redirect_uri=https%3A%2F%2Fmyopenplatform.appspot.com%2Ffacebookcallback&
        scrolling="auto"
        frameborder="no"
        style="border:none"
        allowTransparency="true"
        width="100%"
        height="330">
</iframe>
 </TD>
</TR>
 
  </TABLE>


 
    
  </body>
</html>
