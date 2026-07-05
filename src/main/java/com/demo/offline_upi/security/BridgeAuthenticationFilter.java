package com.demo.offline_upi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter that intercepts incoming HTTP requests to authenticate Bridge Nodes
 * via 'X-Bridge-Node-Id' and Administrators via 'X-Admin-Key'.
 */
public class BridgeAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String bridgeNodeId = request.getHeader("X-Bridge-Node-Id");
        String adminKey = request.getHeader("X-Admin-Key");

        if ("admin-secret".equals(adminKey)) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else if (bridgeNodeId != null && !bridgeNodeId.trim().isEmpty()) {
            // In production, we validate node certificates or signature headers.
            // For this simulator, we authenticate any non-blank bridge ID as ROLE_BRIDGE.
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    bridgeNodeId, null, List.of(new SimpleGrantedAuthority("ROLE_BRIDGE")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
