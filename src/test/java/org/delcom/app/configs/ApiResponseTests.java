package org.delcom.app.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ApiResponseTests {

    @Test
    @DisplayName("Test membuat instance ApiResponse dengan Data String")
    void testCreateApiResponseWithString() {
        // Setup data
        String status = "success";
        String message = "Berhasil memuat data";
        String data = "Hello World";

        // Eksekusi: Membuat object
        ApiResponse<String> response = new ApiResponse<>(status, message, data);

        // Verifikasi (Assert)
        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    @DisplayName("Test membuat instance ApiResponse dengan Data Integer")
    void testCreateApiResponseWithInteger() {
        // Eksekusi
        ApiResponse<Integer> response = new ApiResponse<>("success", "Angka ditemukan", 123);

        // Verifikasi
        assertEquals("success", response.getStatus());
        assertEquals("Angka ditemukan", response.getMessage());
        assertEquals(123, response.getData());
    }

    @Test
    @DisplayName("Test membuat instance ApiResponse dengan Data Null")
    void testCreateApiResponseWithNullData() {
        // Eksekusi
        ApiResponse<Object> response = new ApiResponse<>("fail", "Data tidak ditemukan", null);

        // Verifikasi
        assertEquals("fail", response.getStatus());
        assertEquals("Data tidak ditemukan", response.getMessage());
        assertNull(response.getData());
    }
}