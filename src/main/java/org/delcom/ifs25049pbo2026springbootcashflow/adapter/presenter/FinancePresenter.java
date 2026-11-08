package org.delcom.ifs25049pbo2026springbootcashflow.adapter.presenter;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.Transaction;

public class FinancePresenter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public String formatMoney(double amount) {
        return "Rp " + String.format(Locale.US, "%,.2f", amount);
    }

    public String formatTransaction(Transaction t) {
        return "[" + t.getId() + "] " + t.getDescription()
                + " | " + formatMoney(t.getAmount())
                + " | [" + t.getType().label() + "]";
    }

    public String formatBalance(double balance) {
        return "Saldo: " + formatMoney(balance);
    }

    public String formatType(Transaction t) {
        return t.getType().label();
    }

    public String formatCreatedAt(Transaction t) {
        return t.getCreatedAt().format(DATE_FORMAT);
    }

    public String showAddIncomeSuccess(Transaction t) {
        return "Pemasukan berhasil dicatat: " + formatTransaction(t);
    }

    public String showAddExpenseSuccess(Transaction t) {
        return "Pengeluaran berhasil dicatat: " + formatTransaction(t);
    }

    public String showRemoveSuccess() {
        return "Transaksi berhasil dihapus.";
    }

    public String showRemoveFailed(int id) {
        return "Gagal menghapus: transaksi dengan ID " + id + " tidak ditemukan.";
    }

    public String showValidationError(String message) {
        return "Validasi gagal: " + message;
    }

    public String showInvalidAmount() {
        return "Validasi gagal: jumlah harus berupa angka (contoh: 1000000 atau 25000.50)";
    }
}