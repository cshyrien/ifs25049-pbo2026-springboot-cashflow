package org.delcom.ifs25049pbo2026springbootcashflow.framework.view;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.delcom.ifs25049pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25049pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25049pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Menguji FinanceView tanpa browser memakai Karibu Testing (MockVaadin). */
class FinanceViewTest {

    private FinanceUseCase useCase;
    private FinanceView view;

    @BeforeEach
    void setUp() {
        MockVaadin.setup();
        useCase = new FinanceUseCase(new TransactionRepository());
        view = new FinanceView(useCase, new FinancePresenter());
        UI.getCurrent().add(view);
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    private int itemCount() {
        return view.grid.getListDataView().getItemCount();
    }

    private List<String> descriptions() {
        return view.grid.getListDataView().getItems().map(Transaction::getDescription).toList();
    }

    private void submit(boolean income, String description, String amount) {
        if (income) {
            view.incomeButton.click();
        } else {
            view.expenseButton.click();
        }
        view.descriptionField.setValue(description);
        view.amountField.setValue(amount);
        view.confirmButton.click();
    }

    private void seed() {
        submit(true, "Gaji", "5000000");
        submit(false, "Makan", "25000");
        submit(true, "Bonus", "100");
    }

    @Test
    void initialStateIsEmpty() {
        assertEquals(0, itemCount());
        assertEquals("0", view.totalCount.getText());
        assertEquals("Rp 0.00", view.totalIncome.getText());
        assertEquals("Rp 0.00", view.totalExpense.getText());
        assertEquals("Rp 0.00", view.balance.getText());
        assertEquals("Daftar Transaksi — Saldo: Rp 0.00", view.statusLabel.getText());
    }

    @Test
    void addingIncomeAndExpenseUpdatesSummaryCards() {
        submit(true, "Gaji", "5000000");
        submit(false, "Makan", "25000");

        assertEquals(2, itemCount());
        assertEquals("2", view.totalCount.getText());
        assertEquals("Rp 5,000,000.00", view.totalIncome.getText());
        assertEquals("Rp 25,000.00", view.totalExpense.getText());
        assertEquals("Rp 4,975,000.00", view.balance.getText());
        assertEquals("Daftar Transaksi — Saldo: Rp 4,975,000.00", view.statusLabel.getText());
    }

    @Test
    void dialogTitleMatchesTransactionType() {
        view.incomeButton.click();
        assertEquals("Tambah Pemasukan", view.dialog.getHeaderTitle());
        assertTrue(view.dialog.isOpened());

        view.expenseButton.click();
        assertEquals("Tambah Pengeluaran", view.dialog.getHeaderTitle());
    }

    @Test
    void cancelClosesTheDialogWithoutSaving() {
        view.incomeButton.click();
        view.descriptionField.setValue("Tidak jadi");
        view.amountField.setValue("100");

        view.cancelButton.click();

        assertFalse(view.dialog.isOpened());
        assertEquals(0, itemCount());
    }

    @Test
    void nonNumericAmountIsRejectedAndDialogStaysOpen() {
        submit(true, "Gaji", "abc");

        assertEquals(0, itemCount());
        assertTrue(view.dialog.isOpened());
    }

    @Test
    void invalidDomainInputIsRejectedForIncomeAndExpense() {
        submit(true, "", "1000");
        assertEquals(0, itemCount());

        submit(false, "Makan", "-5");
        assertEquals(0, itemCount());
        assertTrue(view.dialog.isOpened());
    }

    @Test
    void searchFiltersByDescriptionWithoutChangingData() {
        seed();

        view.searchField.setValue("gaj");
        view.searchButton.click();

        assertEquals(List.of("Gaji"), descriptions());
        assertEquals("Hasil pencarian: \"gaj\" — Saldo: Rp 4,975,100.00", view.statusLabel.getText());
        assertEquals("3", view.totalCount.getText());
    }

    @Test
    void searchWithoutMatchShowsNotFoundMessage() {
        seed();

        view.searchField.setValue("zzz");
        view.searchButton.click();

        assertEquals(0, itemCount());
        assertEquals("- Transaksi tidak ditemukan!", view.emptyState.getText());
    }

    @Test
    void blankSearchAndRefreshRestoreFullList() {
        seed();
        view.searchField.setValue("gaj");
        view.searchButton.click();

        view.searchField.setValue("   ");
        view.searchButton.click();
        assertEquals(3, itemCount());

        view.searchField.setValue("gaj");
        view.searchButton.click();
        view.refreshButton.click();
        assertEquals(3, itemCount());
        assertEquals("", view.searchField.getValue());
        assertEquals("- Belum ada transaksi!", view.emptyState.getText());
    }

    @Test
    void sortSelectUsesDomainSortOptions() {
        seed();

        view.sortSelect.setValue(SortOption.AMOUNT_ASC);
        assertEquals(List.of("Bonus", "Makan", "Gaji"), descriptions());

        view.sortSelect.setValue(SortOption.AMOUNT_DESC);
        assertEquals(List.of("Gaji", "Makan", "Bonus"), descriptions());

        view.sortSelect.setValue(SortOption.INCOME_FIRST);
        assertEquals(List.of("Gaji", "Bonus", "Makan"), descriptions());

        view.sortSelect.setValue(SortOption.EXPENSE_FIRST);
        assertEquals(List.of("Makan", "Gaji", "Bonus"), descriptions());
        assertEquals("Diurutkan: Pengeluaran dulu — Saldo: Rp 4,975,100.00", view.statusLabel.getText());

        view.refreshButton.click();
        assertNull(view.sortSelect.getValue());
        assertEquals("Daftar Transaksi — Saldo: Rp 4,975,100.00", view.statusLabel.getText());
    }

    @Test
    void deleteWithConfirmationRemovesTransactionAndUpdatesBalance() {
        submit(true, "Gaji", "1000");
        submit(false, "Makan", "250");
        Transaction expense = useCase.getAllTransactions().get(1);

        HorizontalLayout actions = view.createActionButtons(expense);
        Button deleteButton = (Button) actions.getComponentAt(0);
        deleteButton.click();
        assertEquals("Hapus Transaksi", view.dialog.getHeaderTitle());

        view.confirmButton.click();

        assertEquals(1, itemCount());
        assertEquals("Rp 1,000.00", view.balance.getText());
        assertFalse(view.dialog.isOpened());
    }

    @Test
    void cancelOnDeleteDialogKeepsTheTransaction() {
        submit(true, "Gaji", "1000");
        Transaction income = useCase.getAllTransactions().get(0);

        view.openDeleteDialog(income);
        view.cancelButton.click();

        assertEquals(1, itemCount());
        assertFalse(view.dialog.isOpened());
    }

    @Test
    void deleteHandlesInvalidAndUnknownIdsGracefully() {
        submit(true, "Gaji", "1000");

        view.deleteTransaction(new Transaction(0, "ID tidak valid", 1, TransactionType.INCOME));
        view.deleteTransaction(new Transaction(99, "Tidak ada", 1, TransactionType.EXPENSE));

        assertEquals(1, itemCount());
    }

    @Test
    void amountAndTypeComponentsAreColoredByType() {
        Transaction income = new Transaction(1, "Gaji", 1000, TransactionType.INCOME);
        Transaction expense = new Transaction(2, "Makan", 50, TransactionType.EXPENSE);

        assertEquals("Rp 1,000.00", view.createAmountText(income).getText());
        assertEquals("#166534", view.createAmountText(income).getStyle().get("color"));
        assertEquals("#991B1B", view.createAmountText(expense).getStyle().get("color"));

        assertEquals("Pemasukan", view.createTypeBadge(income).getText());
        assertEquals("#DCFCE7", view.createTypeBadge(income).getStyle().get("background"));
        assertEquals("Pengeluaran", view.createTypeBadge(expense).getText());
        assertEquals("#FEE2E2", view.createTypeBadge(expense).getStyle().get("background"));
    }

    @Test
    void viewIsAttachedWithAccessibleStructure() {
        assertNotNull(view.getElement().getAttribute("role"));
        assertEquals("main", view.getElement().getAttribute("role"));
        assertEquals("Daftar transaksi keuangan", view.grid.getElement().getAttribute("aria-label"));
    }
}
