/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth service for Facebook as third-party authenticator.
 * <p>
 * 
 * The configuration of the service is done using properties, whose values need
 * to match the values configured at Facebook.
 * <p>
 * <ul>
 * <li><code>facebook-oauth2-redirect-endpoint</code>: the URL of the local
 * redirect endpoint, to which the Facebook OAuth service redirects the user
 * after authentication. See also {@link FacebookService#getRedirectEndpoint()
 * getRedirectEndpoint()}</li>
 * <li><code>facebook-oauth2-app-id</code>: The application ID</li>
 * <li><code>facebook-oauth2-app-secret</code>: The application secret.</li>
 * </ul>
 * <p>
 * For example: <blockquote>
 * 
 * <pre>
 * <properties>
 *    <property name="facebook-oauth2-redirect-endpoint">
 *      http://localhost:8080/oauth2callback
 *    </property>
 *    <property name="facebook-oauth2-app-id">
 *      1234567890123456
 *    </property>
 *    <property name="facebook-oauth2-app-secret">
 *      a3cf1630b1ae415c7260d849efdf444d
 *    </property>
 *  </properties>
 * </pre>
 * 
 * </blockquote>
 * <p>
 * Like all <b>service classes</b>, this class holds only configuration state.
 * Thus, once configured, it can be safely shared between multiple sessions
 * since its state (the configuration) is read-only. A &quot;const
 * FacebookService&quot; object is thus thread-safe.
 * <p>
 */
public class FacebookService extends OAuthService {
	private static Logger logger = LoggerFactory
			.getLogger(FacebookService.class);

	/**
	 * Constructor.
	 */
	public FacebookService(AuthService baseAuth) {
		super(baseAuth);
	}

	/**
	 * Checks whether a FacebookAuth service is properly configured.
	 * <p>
	 * This returns <code>true</code> if a value is found for the three
	 * configuration properties.
	 */
	public static boolean configured() {
		try {
			configurationProperty(RedirectEndpointProperty);
			configurationProperty(ClientIdProperty);
			configurationProperty(ClientSecretProperty);
			return true;
		} catch (RuntimeException e) {
			logger.info(new StringWriter().append("not configured: ").append(
					e.toString()).toString());
			return false;
		}
	}

	public String getName() {
		return "facebook";
	}

	public WString getDescription() {
		return new WString("Facebook Account");
	}

	public int getPopupWidth() {
		return 1000;
	}

	public int getPopupHeight() {
		return 600;
	}

	public String getAuthenticationScope() {
		return EmailScope;
	}

	public String getRedirectEndpoint() {
		return configurationProperty(RedirectEndpointProperty);
	}

	public String getAuthorizationEndpoint() {
		return AuthUrl;
	}

	public String getTokenEndpoint() {
		return TokenUrl;
	}

	public String getClientId() {
		return configurationProperty(ClientIdProperty);
	}

	public String getClientSecret() {
		return configurationProperty(ClientSecretProperty);
	}

	public Method getTokenRequestMethod() {
		return Method.Get;
	}

	public OAuthProcess createProcess(String scope) {
		return new FacebookProcess(this, scope);
	}

	static String RedirectEndpointProperty = "facebook-oauth2-redirect-endpoint";
	static String ClientIdProperty = "facebook-oauth2-app-id";
	static String ClientSecretProperty = "facebook-oauth2-app-secret";
	static String AuthUrl = "https://www.facebook.com/dialog/oauth";
	static String TokenUrl = "https://graph.facebook.com/oauth/access_token";
	static String EmailScope = "email";
}
