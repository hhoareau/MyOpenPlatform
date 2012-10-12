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

import MyOpenPlatform.core.baseServlet;



//Seller Identifier:11381708135174882060
//Seller Secret:WAVjqXAqE13Zy_yNR6IIAA
//Postback URL:https://contribuez.appspot.com/contribuez/walletpostback

//Execution https://contribuez.appspot.com/buy.jsp
//					https://localhost:8888/buy.jsp

@SuppressWarnings("serial")
public class walletPostback extends baseServlet {
	/*
	    @Override
	    protected void doPost(HttpServletRequest request,
	            HttpServletResponse response) throws ServletException, IOException {
	        //"Handles post request.
	        String jwt = request.getParameter("jwt");
	        String orderID;
	        String jwt_response = new JWT_Handler(ISSUER, SIGNING_KEY).deserialize(jwt);
	        JsonParser parser = new JsonParser();
	        Gson gson = new GsonBuilder().create();
	        JsonArray payload = parser.parse("[" + jwt_response + "]").getAsJsonArray();
	        Payload payload_1 = gson.fromJson(payload.get(0), Payload.class);
	        // validate the payment request and respond back to Google
	        if (payload_1.iss_getter().equals("Google")
	                && payload_1.aud_getter().equals(ISSUER)) {
	            if (payload_1.response_getter() != null
	                    && payload_1.response_getter().orderId_getter() != null) {
	                orderID = payload_1.response_getter().orderId_getter();
	                if (payload_1.request_getter().currencyCode_getter() != null
	                        && payload_1.request_getter().sellerData_getter() != null
	                        && payload_1.request_getter().name_getter() != null
	                        && payload_1.request_getter().price_getter() != null) {
	                    // optional - update local database
	                    // respond back to complete payment
	                    response.setStatus(200);
	                    PrintWriter writer = response.getWriter();
	                    writer.write(orderID);
	                }
	            }
	        }
	    }
	    */
}
