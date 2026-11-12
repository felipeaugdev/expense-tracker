package com.felipeaugdev.dto;

public class ImportResult {
    private int added;
    private int failed;

    public ImportResult(int added, int failed) {
        this.added = added;
        this.failed = failed;
    }

    public int getAdded() {
        return added;
    }

    public int getFailed() {
        return failed;
    }
}
