/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.control.DispatchResult;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;

/**
 * is the "thing" generated by one user-click. It contains mainly the servlet
 * request and response and the usersession, and it should not be assigned to an instance variable.
 *
 * @author Felix Jost
 */
public class UserRequestImpl implements UserRequest {
	
	private static final OLog log = Tracing.createLoggerFor(UserRequestImpl.class);

	/**
	 * <code>PARAM_DELIM</code>
	 */
	public static final String PARAM_DELIM = ":";

	private final HttpServletRequest httpReq;
	private final HttpServletResponse httpResp;

	private String uriPrefix;
	private String moduleURI;
	private String nonParsedUri;
	private Map<String,String> params;

	// results set by the controller which dispatches the http/click event
	private DispatchResult dispatchResult;

	private String windowID;
	private String timestampID;
	private String componentID;
	private String componentTimestamp;
	private int mode;

	private boolean isValidDispatchURI;

	private final String uuid;
	private final Date requestTimestamp;
	private static AtomicInteger count = new AtomicInteger(0);


	/**
	 * @param uriPrefix
	 * @param httpReq
	 * @param httpResp
	 */
	public UserRequestImpl(String uriPrefix, HttpServletRequest httpReq, HttpServletResponse httpResp) {
		this.httpReq = httpReq;
		this.httpResp = httpResp;
		this.uriPrefix = uriPrefix;
		isValidDispatchURI = false;
		params = new HashMap<String,String>(4);
		dispatchResult = new DispatchResult();
		parseRequest(httpReq);
		
		requestTimestamp = new Date();
		uuid = Integer.toString(count.incrementAndGet());
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	@Override
	public Date getRequestTimestamp() {
		return requestTimestamp;
	}

	@Override
	public String getUriPrefix() {
		return uriPrefix;
	}

	/**
	 * @param key
	 * @return the value of the parameter with key 'key'
	 */
	@Override
	public String getParameter(String key) {
		return params.get(key);
	}

	/**
	 * @return the Set of parameters
	 */
	@Override
	public Set<String> getParameterSet() {
		return params.keySet();
	}

	/**
	 * @return the http request
	 */
	@Override
	public HttpServletRequest getHttpReq() {
		return httpReq;
	}

	/**
	 * @return The openolat user session
	 */
	@Override
	public UserSession getUserSession() {
		UserSession result = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(getHttpReq());
		if (result == null) {
			log.warn("getUserSession: null, this="+this, new RuntimeException("getUserSession"));
		}
		return result;
	}

	/**
	 * @return HttpServletResponse
	 */
	public HttpServletResponse getHttpResp() {
		return httpResp;
	}

	/**
	 * convenience method
	 * 
	 * @return Locale
	 */
	public Locale getLocale() {
		return getUserSession().getLocale();
	}

	/**
	 * convenience method
	 * 
	 * @return Subject
	 */
	public Identity getIdentity() {
		return getUserSession().getIdentity();
	}

	/**
	 * <pre>
	 * 
	 *  
	 *   
	 *    
	 *     e.g. /..../92,20,15,cid,del/bla/blu.html
	 *          ......
	 *          uriPrefix (must be provided at construction time by dispatcher)
	 *               /.........................
	 *                encoded params           /............
	 *                                          moduleURI (without starting slash)
	 *     
	 *    
	 *   
	 *  
	 * </pre>
	 * 
	 * Encoded params follow a strict syntax: Params are separated by PARAM_DELIM.
	 * The first three params are WindowsID, TimestampID and ComponentID. Any
	 * remaining params make up key/value pairs.
	 */
	private void parseRequest(HttpServletRequest hreq) {
		String uri = hreq.getRequestURI();
		String decodedUri;
		try {
			hreq.setCharacterEncoding("utf-8");
			decodedUri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertException("utf-8 encoding not supported!!!!");
		}

		// log the http request headers, but do not parse the parameters (could destroy data for file upload)
		if (log.isDebug()) {
			StringBuilder sb = new StringBuilder("\nRequest Parameters:\n");
			appendFormattedKeyValue(sb, "URI", hreq.getRequestURI());		
			appendFormattedKeyValue(sb, "Protocol", hreq.getProtocol());
			appendFormattedKeyValue(sb, "HTTP Method", hreq.getMethod());
			appendFormattedKeyValue(sb, "Scheme", hreq.getScheme());
			appendFormattedKeyValue(sb, "Server Name", hreq.getServerName());
			appendFormattedKeyValue(sb, "Server Port", new Integer(hreq.getServerPort()));
			appendFormattedKeyValue(sb, "Remote Addr", hreq.getRemoteAddr());
			appendFormattedKeyValue(sb, "Remote Host", hreq.getRemoteHost());
			appendFormattedKeyValue(sb, "Character Encoding", hreq.getCharacterEncoding());
			appendFormattedKeyValue(sb, "Content Length", new Integer(hreq.getContentLength()));
			appendFormattedKeyValue(sb, "Content Type", hreq.getContentType());
			appendFormattedKeyValue(sb, "Locale", hreq.getLocale());
			appendFormattedKeyValue(sb, "QueryString", hreq.getQueryString());
			sb.append("\n\nHeaders in this hreq:\n");
			Enumeration<String> e = hreq.getHeaderNames();
			while (e.hasMoreElements()) {
				String key = e.nextElement();
				String value = hreq.getHeader(key);
				appendFormattedKeyValue(sb, key, value);
			}
			// show params for forms
			if (hreq.getContentType() != null && hreq.getContentType().equals("application/x-www-form-urlencoded")) {
				sb.append("Parameter names in this hreq:</h4>");
				e = hreq.getParameterNames();
				while (e.hasMoreElements()) {
					String key = e.nextElement();
					String[] values = hreq.getParameterValues(key);
					String value = "";
					for (int i = 0; i < values.length; i++) {
						value = value + " " + values[i];
					}
					appendFormattedKeyValue(sb, key, value);
				}

			}
			log.debug(sb.toString());
		}		
		
		// copy legacy GET or POST parameters (?a=10&b=20...)
		String contentType = hreq.getContentType();

		// do not waste inputstream on file uploads
		if (contentType == null || !contentType.startsWith("multipart/")) {
			//if you encouter problems with content in url like german umlauts
			//make sure you set <Connector port="8080" URIEncoding="utf-8" /> to utf-8 encoding
			//this will decode content in get requests like request.getParameter(...
			Map<String, String[]> m = hreq.getParameterMap();
			for (Iterator<String> ksi = m.keySet().iterator(); ksi.hasNext();) {
				String key = ksi.next();
				String val = hreq.getParameterValues(key)[0];
				params.put(key, val);
			}
		} else if(contentType.startsWith("multipart/")) {
			try {
				hreq.getParts();
			} catch (IOException | ServletException e) {
				log.error("", e);
			}
		}

		nonParsedUri = decodedUri.substring(uriPrefix.length()); // guaranteed to
		// exist by OpenOLATServlet

		// parse parameters
		int nextSlash = nonParsedUri.indexOf('/');
		if (nextSlash == -1) return; //no params
		String encparams = nonParsedUri.substring(0, nextSlash);
		parseEncodedParams(encparams);

		// get moduleURI
		if (nextSlash + 1 < nonParsedUri.length()) {
			moduleURI = nonParsedUri.substring(nextSlash + 1);
			if (moduleURI.indexOf("../") != -1) throw new AssertException("a non-normalized url encountered "+moduleURI);
		}
	}

	private void appendFormattedKeyValue(StringBuilder sb, String key, Object value) {
		sb.append("\n");
		sb.append(key);
		sb.append(" : ");
		sb.append(value);
	}

	private void parseEncodedParams(String encodedParams) {
		try {
			StringTokenizer st = new StringTokenizer(encodedParams, PARAM_DELIM, false);
			// first decode framework params
			if (st.countTokens() < 4) return; // if framework params not present,
			// return
			windowID = st.nextToken();
			if (windowID.equals("0")) windowID = null;
			timestampID = st.nextToken();
			if (timestampID.equals("0")) timestampID = null;
			componentID = st.nextToken();
			if (componentID.equals("0")) componentID = null;

			// decode the componentTimestamp (always there, also if not used for a given link)
			componentTimestamp = st.nextToken(); // always "1" or a higher number
			
			// decode the "mode" token (ajax or standard)
			mode = Integer.parseInt(st.nextToken());

			/*
			// business control path for bookmarking
			// for parsing, it is easiest to have at least one char -> substring
			businessControlPath = st.nextToken().substring(1);
			*/
			
			// decode remaining module specific key/value params
			while (st.hasMoreTokens()) { // hasMoreToken would return false if there
				// isn't any token at all
				String key = st.nextToken();
				String value = st.nextToken();
				params.put(key, value);
			}
		} catch (NoSuchElementException nse) { // thrown if # of tokes is odd. Just
			// ignore last token
			throw new AssertException("Odd number of encoded parameter tokens. Was trying to parse '" + encodedParams + "'");
		}
		isValidDispatchURI = true;
	}

	/**
	 * @return String
	 */
	public String getModuleURI() {
		return moduleURI;
	}

	/**
	 * Only getter provided. User URLBuilder to set the resulting respond's
	 * windowID.
	 * 
	 * @return the windowid
	 */
	public String getWindowID() {
		return windowID;
	}

	/**
	 * Only getter provided. User URLBuilder to set the resulting respond's
	 * timestampID.
	 * 
	 * @return the timestamp
	 */
	public String getTimestampID() {
		return timestampID;
	}

	/**
	 * Only getter provided. User URLBuilder to set the resulting respond's
	 * componentID.
	 * 
	 * @return the component id
	 */
	public String getComponentID() {
		return componentID;
	}
	
	/**
	 * @return
	 */
	public String getComponentTimestamp() {
		return componentTimestamp;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "w:" + getWindowID() + ", t:" + getTimestampID() + ", c:" + getComponentID() +  ", ct:" + componentTimestamp + ", m:" + mode +  ", =" + httpReq.getRequestURL().toString()+" , params: "+params.toString();
	}

	/**
	 * @return true if the url containing the encoded params for timestamp,
	 *         windowid, and component id; and false if the url was e.g. an url
	 *         like /olat/auth/go/course
	 */
	public boolean isValidDispatchURI() {
		return isValidDispatchURI;
	}

	/**
	 * @return the uri; never null, but may be an empty string
	 */
	public String getNonParsedUri() {
		return nonParsedUri;
	}

	/**
	 * @return Returns the dispatchResult.
	 */
	public DispatchResult getDispatchResult() {
		return dispatchResult;
	}

	/**
	 * @return Returns the mode.
	 */
	public int getMode() {
		return mode;
	}
}