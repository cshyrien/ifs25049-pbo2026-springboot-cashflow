package org.delcom.ifs25049pbo2026springbootcashflow.domain.entity;

import java.util.Comparator;

public enum SortOption {
    AMOUNT_ASC("Jumlah terkecil",
            Comparator.comparingDouble(Transaction::getAmount)),
    AMOUNT_DESC("Jumlah terbesar",
            Comparator.comparingDouble(Transaction::getAmount).reversed()),
    INCOME_FIRST("Pemasukan dulu",
            Comparator.comparing((Transaction t) -> t.getType() != TransactionType.INCOME)),
    EXPENSE_FIRST("Pengeluaran dulu",
            Comparator.comparing((Transaction t) -> t.getType() != TransactionType.EXPENSE));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String label() {
        return label;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}