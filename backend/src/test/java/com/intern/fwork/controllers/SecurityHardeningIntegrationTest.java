package com.intern.fwork.controllers;

import com.intern.fwork.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class SecurityHardeningIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void testCorsAllowedOrigin() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    public void testCorsAllowedVercelOrigin() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://fwork-preview.vercel.app")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://fwork-preview.vercel.app"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    public void testCorsDisallowedOrigin() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://malicious-site.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    public void testJwtSecretValidation_RejectsEmptySecret() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "");
        assertThrows(IllegalStateException.class, jwtService::validateConfiguration);
    }

    @Test
    public void testJwtSecretValidation_RejectsWeakSecret() {
        JwtService jwtService = new JwtService();
        // 16 bytes = 128 bits (too short for HMAC-SHA256)
        String shortBase64 = java.util.Base64.getEncoder().encodeToString(new byte[16]);
        ReflectionTestUtils.setField(jwtService, "secretKey", shortBase64);
        assertThrows(IllegalStateException.class, jwtService::validateConfiguration);
    }

    @Test
    public void testJwtSecretValidation_AcceptsStrongSecret() {
        JwtService jwtService = new JwtService();
        // 32 bytes = 256 bits (valid)
        String validBase64 = java.util.Base64.getEncoder().encodeToString(new byte[32]);
        ReflectionTestUtils.setField(jwtService, "secretKey", validBase64);
        jwtService.validateConfiguration(); // Should not throw
    }
}
