package Fruit.Web.projections;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface VariantRow {

    Long getId();

    Long getProductId();
    String getProductName();

    String getSku();
    String getOptionName();
    String getUnit();

    BigDecimal getPrice();
    BigDecimal getCompareAtPrice();
    BigDecimal getWeightKg();

    Boolean getInStock();
    Integer getStockQty();

    OffsetDateTime getUpdatedAt();
}
