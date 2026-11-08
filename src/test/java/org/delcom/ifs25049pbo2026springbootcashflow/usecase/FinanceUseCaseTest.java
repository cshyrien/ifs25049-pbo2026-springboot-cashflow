package org.delcom.ifs25049pbo2026springbootcashflow.usecase;

import org.delcom.ifs25049pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinanceUseCaseTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    private void seed() throws ValidationException {
        useCase.addIncome("Gaji", 5000000);
        useCase.addExpense("Makan", 25000);
        useCase.addIncome("Bonus", 100);
        useCase.addExpense("Kopi", 300);
    }

    @Test
    void addIncomeAndExpenseTrimDescriptionAndSetType() throws ValidationException {
        Transaction income = useCase.addIncome("  Gaji  ", 1000);
        Transaction expense = useCase.addExpense("Makan", 250);

        assertEquals("Gaji", income.getDescription());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals(2, useCase.getAllTransactions().size());
    }

    @Test
    void blankOrNullDescriptionIsRejected() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(null, 10));
        assertThrows(ValidationException.class, () -> useCase.addIncome("", 10));
        assertThrows(ValidationException.class, () -> useCase.addExpense("   ", 10));
    }

    @Test
    void nonPositiveOrNonFiniteAmountIsRejected() {
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", 0));
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", -5));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x", Double.NaN));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x", Double.POSITIVE_INFINITY));
        assertEquals(0, useCase.getAllTransactions().size());
    }

    @Test
    void balanceIsIncomeMinusExpense() throws ValidationException {
        seed();

        assertEquals(5000100.0, useCase.getTotalIncome(), 0.0001);
        assertEquals(25300.0, useCase.getTotalExpense(), 0.0001);
        assertEquals(4974800.0, useCase.getBalance(), 0.0001);
    }

    @Test
    void balanceStaysConsistentAfterRemove() throws Exception {
        Transaction income = useCase.addIncome("Gaji", 1000);
        Transaction expense = useCase.addExpense("Makan", 250);
        assertEquals(750.0, useCase.getBalance(), 0.0001);

        useCase.removeTransaction(expense.getId());
        assertEquals(1000.0, useCase.getBalance(), 0.0001);

        useCase.removeTransaction(income.getId());
        assertEquals(0.0, useCase.getBalance(), 0.0001);
    }

    @Test
    void removeRejectsInvalidAndUnknownIds() {
        assertThrows(ValidationException.class, () -> useCase.removeTransaction(0));
        assertThrows(ValidationException.class, () -> useCase.removeTransaction(-1));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(99));
    }

    @Test
    void searchIsCaseInsensitiveAndDoesNotChangeData() throws ValidationException {
        seed();

        assertEquals(1, useCase.searchTransactions("GAJ").size());
        assertEquals(0, useCase.searchTransactions("tidak ada").size());
        assertEquals(4, useCase.getAllTransactions().size());
    }

    @Test
    void emptySearchReturnsEverything() throws ValidationException {
        seed();

        assertEquals(4, useCase.searchTransactions(null).size());
        assertEquals(4, useCase.searchTransactions("").size());
        assertEquals(4, useCase.searchTransactions("   ").size());
    }

    private List<Double> amounts(SortOption option) {
        return useCase.sortTransactions(option).stream().map(Transaction::getAmount).toList();
    }

    @Test
    void sortUsesDomainComparators() throws ValidationException {
        seed();

        assertEquals(List.of(100.0, 300.0, 25000.0, 5000000.0), amounts(SortOption.AMOUNT_ASC));
        assertEquals(List.of(5000000.0, 25000.0, 300.0, 100.0), amounts(SortOption.AMOUNT_DESC));
        assertEquals(List.of(5000000.0, 100.0, 25000.0, 300.0), amounts(SortOption.INCOME_FIRST));
        assertEquals(List.of(25000.0, 300.0, 5000000.0, 100.0), amounts(SortOption.EXPENSE_FIRST));
    }
}
