package org.delcom.ifs25049pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25049pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25049pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web catatan keuangan (Vaadin).
 * Semua aturan bisnis ada di use case; view hanya menangani input, tampilan, dan notifikasi.
 * Anggota ber-akses package-private sengaja dibuka agar bisa diuji tanpa browser.
 */
@Route("")
@PageTitle("Cashflow - Catatan Keuangan")
public class FinanceView extends VerticalLayout {

    // Warna dipilih agar kontras teks terhadap latar minimal 4.5:1 (WCAG AA).
    private static final String INCOME_TEXT = "#166534";
    private static final String INCOME_BG = "#DCFCE7";
    private static final String EXPENSE_TEXT = "#991B1B";
    private static final String EXPENSE_BG = "#FEE2E2";
    private static final String DANGER_BG = "#B91C1C";

    private final FinanceUseCase useCase;
    private final FinancePresenter presenter;

    final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    final TextField searchField = new TextField("Cari keterangan");
    final Select<SortOption> sortSelect = new Select<>();
    final Paragraph statusLabel = new Paragraph();
    final Span emptyState = new Span("- Belum ada transaksi!");

    final Span totalCount = new Span("0");
    final Span totalIncome = new Span("Rp 0.00");
    final Span totalExpense = new Span("Rp 0.00");
    final Span balance = new Span("Rp 0.00");

    final Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), e -> searchTransactions());
    final Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> refreshAll());
    final Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(), e -> openAddDialog(true));
    final Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(), e -> openAddDialog(false));

    // Dialog yang sedang aktif (form tambah atau konfirmasi hapus).
    Dialog dialog;
    TextField descriptionField;
    TextField amountField;
    Button confirmButton;
    Button cancelButton;

    public FinanceView(FinanceUseCase useCase, FinancePresenter presenter) {
        this.useCase = useCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("max-width", "1200px").set("margin", "0 auto");
        getElement().setAttribute("role", "main");

        H1 title = new H1("Cashflow");
        title.getStyle().set("margin", "0");

        Paragraph subtitle = new Paragraph("Catat pemasukan dan pengeluaran, lalu pantau saldo Anda secara langsung.");
        subtitle.getStyle().set("margin", "0").set("color", "#374151");

        configureGrid();
        configureToolbar();

        add(title, subtitle, buildSummaryCards(), buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Bahasa halaman dan deskripsi meta (dibaca oleh pembaca layar dan mesin pencari).
        attachEvent.getUI().getPage().executeJs(
                "document.documentElement.lang = 'id';"
                        + "let m = document.querySelector('meta[name=description]');"
                        + "if (!m) { m = document.createElement('meta'); m.name = 'description'; document.head.appendChild(m); }"
                        + "m.content = 'Cashflow: aplikasi pencatat pemasukan dan pengeluaran dengan saldo otomatis.';");
    }

    // ---------- Kartu ringkasan ----------

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout cards = new HorizontalLayout(
                createCard("Total Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                createCard("Total Pemasukan", totalIncome, INCOME_BG, "#16A34A", INCOME_TEXT),
                createCard("Total Pengeluaran", totalExpense, EXPENSE_BG, "#DC2626", EXPENSE_TEXT),
                createCard("Saldo", balance, "#FEF3C7", "#D97706", "#92400E")
        );
        cards.setWidthFull();
        cards.setSpacing(true);
        cards.getStyle().set("align-items", "stretch").set("flex-wrap", "wrap");
        return cards;
    }

    private Div createCard(String label, Span value, String background, String accent, String textColor) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.9rem")
                .set("font-weight", "600")
                .set("color", textColor);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", textColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1 1 200px")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        totalCount.setText(String.valueOf(useCase.getAllTransactions().size()));
        totalIncome.setText(presenter.formatMoney(useCase.getTotalIncome()));
        totalExpense.setText(presenter.formatMoney(useCase.getTotalExpense()));
        balance.setText(presenter.formatMoney(useCase.getBalance()));
    }

    // ---------- Grid ----------

    private void configureGrid() {
        grid.getElement().setAttribute("aria-label", "Daftar transaksi keuangan");

        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addComponentColumn(this::createAmountText)
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(SortOption.AMOUNT_ASC.comparator());

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(SortOption.INCOME_FIRST.comparator());

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.setSizeFull();
    }

    Span createAmountText(Transaction t) {
        Span amount = new Span(presenter.formatMoney(t.getAmount()));
        amount.getStyle()
                .set("font-weight", "600")
                .set("color", t.getType() == TransactionType.INCOME ? INCOME_TEXT : EXPENSE_TEXT);
        return amount;
    }

    Span createTypeBadge(Transaction t) {
        boolean income = t.getType() == TransactionType.INCOME;
        Span badge = new Span(presenter.formatType(t));
        badge.getStyle()
                .set("display", "inline-block")
                .set("padding", "0.15rem 0.65rem")
                .set("border-radius", "999px")
                .set("font-size", "0.8125rem")
                .set("font-weight", "600")
                .set("background", income ? INCOME_BG : EXPENSE_BG)
                .set("color", income ? INCOME_TEXT : EXPENSE_TEXT);
        return badge;
    }

    HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), e -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.getElement().setAttribute("aria-label", "Hapus transaksi nomor " + transaction.getId());
        return new HorizontalLayout(deleteButton);
    }

    // ---------- Toolbar ----------

    private void configureToolbar() {
        searchField.setPlaceholder("contoh: gaji");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        sortSelect.setLabel("Urutkan");
        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(SortOption::label);
        sortSelect.setPlaceholder("Pilih urutan");
        sortSelect.addValueChangeListener(event -> {
            SortOption option = event.getValue();
            if (option != null) {
                refreshGrid(useCase.sortTransactions(option), "Diurutkan: " + option.label());
            }
        });

        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        incomeButton.getStyle().set("background", "#15803D").set("color", "#FFFFFF");

        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        expenseButton.getStyle().set("background", DANGER_BG).set("color", "#FFFFFF");
    }

    private HorizontalLayout buildToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortSelect, refreshButton, incomeButton, expenseButton);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        toolbar.getStyle().set("flex-wrap", "wrap");
        return toolbar;
    }

    // ---------- Dialog ----------

    void openAddDialog(boolean income) {
        descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();

        amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("contoh: 1000000");
        amountField.setWidthFull();

        dialog = new Dialog();
        dialog.setHeaderTitle(income ? "Tambah Pemasukan" : "Tambah Pengeluaran");
        dialog.setWidth("450px");

        VerticalLayout body = new VerticalLayout(descriptionField, amountField);
        body.setPadding(false);
        dialog.add(body);

        confirmButton = new Button("Simpan", VaadinIcon.CHECK.create(), e -> submitTransaction(income));
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());
        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
    }

    void submitTransaction(boolean income) {
        double amount;
        try {
            amount = Double.parseDouble(amountField.getValue().trim());
        } catch (NumberFormatException ex) {
            showError(presenter.showInvalidAmount());
            return;
        }
        try {
            if (income) {
                showSuccess(presenter.showAddIncomeSuccess(useCase.addIncome(descriptionField.getValue(), amount)));
            } else {
                showSuccess(presenter.showAddExpenseSuccess(useCase.addExpense(descriptionField.getValue(), amount)));
            }
            dialog.close();
            refreshAll();
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    void openDeleteDialog(Transaction transaction) {
        dialog = new Dialog();
        dialog.setHeaderTitle("Hapus Transaksi");
        dialog.setWidth("450px");
        dialog.add(new Paragraph("Yakin ingin menghapus " + presenter.formatTransaction(transaction) + "?"));

        confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), e -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.getStyle().set("background", DANGER_BG).set("color", "#FFFFFF");

        cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());
        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
    }

    // ---------- Aksi ----------

    void deleteTransaction(Transaction transaction) {
        try {
            useCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        }
        refreshAll();
    }

    void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword.isBlank()) {
            refreshAll();
            return;
        }
        emptyState.setText("- Transaksi tidak ditemukan!");
        refreshGrid(useCase.searchTransactions(keyword), "Hasil pencarian: \"" + keyword.trim() + "\"");
    }

    void refreshAll() {
        emptyState.setText("- Belum ada transaksi!");
        refreshGrid(useCase.getAllTransactions(), "Daftar Transaksi");
        searchField.clear();
        sortSelect.clear();
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary();
        statusLabel.setText(title + " — " + presenter.formatBalance(useCase.getBalance()));
    }

    private void showSuccess(String message) {
        Notification n = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification n = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
