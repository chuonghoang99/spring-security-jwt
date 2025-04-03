package com.chuong.training.service;

import com.chuong.training.dto.request.AuthenticationRequest;
import com.chuong.training.dto.response.AuthenticationResponse;
import com.chuong.training.dto.response.IntrospectResponse;
import com.chuong.training.dto.response.RefreshTokenResponse;
import com.chuong.training.entity.InvalidatedToken;
import com.chuong.training.entity.Role;
import com.chuong.training.entity.User;
import com.chuong.training.exception.AppException;
import com.chuong.training.exception.ErrorCode;
import com.chuong.training.repository.InvalidatedTokenRepository;
import com.chuong.training.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String signerKey;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long validDuration;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long refreshDuration;

    public IntrospectResponse introspect(String token) {
        var isValid = true;

        try {
            verifyToken(token);
        } catch (Exception e) {
            isValid = false;
        }

        return new IntrospectResponse(isValid);

    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        var user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).build();
    }

    public void logout(String token) {

        var signToken = verifyToken(token);

        try {
            String jwt = signToken.getJWTClaimsSet().getJWTID();

            var expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jwt).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);

        } catch (ParseException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }

    }

    public RefreshTokenResponse refreshToken(String token) {
        return null;
    }

    private SignedJWT verifyToken(String token) {
        JWSVerifier verifier = null;
        SignedJWT signedJWT = null;
        try {
            verifier = new MACVerifier(signerKey.getBytes());
            signedJWT = SignedJWT.parse(token);
            var verified = signedJWT.verify(verifier);
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (!(verified && expiration.after(new Date())))
                throw new AppException(ErrorCode.UNAUTHENTICATED);

            if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);

            }

        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }

        return signedJWT;
    }

    private String generateToken(User user) {
        // Header -> xac dinh mk sd thuat toan ma hoa gi
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        // Payload
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("com.chuong")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .claim("scope", buildScope(user))
                .jwtID(UUID.randomUUID().toString()).build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        // Sign token:
        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot generate token", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }

    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            for (Role role : user.getRoles()) {
                stringJoiner.add(role.getName());
            }
        }

        return stringJoiner.toString();
    }
}
