import java.math.BigDecimal;
import java.time.YearMonth;

public class MonthOverMonthReport {

    private final YearMonth previousMonth;
    private final YearMonth currentMonth;
    private final BigDecimal previousTotal;
    private final BigDecimal currentTotal;
    private final BigDecimal difference;
    private final BigDecimal percentageChange;

    public MonthOverMonthReport(YearMonth previousMonth, YearMonth currentMonth,
            BigDecimal previousTotal, BigDecimal currentTotal,
            BigDecimal difference, BigDecimal percentageChange) {

        this.previousMonth = previousMonth;
        this.currentMonth = currentMonth;
        this.previousTotal = previousTotal;
        this.currentTotal = currentTotal;
        this.difference = difference;
        this.percentageChange = percentageChange;

    }

    public YearMonth getPreviousMonth() {
        return previousMonth;
    }

    public YearMonth getCurrentMonth() {
        return currentMonth;
    }

    public BigDecimal getPreviousTotal() {
        return previousTotal;
    }

    public BigDecimal getCurrentTotal() {
        return currentTotal;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public BigDecimal getPercentageChange() {
        return percentageChange;
    }

}
