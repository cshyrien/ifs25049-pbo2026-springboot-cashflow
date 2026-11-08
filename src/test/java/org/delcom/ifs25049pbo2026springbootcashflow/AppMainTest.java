package org.delcom.ifs25049pbo2026springbootcashflow;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class AppMainTest {

    @Test
    void mainLoadsDotEnvAndDelegatesToSpringApplication() throws Exception {
        String[] args = {"--contoh"};

        try (MockedStatic<SpringApplication> spring = Mockito.mockStatic(SpringApplication.class)) {
            App.main(args);

            spring.verify(() -> SpringApplication.run(App.class, args));
        }
    }
}
