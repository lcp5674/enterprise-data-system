package com.enterprise.edams.auth.service.impl;

import com.enterprise.edams.auth.dto.CaptchaResponse;
import com.enterprise.edams.auth.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Random random = new Random();

    @Value("${auth.captcha.expiration:300}")
    private long captchaExpiration;

    @Value("${auth.captcha.length:4}")
    private int captchaLength;

    public CaptchaServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public CaptchaResponse generateCaptcha() {
        // 生成验证码
        String captchaKey = UUID.randomUUID().toString();
        String captchaCode = generateCode();

        // 保存到Redis
        String key = "captcha:" + captchaKey;
        redisTemplate.opsForValue().set(key, captchaCode.toLowerCase(), captchaExpiration, TimeUnit.SECONDS);

        // 生成图片
        String captchaImage = generateImage(captchaCode);

        return CaptchaResponse.builder()
                .captchaKey(captchaKey)
                .captchaImage(captchaImage)
                .expiresIn(captchaExpiration)
                .build();
    }

    @Override
    public boolean verifyCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaCode == null) {
            return false;
        }

        String key = "captcha:" + captchaKey;
        Object storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            return false;
        }

        boolean result = storedCode.toString().equalsIgnoreCase(captchaCode);
        if (result) {
            redisTemplate.delete(key);
        }

        return result;
    }

    @Override
    public void deleteCaptcha(String captchaKey) {
        String key = "captcha:" + captchaKey;
        redisTemplate.delete(key);
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        for (int i = 0; i < captchaLength; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    private String generateImage(String code) {
        int width = 120;
        int height = 40;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 设置字体
        Font font = new Font("Arial", Font.BOLD, 24);
        g.setFont(font);

        // 绘制验证码
        int x = 10;
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
            g.drawString(String.valueOf(code.charAt(i)), x, 28);
            x += 28;
        }

        // 添加干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }

        // 添加噪点
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.fillRect(random.nextInt(width), random.nextInt(height), 2, 2);
        }

        g.dispose();

        // 转换为Base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            log.error("生成验证码图片失败", e);
            return null;
        }
    }
}
