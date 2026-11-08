package org.delcom.ifs25049pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction income = new Transaction(
            1, "Gaji", 5000000, TransactionType.INCOME, LocalDateTime.of(2026, 11, 5, 11, 30));
    private final Transaction expense = new Transaction(
            2, "Makan", 25000.5, TransactionType.EXPENSE, LocalDateTime.of(2026, 1, 2, 3, 4));

    @Test
    void formatMoneyUsesThousandsSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1000000));
        assertEquals("Rp 0.00", presenter.formatMoney(0));
        assertEquals("Rp 25,000.50", presenter.formatMoney(25000.5));
    }

    @Test
    void formatTransactionFollowsReferenceFormat() {
        assertEquals("[1] Gaji | Rp 5,000,000.00 | [Pemasukan]", presenter.formatTransaction(income));
        assertEquals("[2] Makan | Rp 25,000.50 | [Pengeluaran]", presenter.formatTransaction(expense));
    }

    @Test
    void formatBalanceFollowsReferenceFormat() {
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4975000));
    }

    @Test
    void formatTypeAndDate() {
        assertEquals("Pemasukan", presenter.formatType(income));
        assertEquals("Pengeluaran", presenter.formatType(expense));
        assertEquals("05-11-2026 11:30", presenter.formatCreatedAt(income));
    }

    @Test
    void messagesAreClearForUsers() {
        assertEquals("Pemasukan berhasil dicatat: [1] Gaji | Rp 5,000,000.00 | [Pemasukan]",
                presenter.showAddIncomeSuccess(income));
        assertEquals("Pengeluaran berhasil dicatat: [2] Makan | Rp 25,000.50 | [Pengeluaran]",
                presenter.showAddExpenseSuccess(expense));
        assertEquals("Transaksi berhasil dihapus.", presenter.showRemoveSuccess());
        assertEquals("Gagal menghapus: transaksi dengan ID 9 tidak ditemukan.", presenter.showRemoveFailed(9));
        assertEquals("Validasi gagal: pesan", presenter.showValidationError("pesan"));
        assertEquals("Validasi gagal: jumlah harus berupa angka (contoh: 1000000 atau 25000.50)",
                presenter.showInvalidAmount());
    }
}
