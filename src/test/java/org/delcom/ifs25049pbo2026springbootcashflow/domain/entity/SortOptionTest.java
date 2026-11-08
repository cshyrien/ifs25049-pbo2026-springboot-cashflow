package org.delcom.ifs25049pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOptionTest {

    private final List<Transaction> data = List.of(
            new Transaction(1, "A", 100.0, TransactionType.INCOME),
            new Transaction(2, "B", 500.0, TransactionType.EXPENSE),
            new Transaction(3, "C", 300.0, TransactionType.INCOME),
            new Transaction(4, "D", 200.0, TransactionType.EXPENSE));

    private List<Double> amountsSortedBy(SortOption option) {
        return data.stream().sorted(option.comparator()).map(Transaction::getAmount).toList();
    }

    @Test
    void amountAscendingOrdersSmallestFirst() {
        assertEquals(List.of(100.0, 200.0, 300.0, 500.0), amountsSortedBy(SortOption.AMOUNT_ASC));
    }

    @Test
    void amountDescendingOrdersLargestFirst() {
        assertEquals(List.of(500.0, 300.0, 200.0, 100.0), amountsSortedBy(SortOption.AMOUNT_DESC));
    }

    @Test
    void incomeFirstPutsIncomeBeforeExpense() {
        assertEquals(List.of(100.0, 300.0, 500.0, 200.0), amountsSortedBy(SortOption.INCOME_FIRST));
    }

    @Test
    void expenseFirstPutsExpenseBeforeIncome() {
        assertEquals(List.of(500.0, 200.0, 100.0, 300.0), amountsSortedBy(SortOption.EXPENSE_FIRST));
    }

    @Test
    void everyOptionHasALabel() {
        assertEquals("Jumlah terkecil", SortOption.AMOUNT_ASC.label());
        assertEquals("Jumlah terbesar", SortOption.AMOUNT_DESC.label());
        assertEquals("Pemasukan dulu", SortOption.INCOME_FIRST.label());
        assertEquals("Pengeluaran dulu", SortOption.EXPENSE_FIRST.label());
    }
}
