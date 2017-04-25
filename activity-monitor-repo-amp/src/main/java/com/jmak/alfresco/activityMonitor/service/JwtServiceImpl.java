package com.jmak.alfresco.activityMonitor.service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

@Service
public final class JwtServiceImpl implements JwtService {

	private static final Log LOG = LogFactory.getLog(JwtServiceImpl.class);

	private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;
	private static final long EXPIRATION_TIME = 60 * 1000;

	@Value("${activity.monitor.api.key.secret}")
	private String apiKeySecret;


	public String generate(final String subject, final Map<String, Object> claims) {	 
		final long nowMillis = System.currentTimeMillis();
		final Date now = new Date(nowMillis);
		final Date exp = new Date(nowMillis + EXPIRATION_TIME);

		final byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(apiKeySecret);
		final Key signingKey = new SecretKeySpec(apiKeySecretBytes, SIGNATURE_ALGORITHM.getJcaName());

		return Jwts.builder()
				.setSubject(subject)
				.setClaims(claims)
				.signWith(SIGNATURE_ALGORITHM, signingKey)
				.setIssuedAt(now)
				.setExpiration(exp)
				.compact();
	}

	public Claims parse(final String jwt) {
		Claims claims = null;

		try {
			claims = Jwts.parser()         
					.setSigningKey(DatatypeConverter.parseBase64Binary(apiKeySecret))
					.parseClaimsJws(jwt)
					.getBody();
		}
		catch (SignatureException se) {
		    LOG.error("The received token is not valid");
		}
		catch (Exception e) {
			LOG.error("There was an error while reading the received token");
		}

		final Date now = new Date(System.currentTimeMillis());
		final Date expiration = claims.getExpiration();

		if (Objects.isNull(expiration) || now.after(expiration)) {
			LOG.error("The received token has expired");
			return null;
		}

		return claims;
	}
}
