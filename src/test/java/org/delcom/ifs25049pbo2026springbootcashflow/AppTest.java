package org.delcom.ifs25049pbo2026springbootcashflow;

import org.delcom.ifs25049pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25049pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25049pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AppTest {

    @Autowired
    private FinanceUseCase useCase;

    @Autowired
    private FinancePresenter presenter;

    @Test
    void contextLoads() {
        assertNotNull(useCase);
        assertNotNull(presenter);
    }

    @Test
    void balanceFollowsIncomeAndExpense() throws Exception {
        double before = useCase.getBalance();
        useCase.addIncome("Gaji", 1000);
        useCase.addExpense("Makan", 250);
        assertEquals(before + 750, useCase.getBalance(), 0.001);
    }

    @Test
    void invalidAmountIsRejected() {
        assertThrows(ValidationException.class, () -> useCase.addIncome("Tes", -1));
    }

    @Test
    void invalidIdIsRejected() {
        assertThrows(ValidationException.class, () -> useCase.removeTransaction(0));
    }

    @Test
    void unknownIdIsNotFound() {
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(999999));
    }

    @Test
    void moneyIsFormattedWithThousandsSeparator() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1000000));
    }
}
