package com.codingshuttle.youtube.hospitalManagement.config;
//package com.clinic.doctorappointment.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class GoogleCalendarConfig {

    /**
     * Provides a singleton JsonFactory for Google API client libraries.
     */
    @Bean
    public JsonFactory jsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    /**
     * Provides a singleton NetHttpTransport for Google API client libraries.
     * This bean is created once to be reused across the application.
     * @return A trusted NetHttpTransport instance.
     * @throws GeneralSecurityException if the transport cannot be initialized.
     * @throws IOException if an I/O error occurs.
     */
    @Bean
    public NetHttpTransport netHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }
}
