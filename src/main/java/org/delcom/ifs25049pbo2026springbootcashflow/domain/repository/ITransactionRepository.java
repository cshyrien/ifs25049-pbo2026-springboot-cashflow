package org.delcom.ifs25049pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.Transaction;

public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    Transaction saveIncome(String description, double amount);

    Transaction saveExpense(String description, double amount);
}