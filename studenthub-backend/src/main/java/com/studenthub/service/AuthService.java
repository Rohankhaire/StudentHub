package com.studenthub.service;

import com.studenthub.dto.*;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto loginRequest);
    AuthResponseDto register(RegisterRequestDto registerRequest, String roleName);
    AuthResponseDto refreshAccessToken(RefreshTokenRequestDto refreshRequest);
    void forgotPassword(ForgotPasswordRequestDto forgotRequest);
    void resetPassword(ResetPasswordRequestDto resetRequest);
    void logout(String refreshTokenValue);
}
