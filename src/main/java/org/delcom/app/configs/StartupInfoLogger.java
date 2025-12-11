package org.delcom.app.configs;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupInfoLogger implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ConfigurableApplicationContext context = event.getApplicationContext();
        
        // [Branch 1] Guard Clause: Cek jika context null
        if (context == null) {
            return; 
        }

        Environment env = context.getEnvironment();

        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        
        // [Branch 2 & 3] Normalisasi Context Path
        if (contextPath == null) {
            contextPath = "";
        } else if (contextPath.equals("/")) {
            contextPath = "";
        }
        // [Else implicit] Jika path "/app", biarkan apa adanya

        // [Branch 4] LiveReload Check (Ternary Operator)
        boolean liveReloadEnabled = env.getProperty("spring.devtools.livereload.enabled", Boolean.class, false);
        String liveReloadPort = env.getProperty("spring.devtools.livereload.port", "35729");

        String host = env.getProperty("server.address", "localhost");

        String GREEN = "\u001B[32m";
        String CYAN = "\u001B[36m";
        String YELLOW = "\u001B[33m";
        String RESET = "\u001B[0m";

        System.out.println();
        System.out.println(GREEN + "Application started successfully!" + RESET);
        System.out.println(CYAN + "> URL: http://" + host + ":" + port + contextPath + RESET);
        
        // Mencetak baris LiveReload sesuai status true/false
        System.out.println(
                liveReloadEnabled
                        ? (YELLOW + "> LiveReload: ENABLED (port " + liveReloadPort + ")" + RESET)
                        : (YELLOW + "> LiveReload: DISABLED" + RESET));
        System.out.println();
    }
}