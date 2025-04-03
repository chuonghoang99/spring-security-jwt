package com.chuong.training.controller;

import com.chuong.training.dto.request.AuthenticationRequest;
import com.chuong.training.dto.request.IntrospectRequest;
import com.chuong.training.dto.request.LogoutRequest;
import com.chuong.training.dto.request.RefreshTokenRequest;
import com.chuong.training.dto.response.ApiResponse;
import com.chuong.training.dto.response.AuthenticationResponse;
import com.chuong.training.dto.response.IntrospectResponse;
import com.chuong.training.dto.response.RefreshTokenResponse;
import com.chuong.training.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authentice(@RequestBody AuthenticationRequest authenticationRequest) {
        var result = authenticationService.authenticate(authenticationRequest);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest introspectRequest) {
        var result = authenticationService.introspect(introspectRequest.token());
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request.token());
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refresh-token")
    ApiResponse<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        var result =
                authenticationService.refreshToken(refreshTokenRequest.jti());

        return ApiResponse.<RefreshTokenResponse>builder().result(result).build();

    }

}

