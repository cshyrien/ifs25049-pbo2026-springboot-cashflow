package org.delcom.ifs25049pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionTest {

    @Test
    void constructorStoresValuesAndSetsCreationTime() {
        Transaction t = new Transaction(1, "Gaji", 1000.0, TransactionType.INCOME);

        assertEquals(1, t.getId());
        assertEquals("Gaji", t.getDescription());
        assertEquals(1000.0, t.getAmount(), 0.0001);
        assertEquals(TransactionType.INCOME, t.getType());
        assertNotNull(t.getCreatedAt());
    }

    @Test
    void constructorAcceptsExplicitCreationTime() {
        LocalDateTime time = LocalDateTime.of(2026, 1, 2, 3, 4);
        Transaction t = new Transaction(2, "Makan", 50.0, TransactionType.EXPENSE, time);

        assertEquals(time, t.getCreatedAt());
        assertEquals(TransactionType.EXPENSE, t.getType());
    }

    @Test
    void changeMethodsUpdateDescriptionAndAmount() {
        Transaction t = new Transaction(3, "Lama", 10.0, TransactionType.EXPENSE);

        t.changeDescription("Baru");
        t.changeAmount(99.5);

        assertEquals("Baru", t.getDescription());
        assertEquals(99.5, t.getAmount(), 0.0001);
        assertEquals(3, t.getId());
    }

    @Test
    void transactionTypeHasReadableLabels() {
        assertEquals("Pemasukan", TransactionType.INCOME.label());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.label());
    }
}
