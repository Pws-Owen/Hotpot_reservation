package org.li.hotpot_reservationsystem.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 提供JWT Token的生成、解析、验证等功能
 * 使用HS512算法进行签名，Token包含用户ID和用户名信息
 */
@Component
public class JwtUtil {

    /**
     * JWT签名密钥（从配置文件读取）
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT过期时间（毫秒，从配置文件读取）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成JWT Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return createToken(claims, username);
    }

    /**
     * 获取签名密钥
     * 确保密钥长度至少64字节（HS512算法要求）
     * 如果密钥太短，则循环填充到64字节
     * 
     * @return SecretKey签名密钥
     */
    private SecretKey getSigningKey() {
        // 确保密钥长度至少64字节（HS512需要）
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            // 如果密钥太短，循环填充到64字节
            byte[] paddedKey = new byte[64];
            for (int i = 0; i < 64; i++) {
                paddedKey[i] = keyBytes[i % keyBytes.length];
            }
            keyBytes = paddedKey;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 创建JWT Token
     * 
     * @param claims Token中存储的声明信息（如userId、username）
     * @param subject Token主题（通常为用户名）
     * @return JWT Token字符串
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        SecretKey key = getSigningKey();

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 从Token中解析并获取Claims（声明信息）
     * 
     * @param token JWT Token字符串
     * @return Claims对象，包含Token中的所有声明信息
     * @throws io.jsonwebtoken.JwtException 如果Token无效或已过期
     */
    public Claims getClaimsFromToken(String token) {
        SecretKey key = getSigningKey();
        
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 从Token中获取用户名（Token的主题）
     * 
     * @param token JWT Token字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 验证Token是否已过期
     * 
     * @param token JWT Token字符串
     * @return true表示已过期，false表示未过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证Token是否有效
     * 检查Token中的用户名是否匹配，以及Token是否未过期
     * 
     * @param token JWT Token字符串
     * @param username 要验证的用户名
     * @return true表示Token有效，false表示无效
     */
    public Boolean validateToken(String token, String username) {
        String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }
}

