package org.delcom.ifs25049pbo2026springbootcashflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DotEnvTest {

    private static final String[] KEYS = {
            "DOTENV_TEST_A", "DOTENV_TEST_B", "DOTENV_TEST_C", "DOTENV_TEST_D",
            "DOTENV_TEST_E", "DOTENV_TEST_F", "DOTENV_TEST_G", "DOTENV_TEST_H", "noequals"
    };

    @AfterEach
    void cleanUp() {
        for (String key : KEYS) {
            System.clearProperty(key);
        }
    }

    @Test
    void loadIgnoresMissingFile() throws IOException {
        DotEnv.load(Path.of("file-yang-tidak-ada.env"));

        assertNull(System.getProperty("DOTENV_TEST_A"));
    }

    @Test
    void loadReadsKeyValuePairsAndSkipsCommentsAndBlankLines() throws IOException {
        Path file = Files.createTempFile("dotenv", ".env");
        try {
            Files.writeString(file, "# komentar\n\nDOTENV_TEST_A=1\nDOTENV_TEST_B=\"dua\"\n");

            DotEnv.load(file);

            assertEquals("1", System.getProperty("DOTENV_TEST_A"));
            assertEquals("dua", System.getProperty("DOTENV_TEST_B"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void loadWithoutArgumentsReadsDotEnvInWorkingDirectory() throws IOException {
        DotEnv.load();
        System.clearProperty("APP_PORT");
    }

    @Test
    void invalidLinesAreIgnored() {
        DotEnv.applyLine("   ");
        DotEnv.applyLine("# komentar");
        DotEnv.applyLine("noequals");
        DotEnv.applyLine("=tanpa-kunci");

        assertNull(System.getProperty("noequals"));
    }

    @Test
    void existingSystemPropertyIsNotOverridden() {
        System.setProperty("DOTENV_TEST_C", "asli");

        DotEnv.applyLine("DOTENV_TEST_C=baru");

        assertEquals("asli", System.getProperty("DOTENV_TEST_C"));
    }

    @Test
    void existingEnvironmentVariableIsNotOverridden() {
        String envName = System.getenv().keySet().iterator().next();
        String before = System.getProperty(envName);

        DotEnv.applyLine(envName + "=nilai-dari-dotenv");

        assertEquals(before, System.getProperty(envName));
    }

    @Test
    void quotesAreStrippedOnlyWhenTheyMatch() {
        DotEnv.applyLine("DOTENV_TEST_D=\"abc\"");
        DotEnv.applyLine("DOTENV_TEST_E='abc'");
        DotEnv.applyLine("DOTENV_TEST_F=\"abc'");
        DotEnv.applyLine("DOTENV_TEST_G='abc\"");
        DotEnv.applyLine("DOTENV_TEST_H=x");

        assertEquals("abc", System.getProperty("DOTENV_TEST_D"));
        assertEquals("abc", System.getProperty("DOTENV_TEST_E"));
        assertEquals("\"abc'", System.getProperty("DOTENV_TEST_F"));
        assertEquals("'abc\"", System.getProperty("DOTENV_TEST_G"));
        assertEquals("x", System.getProperty("DOTENV_TEST_H"));
    }

    @Test
    void plainValueAndEmptyValueAreKept() {
        DotEnv.applyLine("DOTENV_TEST_A=polos");
        DotEnv.applyLine("DOTENV_TEST_B=");

        assertEquals("polos", System.getProperty("DOTENV_TEST_A"));
        assertEquals("", System.getProperty("DOTENV_TEST_B"));
    }
}
