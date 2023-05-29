package com.stage.code_gen.config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	
	private static final String SECRET_KEY="5266546A576E5A7234753778214125442A472D4B6150645367566B5870327335";
	private static final long TOKEN_VALIDITY=1000*60*24;
	
	public String extractUserEmail(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	
	public <T> T extractClaim(String token,Function<Claims, T> claimsResolver){
		 final Claims claims = extarctAllClaims(token);
		 return claimsResolver.apply(claims);
	}
	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(),userDetails);
	}
	
	public String generateToken(Map<String,Object> extraClaims,UserDetails userDetails) {
		return Jwts
				.builder()
				.setClaims(extraClaims)
				.setSubject(userDetails.getUsername()) //this one is getting the email but in userDetails it's called the username
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis()+TOKEN_VALIDITY))
				.signWith(getSignInKey(),SignatureAlgorithm.HS256)
				.compact();
	}
	public boolean isTokenValid(String token ,UserDetails userDetails) {
		final String username=extractUserEmail(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}
	
	private boolean isTokenExpired(String token) {
		// TODO Auto-generated method stub
		return extractExpiation(token).before(new Date());
	}

	private Date extractExpiation(String token) {
		// TODO Auto-generated method stub
		return extractClaim(token, Claims::getExpiration);
	}

	private Claims extarctAllClaims(String token) {
		return  Jwts
				.parserBuilder()
				.setSigningKey(getSignInKey()) 
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	private Key getSignInKey() {
		// TODO Auto-generated method stub
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
