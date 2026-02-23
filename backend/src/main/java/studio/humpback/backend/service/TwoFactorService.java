package studio.humpback.backend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import lombok.RequiredArgsConstructor;
import studio.humpback.backend.dto.TwoFactorSetupResponse;
import studio.humpback.backend.model.User;
import studio.humpback.backend.repository.UserRepository;
import studio.humpback.backend.security.TwoFactorSecretCryptoService;

@Service
@RequiredArgsConstructor
public class TwoFactorService {
    private static final String ERR_2FA_ALREADY_ENABLED = "2FA is already enabled for this account";
    private static final String ERR_2FA_SETUP_NOT_STARTED = "2FA setup not started for this account";
    private static final String ERR_2FA_INVALID_CODE = "Invalid 2FA code";
    private static final String ERR_2FA_SECRET_MISSING = "2FA secret is missing";
    private static final String ERR_2FA_NOT_ENABLED = "2FA is not enabled for this account";
    private static final String ERR_QR_GENERATION_FAILED = "Could not generate QR code for 2FA setup";
    private static final String CODE_REGEX = "\\d{6}";
    private static final String PNG_FORMAT = "PNG";

    private final UserRepository userRepository;
    private final TwoFactorSecretCryptoService twoFactorSecretCryptoService;
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    @Value("${security.2fa.issuer:Humpback Studio}")
    private String issuer;

    public TwoFactorSetupResponse startSetup(User user) {
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new IllegalArgumentException(ERR_2FA_ALREADY_ENABLED);
        }

        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secret = key.getKey();

        user.setTwoFactorSecret(twoFactorSecretCryptoService.encrypt(secret));
        user.setTwoFactorEnabled(Boolean.FALSE);
        userRepository.save(user);

        String account = Optional.ofNullable(user.getEmail())
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElse(user.getUsername());

        String otpAuthUrl = buildOtpAuthUrl(secret, account);
        String qrCodeDataUri = buildQrCodeDataUri(otpAuthUrl);

        return TwoFactorSetupResponse.builder()
                .manualEntryKey(secret)
                .otpAuthUrl(otpAuthUrl)
                .qrCodeDataUri(qrCodeDataUri)
                .twoFactorEnabled(Boolean.FALSE)
                .build();
    }

    public boolean enable(User user, String code) {
        String secret = Optional.of(resolveDecryptedSecret(user, true))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalArgumentException(ERR_2FA_SETUP_NOT_STARTED));

        if (!isCodeValid(secret, code)) {
            throw new IllegalArgumentException(ERR_2FA_INVALID_CODE);
        }

        user.setTwoFactorEnabled(Boolean.TRUE);
        userRepository.save(user);
        return true;
    }

    public boolean disable(User user, String code) {
        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return false;
        }

        String secret = Optional.of(resolveDecryptedSecret(user, true))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalArgumentException(ERR_2FA_SECRET_MISSING));

        if (!isCodeValid(secret, code)) {
            throw new IllegalArgumentException(ERR_2FA_INVALID_CODE);
        }

        user.setTwoFactorEnabled(Boolean.FALSE);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
        return false;
    }

    public boolean verify(User user, String code) {
        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new IllegalArgumentException(ERR_2FA_NOT_ENABLED);
        }

        String secret = Optional.of(resolveDecryptedSecret(user, true))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalArgumentException(ERR_2FA_SECRET_MISSING));

        return isCodeValid(secret, code);
    }

    private boolean isCodeValid(String secret, String rawCode) {
        String normalized = Optional.ofNullable(rawCode)
                .map(String::trim)
                .orElse("");
        if (!normalized.matches(CODE_REGEX)) return false;

        int code = Integer.parseInt(normalized);
        return googleAuthenticator.authorize(secret, code);
    }

    private String buildOtpAuthUrl(String secret, String account) {
        String encodedIssuer = encode(issuer);
        String encodedAccount = encode(account);

        return "otpauth://totp/"
                + encodedIssuer
                + ":"
                + encodedAccount
                + "?secret="
                + secret
                + "&issuer="
                + encodedIssuer;
    }

    private String buildQrCodeDataUri(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix matrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 256, 256);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, PNG_FORMAT, outputStream);
            String encodedPng = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            return "data:image/png;base64," + encodedPng;
        } catch (WriterException | IOException e) {
            throw new IllegalStateException(ERR_QR_GENERATION_FAILED, e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String resolveDecryptedSecret(User user, boolean migrateLegacyPlaintext) {
        String storedSecret = Optional.ofNullable(user.getTwoFactorSecret())
                .map(String::trim)
                .orElse("");
        if (storedSecret.isBlank()) {
            return storedSecret;
        }

        String decryptedSecret = twoFactorSecretCryptoService.decrypt(storedSecret);

        if (migrateLegacyPlaintext && !twoFactorSecretCryptoService.isEncrypted(storedSecret)) {
            user.setTwoFactorSecret(twoFactorSecretCryptoService.encrypt(decryptedSecret));
            userRepository.save(user);
        }

        return decryptedSecret;
    }
}
