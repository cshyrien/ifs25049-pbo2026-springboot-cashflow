package org.delcom.ifs25049pbo2026springbootcashflow.usecase;

import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Logika bisnis catatan keuangan. Tidak ada I/O (tanpa Vaadin / System.out);
 * kegagalan dilaporkan lewat checked exception domain.
 */
public class FinanceUseCase {

    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    public Transaction addIncome(String description, double amount) throws ValidationException {
        validate(description, amount);
        return repository.saveIncome(description.trim(), amount);
    }

    public Transaction addExpense(String description, double amount) throws ValidationException {
        validate(description, amount);
        return repository.saveExpense(description.trim(), amount);
    }

    public void removeTransaction(int id) throws ValidationException, EntityNotFoundException {
        if (id <= 0) {
            throw new ValidationException("ID tidak valid: harus berupa bilangan bulat positif");
        }
        if (!repository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari berdasarkan keterangan (case-insensitive). Kata kunci kosong = semua data. */
    public List<Transaction> searchTransactions(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return repository.findAll();
        }
        String lower = keyword.trim().toLowerCase();
        return repository.findBy(t -> t.getDescription().toLowerCase().contains(lower));
    }

    public List<Transaction> sortTransactions(SortOption option) {
        return repository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    public double getTotalIncome() {
        return totalOf(TransactionType.INCOME);
    }

    public double getTotalExpense() {
        return totalOf(TransactionType.EXPENSE);
    }

    /** Saldo = total pemasukan - total pengeluaran. */
    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    private double totalOf(TransactionType type) {
        return repository.findAll().stream()
                .filter(t -> t.getType() == type)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private void validate(String description, double amount) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new ValidationException("Jumlah harus berupa angka lebih besar dari 0");
        }
    }
}
