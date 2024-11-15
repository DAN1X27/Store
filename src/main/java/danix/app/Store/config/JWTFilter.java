package danix.app.Store.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import danix.app.Store.controllers.AuthController;
import danix.app.Store.services.PersonDetailsService;
import danix.app.Store.security.JWTUtil;
import danix.app.Store.services.TokensService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final PersonDetailsService detailsService;
    private final TokensService tokensService;

    @Autowired
    public JWTFilter(JWTUtil jwtUtil, PersonDetailsService detailsService, TokensService tokensService) {
        this.jwtUtil = jwtUtil;
        this.detailsService = detailsService;
        this.tokensService = tokensService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if(authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);


            if(!tokensService.isValid(jwtUtil.getIdFromToken(token))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (token.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid JWT token in Bearer Header");
            }else {
                try {

                    String email = jwtUtil.validateTokenAndRetrieveClaim(token);

                    UserDetails userDetails = detailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails,
                                    userDetails.getPassword(), userDetails.getAuthorities());

                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }catch (JWTVerificationException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid JWT token");
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
