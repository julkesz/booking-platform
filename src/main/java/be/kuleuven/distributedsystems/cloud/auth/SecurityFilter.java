package be.kuleuven.distributedsystems.cloud.auth;

import be.kuleuven.distributedsystems.cloud.entities.User;
import be.kuleuven.distributedsystems.cloud.repository.UserRepository;
import be.kuleuven.distributedsystems.cloud.utils.Utils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final GooglePublicKeysFetcher googlePublicKeysFetcher;
    private final String projectId;

    @Autowired
    public SecurityFilter(@Qualifier("projectId") String projectId, UserRepository userRepository, GooglePublicKeysFetcher googlePublicKeysFetcher) {
        this.userRepository = userRepository;
        this.googlePublicKeysFetcher = googlePublicKeysFetcher;
        this.projectId = projectId;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // TODO: (level 1) decode Identity Token and assign correct email and role
        // TODO: (level 2) verify Identity Token

        String token = request.getHeader("Authorization");
        User user;
        if (projectId.equals(Utils.LOCAL_PROJECT_ID))
            user = extractUserLocal(token);
        else
            user = extractUserCloud(token);

        if (user != null) {
            userRepository.updateUser(user);
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(new FirebaseAuthentication(user));
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith("/api");
    }

    public static User getUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private DecodedJWT verifyToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            RSAPublicKey publicKey = googlePublicKeysFetcher.getPublicKeyById(decodedJWT.getKeyId());

            if (publicKey != null) {
                Algorithm algorithm = Algorithm.RSA256(publicKey, null);
                return JWT.require(algorithm)
                        .withIssuer("https://securetoken.google.com/" + projectId)
                        .build()
                        .verify(token);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private User extractUserCloud(String token) {
        token = token.substring(7);
        DecodedJWT jwt = verifyToken(token);
        String email = jwt.getClaim("email").asString();
        Claim role = jwt.getClaim("role");
        if (role == null)
            return new User(email, new String[]{});
        return new User(email, new String[]{role.asString()});
    }

    private User extractUserLocal(String token) {
        String[] chunks = token.split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();

        String payload = new String(decoder.decode(chunks[1]));
        JSONParser parser = new JSONParser();
        try {
            JSONObject json = (JSONObject) parser.parse(payload);
            if (json.containsKey("role"))
                return new User(json.get("email").toString(), new String[]{json.get("role").toString()});
            return new User(json.get("email").toString(), new String[]{});
        } catch (ParseException ignored) {
        }
        return null;
    }

    private static class FirebaseAuthentication implements Authentication {
        private final User user;

        FirebaseAuthentication(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public User getPrincipal() {
            return this.user;
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public void setAuthenticated(boolean b) throws IllegalArgumentException {
        }

        @Override
        public String getName() {
            return null;
        }
    }
}

