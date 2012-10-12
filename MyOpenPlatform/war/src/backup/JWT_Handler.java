package backup;

//Voir https://code.google.com/p/gwdg-java/source/browse/src/java/com/google/iapsample/JWT_Handler.java

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Calendar;
import java.util.regex.Pattern;

import org.joda.time.Instant;

import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.crypto.HmacSHA256Signer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.api.client.util.Base64;

public class JWT_Handler {

	private String ISSUER;
	   private String SIGNING_KEY;

	    public JWT_Handler(String issuer, String key) {
	        this.ISSUER = issuer;
	        this.SIGNING_KEY = key;
	    }

	    private JsonToken createToken() throws InvalidKeyException {
	        Calendar cal = Calendar.getInstance();
	        HmacSHA256Signer signer = new HmacSHA256Signer(ISSUER, null, SIGNING_KEY.getBytes());

	        //Configure JSON token
	        JsonToken token = new JsonToken(signer);
	        token.setAudience("Google");
	        token.setParam("typ", "google/payments/inapp/item/v1");
	        token.setIssuedAt(new Instant(cal.getTimeInMillis()));
	        token.setExpiration(new Instant(cal.getTimeInMillis() + 60000L));

	        //Configure request object, which provides information of the item
	        JsonObject request = new JsonObject();
	        request.addProperty("name", "1000 crédits");
	        request.addProperty("description", "1000 crédits");
	        request.addProperty("price", "1");
	        request.addProperty("currencyCode", "EUR");
	        request.addProperty("sellerData", "idUser");

	        JsonObject payload = token.getPayloadAsJsonObject();
	        payload.add("request", request);

	        return token;
	    }

	    public String deserialize(String tokenString) {
	        String[] pieces = splitTokenString(tokenString);
	        String jwtPayloadSegment = pieces[1];
	        JsonParser parser = new JsonParser();
	        JsonElement payload;
			try {
				payload = parser.parse(new String(Base64.decode(jwtPayloadSegment.getBytes()),"UTF-8"));
				return payload.toString();
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return null;
	    }

	    /**
	     * @param tokenString The original encoded representation of a JWT
	     * @return Three components of the JWT as an array of strings
	     */
	    private String[] splitTokenString(String tokenString) {
	        String[] pieces = tokenString.split(Pattern.quote("."));
	        if (pieces.length != 3) {
	            throw new IllegalStateException("Expected JWT to have 3 segments separated by '"
	                    + "." + "', but it has " + pieces.length + " segments");
	        }
	        return pieces;
	    }
	    
	    
	    public String getJWT() throws InvalidKeyException, SignatureException {
	        JsonToken token = this.createToken();
	        return token.serializeAndSign();
	    }

}
