package com.chuong.training.configuration;

import com.chuong.training.exception.AppException;
import com.chuong.training.exception.ErrorCode;
import com.chuong.training.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomJwtDecoder implements JwtDecoder {

    NimbusJwtDecoder nimbusJwtDecoder;

    AuthenticationService authenticationService;

    @Override
    public Jwt decode(String token) throws JwtException {

        boolean isValid = authenticationService.introspect(token).isValid();

        if (!isValid) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return nimbusJwtDecoder.decode(token);
    }
}