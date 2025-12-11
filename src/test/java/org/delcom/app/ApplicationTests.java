package org.delcom.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class ApplicationTest {

    @Test
    @DisplayName("Test Main Method (Mock Static SpringApplication)")
    void testMain() {
        // Kita mock static method SpringApplication.run()
        // Tujuannya: Agar baris di dalam main() tereksekusi, TAPI server tidak beneran start
        // (menghindari error port already in use)
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            
            // Setup: Ketika SpringApplication.run dipanggil, kembalikan context kosong (mock)
            mocked.when(() -> SpringApplication.run(Application.class, new String[]{}))
                  .thenReturn(mock(ConfigurableApplicationContext.class));

            // Act: Panggil method main
            Application.main(new String[]{});

            // Assert: Verifikasi bahwa SpringApplication.run benar-benar dipanggil
            mocked.verify(() -> SpringApplication.run(Application.class, new String[]{}));
        }
    }

    @Test
    @DisplayName("Test Constructor (Untuk Coverage 100%)")
    void testConstructor() {
        // Test ini penting!
        // JaCoCo menghitung "default constructor" implicit sebagai kode yang harus dites.
        // Kita panggil new Application() agar baris "public class Application" dianggap tercover.
        assertDoesNotThrow(() -> new Application());
    }
}