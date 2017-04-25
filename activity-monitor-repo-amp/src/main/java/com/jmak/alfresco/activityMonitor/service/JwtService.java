package com.jmak.alfresco.activityMonitor.service;

import java.util.Map;

import io.jsonwebtoken.Claims;

public interface JwtService {

	String generate(String subject, Map<String, Object> claims);

	Claims parse(String jwt);
}
