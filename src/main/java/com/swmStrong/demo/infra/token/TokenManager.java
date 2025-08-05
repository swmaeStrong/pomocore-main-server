package com.swmStrong.demo.infra.token;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.user.facade.UserDetailsProvider;
import com.swmStrong.demo.infra.redis.repository.RedisRepositoryImpl;
import com.swmStrong.demo.infra.token.dto.RefreshTokenRequestDto;
import com.swmStrong.demo.infra.token.dto.SubjectResponseDto;
import com.swmStrong.demo.infra.token.dto.TokenResponseDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class TokenManager {
    private final RedisRepositoryImpl redisRepository;
    private final UserDetailsProvider userDetailsProvider;

    public static final String REDIS_REFRESH_TOKEN_PREFIX = "auth:refreshToken:";

    public TokenManager(RedisRepositoryImpl redisRepository, UserDetailsProvider userDetailsProvider) {
        this.redisRepository = redisRepository;
        this.userDetailsProvider = userDetailsProvider;
    }

    @Value("${spring.jwt.salt}")
    private String salt;

    /**
     * 암호화 해시 키 생성
     * @return 해시 키
     */
    private Key getKey() {
        return Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 유저의 userId을 받아 토큰을 발급
     *
     * @param userId 유저의 PK
     * @param tokenType 발급 받을 토큰 타입 (accessToken, refreshToken 중 하나)
     * @return accessToken, refreshToken 중 하나
     */
    public String createToken(String userId, TokenType tokenType, Role role) {
        long expireTime = tokenType.getExpireTime() * 1000;

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expireTime))
                .claim("role", role.getAuthority())
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String createToken(String userId, TokenType tokenType, String userAgent) {
        long expireTime = tokenType.getExpireTime() * 1000;

        return Jwts.builder()
                .setSubject(userId)
                .claim("userAgent", userAgent)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expireTime))
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean isTokenValid(String accessToken) {
        return parseToken(accessToken);
    }

    /**
     * <p>리프레시 토큰용</p>
     * 토큰을 복호화 해서 유효한지 확인
     *
     * @param refreshTokenRequestDto 리프레시 토큰과 User-Agent
     * @return 토큰이 유효한지에 대한 boolean 값
     */
    public boolean isTokenValid(String userId, RefreshTokenRequestDto refreshTokenRequestDto, String userAgent) {
        String refreshToken = refreshTokenRequestDto.refreshToken();

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            String tokenUserId = claims.getSubject();
            String tokenUserAgent = claims.get("userAgent", String.class);

            return parseToken(refreshToken) &&
                    tokenUserId.equals(userId) &&
                    (tokenUserAgent.startsWith("Pawcus") ||
                            tokenUserAgent.startsWith("Pomocore")) &&
                    (userAgent.startsWith("Pawcus") ||
                            userAgent.startsWith("Pomocore"));

        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 리프레시
     *
     * @param refreshTokenRequestDto 리프레시 토큰, 이메일
     * @throws RuntimeException 리프레시토큰도 만료되었거나, 리프레시 토큰을 통해 유저를 찾지 못하는 경우
     */
    public TokenResponseDto tokenRefresh(
            RefreshTokenRequestDto refreshTokenRequestDto,
            String userAgent
    ) throws RuntimeException {
        if (!isTokenValid(refreshTokenRequestDto.userId(), refreshTokenRequestDto, userAgent)) {
            throw new ApiException(ErrorCode._INVALID_TOKEN);
        }
        String refreshTokenKey = REDIS_REFRESH_TOKEN_PREFIX + refreshTokenRequestDto.userId();
        if (!refreshTokenRequestDto.refreshToken().equals(
                redisRepository.getData(refreshTokenKey))
        ) {
            throw new ApiException(ErrorCode._INVALID_TOKEN);
        };
        redisRepository.deleteData(refreshTokenKey);
        Role role = userDetailsProvider.loadRoleByUserId(refreshTokenRequestDto.userId());

        return getToken(refreshTokenRequestDto.userId(), userAgent, role);
    }

    /**
     * 토큰을 만들고, refreshToken은 레디스에 저장
     *
     * @param userId 유저의 PK
     * @return 생성된 두 토큰을 반환
     */
    public TokenResponseDto getToken(String userId, String userAgent, Role role) {
        String accessToken = createToken(userId, TokenType.accessToken, role);
        String refreshToken = createToken(userId, TokenType.refreshToken, userAgent);
        try {
            redisRepository.setDataWithExpire(REDIS_REFRESH_TOKEN_PREFIX + userId, refreshToken, TokenType.refreshToken.getExpireTime());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return TokenResponseDto.of(accessToken, refreshToken);
    }

    /**
     * 토큰을 통해 인증을 받음
     *
     * @param token 토큰
     * @throws RuntimeException 유효한 토큰이 아닌 경우
     */
    public void authenticateWithToken(String token) throws RuntimeException {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userId = claims.getSubject();
        String role = claims.get("role", String.class);
        SecurityPrincipal principal = userDetailsProvider.loadPrincipalByUserId(userId);
        if (userId != null && role != null) {
            List<GrantedAuthority> authority = List.of(new SimpleGrantedAuthority(role));
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, authority);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            throw new BadCredentialsException("인증 과정 중 문제가 발생했습니다.");
        }
    }

    private boolean parseToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token, {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT token, {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token, {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT claims string is empty, {}", e.getMessage());
        }
        return false;
    }

    public SubjectResponseDto loadSubjectByToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return SubjectResponseDto.of(claims.getSubject(), claims.get("email", String.class));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApiException(ErrorCode._INVALID_TOKEN);
        }
    }
}