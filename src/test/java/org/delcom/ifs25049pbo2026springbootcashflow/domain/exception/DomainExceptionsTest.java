package org.delcom.ifs25049pbo2026springbootcashflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainExceptionsTest {

    @Test
    void domainExceptionKeepsMessage() {
        DomainException ex = new DomainException("pesan");
        assertEquals("pesan", ex.getMessage());
    }

    @Test
    void domainExceptionKeepsCause() {
        RuntimeException cause = new RuntimeException("akar");
        DomainException ex = new DomainException("pesan", cause);

        assertEquals("pesan", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void entityNotFoundExposesIdAndMessage() {
        EntityNotFoundException ex = new EntityNotFoundException(42);

        assertEquals(42, ex.getId());
        assertEquals("Transaksi dengan ID 42 tidak ditemukan.", ex.getMessage());
        assertTrue(ex instanceof DomainException);
    }

    @Test
    void validationExceptionKeepsMessage() {
        ValidationException ex = new ValidationException("tidak valid");

        assertEquals("tidak valid", ex.getMessage());
        assertTrue(ex instanceof DomainException);
    }
}
