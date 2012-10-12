<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <title>Doodle Poster Store</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"></meta>
    
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script language="javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js" type="text/javascript"></script>
    <script type="text/javascript">
      google.load('payments', '1.0', {'packages': ['sandbox_config']});

      // Success handler
      var successHandler = function(status){
        if (window.console != undefined) {
          console.log("Purchase completed successfully: ", status);
          //window.location.reload();
        }
      }

      // Failure handler
      var failureHandler = function(status){
        if (window.console != undefined) {
          console.log("Purchase failed ", status);
        }
      }

      function purchase(item) {
        var generated_jwt;
        if (item == "Item1") {
          generated_jwt = "<%= request.getParameter("token") %>";
        }  else {
          return;
        }

        goog.payments.inapp.buy({
          'jwt'     : generated_jwt,
          'success' : successHandler,
          'failure' : failureHandler
        });
      }
    </script>
    
    
    
  </head>
  <body>
    <h1>Doodle Poster Store</h1>
    
<form action="https://www.paypal.com/cgi-bin/webscr" method="post">
<input type="hidden" name="cmd" value="_s-xclick">
<input type="hidden" name="hosted_button_id" value="8K7N64T4GAZDG">
<table>
<tr><td><input type="hidden" name="on0" value="Options">Options</td></tr><tr><td><select name="os0">
	<option value="Pack 10 credits">Pack 10 credits €0,01 EUR</option>
	<option value="Pack 1000 credits">Pack 1000 credits €1,00 EUR</option>
	<option value="Pack 5000 credits">Pack 5000 credits €8,00 EUR</option>
</select> </td></tr>
</table>
<input type="hidden" name="currency_code" value="EUR">
<input type="image" src="https://www.paypalobjects.com/fr_FR/FR/i/btn/btn_buynowCC_LG.gif" border="0" name="submit" alt="PayPal - la solution de paiement en ligne la plus simple et la plus sécurisée !">
<img alt="" border="0" src="https://www.paypalobjects.com/fr_FR/i/scr/pixel.gif" width="1" height="1">
</form>


    <table>
    <tr valign="top">
      <td>
      <ul>
        <li>
          <button class="buy-button" type="button" onClick="purchase('Item1');">Purchase</button></li>
          <br/>
      </ul>
      </td>
    </tr>
    </table>
    

    
  </body>
</html>