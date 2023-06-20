/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.banburysymphony.orchestra.storage;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.Rollback;

/**
 *
 * @author daves
 */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
@SpringBootTest
public class FileStorageServiceTest {
    @Autowired
    FileStorageService fileStorageService;
    public FileStorageServiceTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    private final String content = "mary had a little lamb";
    private final String name = "_testFile.txt";
    /**
     * Test of storeFile method, of class FileStorageService.
     */
    @Test
    @Order(1)
    public void testStoreFile() throws Exception {
        InputStreamSource file = new InputStreamResource(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        fileStorageService.storeFile(file, name);
    }

    /**
     * Test of getFile method, of class FileStorageService.
     */
    @Test
    @Order(2)
    public void testGetFile() throws Exception {
        Resource result = fileStorageService.getFile(name);
        assertTrue(result != null, "Cannot retrieve test file " + name);
        String s = new String(result.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(content.compareTo(s) == 0, "Test file content is incorrect: expected [" + content + "], found [" + s + "]");
    }

    /**
     * Test of exists method, of class FileStorageService.
     */
    @Test
    @Order(3)
    public void testExists() {
        assertTrue(fileStorageService.exists(name), "test file does not exist");
    }
    
}
