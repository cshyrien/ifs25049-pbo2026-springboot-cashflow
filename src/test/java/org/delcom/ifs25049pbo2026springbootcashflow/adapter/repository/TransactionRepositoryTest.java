package org.delcom.ifs25049pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveIncomeAndExpenseAssignIncrementingIds() {
        Transaction income = repository.saveIncome("Gaji", 1000);
        Transaction expense = repository.saveExpense("Makan", 50);

        assertEquals(1, income.getId());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(2, expense.getId());
        assertEquals(TransactionType.EXPENSE, expense.getType());
    }

    @Test
    void saveStoresGivenEntity() {
        Transaction t = new Transaction(7, "Manual", 10, TransactionType.INCOME);

        assertEquals(t, repository.save(t));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        repository.saveIncome("Gaji", 1000);

        List<Transaction> copy = repository.findAll();
        copy.clear();

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findByIdReturnsMatchOrEmpty() {
        Transaction saved = repository.saveIncome("Gaji", 1000);

        Optional<Transaction> found = repository.findById(saved.getId());
        Optional<Transaction> missing = repository.findById(99);

        assertTrue(found.isPresent());
        assertEquals("Gaji", found.get().getDescription());
        assertFalse(missing.isPresent());
    }

    @Test
    void deleteByIdReportsWhetherSomethingWasRemoved() {
        Transaction saved = repository.saveIncome("Gaji", 1000);

        assertTrue(repository.deleteById(saved.getId()));
        assertFalse(repository.deleteById(saved.getId()));
        assertEquals(0, repository.findAll().size());
    }

    @Test
    void findByFiltersWithPredicate() {
        repository.saveIncome("Gaji", 1000);
        repository.saveExpense("Makan", 50);

        List<Transaction> expenses = repository.findBy(t -> t.getType() == TransactionType.EXPENSE);

        assertEquals(1, expenses.size());
        assertEquals("Makan", expenses.get(0).getDescription());
    }

    @Test
    void updateKeepsInMemoryObjectChanges() {
        Transaction saved = repository.saveIncome("Gaji", 1000);

        saved.changeDescription("Gaji Bulanan");
        repository.update(saved);

        assertEquals("Gaji Bulanan", repository.findById(saved.getId()).get().getDescription());
    }
}
