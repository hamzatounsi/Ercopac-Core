package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ImportFinanceWbsTemplateRequest {

    @NotEmpty
    @Valid
    private List<FinanceWbsTemplateRowDto> rows;

    private boolean replaceExisting = true;

    public List<FinanceWbsTemplateRowDto> getRows() {
        return rows;
    }

    public void setRows(List<FinanceWbsTemplateRowDto> rows) {
        this.rows = rows;
    }

    public boolean isReplaceExisting() {
        return replaceExisting;
    }

    public void setReplaceExisting(boolean replaceExisting) {
        this.replaceExisting = replaceExisting;
    }
}