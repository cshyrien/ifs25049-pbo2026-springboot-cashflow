package org.delcom.ifs25049pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.repository.ITransactionRepository;

public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {

    private int nextId = 1;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction saveIncome(String description, double amount) {
        return save(new Transaction(nextId++, description, amount, TransactionType.INCOME));
    }

    @Override
    public Transaction saveExpense(String description, double amount) {
        return save(new Transaction(nextId++, description, amount, TransactionType.EXPENSE));
    }
}