package com.ercopac.ercopac_tracker.projectum.finance.dto;

import java.util.ArrayList;
import java.util.List;

public class FinanceImportResultDto {
    private int updatedRows;
    private int unchangedRows;
    private List<String> messages = new ArrayList<>();

    public int getUpdatedRows() { return updatedRows; }
    public void setUpdatedRows(int updatedRows) { this.updatedRows = updatedRows; }

    public int getUnchangedRows() { return unchangedRows; }
    public void setUnchangedRows(int unchangedRows) { this.unchangedRows = unchangedRows; }

    public List<String> getMessages() { return messages; }
    public void setMessages(List<String> messages) { this.messages = messages; }
}
