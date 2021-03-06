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
 * An OAuth access token.
 * <p>
 * 
 * A access token is the result of an authorization process, and encapsulates
 * the authorization to access protected information.
 * <p>
 * Next to its {@link OAuthAccessToken#getValue() getValue()}, it also contains
 * optionally an expires date and a refresh token.
 * <p>
 * 
 * @see OAuthProcess#startAuthorize()
 * @see OAuthProcess#authorized()
 */
public class OAuthAccessToken {
	private static Logger logger = LoggerFactory
			.getLogger(OAuthAccessToken.class);

	/**
	 * Default constructor.
	 * <p>
	 * Creates an invalid access token.
	 */
	public OAuthAccessToken() {
		this.accessToken_ = "";
		this.refreshToken_ = "";
		this.expires_ = null;
	}

	/**
	 * Constructor.
	 */
	public OAuthAccessToken(String accessToken, WDate expires,
			String refreshToken) {
		this.accessToken_ = accessToken;
		this.refreshToken_ = refreshToken;
		this.expires_ = expires;
	}

	/**
	 * Returns whether the token is valid.
	 * <p>
	 * An invalid access token is used to signal for example that the user
	 * denied the authorization request.
	 */
	public boolean isValid() {
		return this.accessToken_.length() != 0;
	}

	/**
	 * Returns the access token value.
	 * <p>
	 * This value can be used to access protected resources.
	 */
	public String getValue() {
		return this.accessToken_;
	}

	/**
	 * Returns the token expires time (if available).
	 * <p>
	 * Returns null if not available.
	 */
	public WDate expires() {
		return this.expires_;
	}

	/**
	 * Returns the refresh token (if available).
	 * <p>
	 * The refresh token is an optional token that can be used when the access
	 * token has expired.
	 * <p>
	 * If not available, returns an empty string.
	 */
	public String getRefreshToken() {
		return this.refreshToken_;
	}

	/**
	 * An invalid token constant.
	 * <p>
	 * This is a token that is not {@link OAuthAccessToken#isValid() isValid()}.
	 */
	public static final OAuthAccessToken Invalid = new OAuthAccessToken();
	private String accessToken_;
	private String refreshToken_;
	private WDate expires_;
}
