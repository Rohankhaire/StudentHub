package com.studenthub.serviceImpl;

import com.studenthub.dto.*;
import com.studenthub.entity.Role;
import com.studenthub.entity.User;
import com.studenthub.entity.UserStatus;
import com.studenthub.entity.RefreshToken;
import com.studenthub.exception.DuplicateResourceException;
import com.studenthub.exception.ResourceNotFoundException;
import com.studenthub.exception.UnauthorizedException;
import com.studenthub.repository.RoleRepository;
import com.studenthub.repository.UserRepository;
import com.studenthub.security.JwtTokenProvider;
import com.studenthub.service.AuthService;
import com.studenthub.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    // Transient reset token cache: Token -> ResetDetails
    private final Map<String, ResetDetails> resetTokenCache = new ConcurrentHashMap<>();

    private static class ResetDetails {
        String email;
        LocalDateTime expiryTime;

        ResetDetails(String email) {
            this.email = email;
            this.expiryTime = LocalDateTime.now().plusMinutes(15);
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           JwtTokenProvider tokenProvider,
                           RefreshTokenService refreshTokenService,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + loginRequest.getEmail()));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is inactive. Contact Admin.");
        }

        String accessToken = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .roles(roles)
                .userId(user.getId())
                .fullName(user.getFullName())
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto registerRequest, String roleName) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + registerRequest.getEmail());
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .phone(registerRequest.getPhone())
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(role)))
                .build();

        userRepository.save(user);

        // Authenticate the newly registered user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()
                )
        );

        String accessToken = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .roles(Collections.singleton(role.getName()))
                .userId(user.getId())
                .fullName(user.getFullName())
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDto refreshAccessToken(RefreshTokenRequestDto refreshRequest) {
        String requestRefreshToken = refreshRequest.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = tokenProvider.generateTokenFromUsername(user.getEmail());
                    
                    Set<String> roles = user.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.toSet());

                    return AuthResponseDto.builder()
                            .accessToken(accessToken)
                            .refreshToken(requestRefreshToken)
                            .email(user.getEmail())
                            .roles(roles)
                            .userId(user.getId())
                            .fullName(user.getFullName())
                            .build();
                })
                .orElseThrow(() -> new UnauthorizedException("Refresh token is not in database. Please log in."));
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDto forgotRequest) {
        User user = userRepository.findByEmail(forgotRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account registered with email: " + forgotRequest.getEmail()));

        String token = UUID.randomUUID().toString();
        resetTokenCache.put(token, new ResetDetails(user.getEmail()));

        // Simulate sending mail by printing to console/logs
        logger.info("==================================================");
        logger.info("PASSWORD RESET REQUEST FOR USER: {}", user.getEmail());
        logger.info("Reset Link: http://localhost:5173/reset-password?token={}", token);
        logger.info("==================================================");
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto resetRequest) {
        ResetDetails details = resetTokenCache.get(resetRequest.getToken());

        if (details == null || details.isExpired()) {
            if (details != null) {
                resetTokenCache.remove(resetRequest.getToken());
            }
            throw new UnauthorizedException("Password reset token is invalid or expired.");
        }

        User user = userRepository.findByEmail(details.email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for token."));

        user.setPassword(passwordEncoder.encode(resetRequest.getNewPassword()));
        userRepository.save(user);

        resetTokenCache.remove(resetRequest.getToken());
        logger.info("Password successfully reset for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenService.revokeToken(refreshTokenValue);
    }
}
