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

import org.restlet.resource.Delete;
import org.restlet.resource.Put;


public class MethodRessource extends baseServlet {
	
	//METHOD=PUT,http://localhost:8888/rest/method/book/standard/0=0,100=1,1000=2/5
	@Put
	public String create() {
		String url=(String) getRequestAttributes().get("url");
		String plan=(String) getRequestAttributes().get("plan");
		String tarifs=(String) getRequestAttributes().get("tarifs");
		String quota=(String) getRequestAttributes().get("quota");
		dao.addMethod(plan, url, tarifs, Integer.parseInt(quota));
		return("Ok");
	}
	
	//METHOD=DELETE,http://localhost:8888/rest/method
	@Delete
	public String remove() {
		dao.razMethod();
		return("ok");
	}

	
}
