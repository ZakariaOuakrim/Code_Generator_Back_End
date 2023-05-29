package com.stage.code_gen.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	
	private final JwtService jwtService;
	
	private final UserDetailsService userDetailsService ;
	
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
									@NonNull HttpServletResponse response,
									@NonNull FilterChain filterChain
									)throws ServletException, IOException {
		if (request.getServletPath().contains("/auth")) {
		      filterChain.doFilter(request, response);
		      return;
		}
		final String authHeader=request.getHeader(HttpHeaders.AUTHORIZATION);
		final String jwt;
		final String user_email;
		if(authHeader==null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		jwt=authHeader.substring(7);
		user_email =jwtService.extractUserEmail(jwt);
		if(user_email!=null && SecurityContextHolder.getContext().getAuthentication()==null) {//the second test is to test if the user is already connected if not it will retun null
			
			UserDetails userDetails =this.userDetailsService.loadUserByUsername(user_email);
			if(jwtService.isTokenValid( jwt, userDetails) ) {
				UsernamePasswordAuthenticationToken authToken=new  UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
		filterChain.doFilter(request, response);		
	}
	 
}
