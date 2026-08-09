package br.com.api.satireapi.infra.security.jwt;

import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final CustomerAuthenticationGateway customerAuthenticationGateway;

    public JwtAuthenticationFilter(
        JwtTokenService jwtTokenService,
        CustomerAuthenticationGateway customerAuthenticationGateway
    ) {
        this.jwtTokenService = jwtTokenService;
        this.customerAuthenticationGateway = customerAuthenticationGateway;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        SecurityContextHolder.clearContext();
        var header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            var token = header.substring(BEARER_PREFIX.length());
            jwtTokenService.parse(token).ifPresent(claims -> {
                try {
                    authenticate(claims);
                } catch (RuntimeException ex) {
                    // Token assinado mas com claims fora do contrato: segue não autenticado (401), nunca 500.
                    SecurityContextHolder.clearContext();
                }
            });
        }
        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private void authenticate(io.jsonwebtoken.Claims claims) {
        var customerId = UUID.fromString(claims.getSubject());
        var customer = customerAuthenticationGateway.findById(customerId)
            .filter(existingCustomer -> existingCustomer.active())
            .orElseThrow(() -> new IllegalArgumentException("Customer is inactive or does not exist"));
        var profiles = customer.profiles();
        var authorities = profiles.stream()
            .map(profile -> new SimpleGrantedAuthority("ROLE_" + profile))
            .toList();
        var authentication = new UsernamePasswordAuthenticationToken(customerId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
