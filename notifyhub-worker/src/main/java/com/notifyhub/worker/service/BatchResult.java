package com.notifyhub.worker.service;

public record BatchResult(int claimed, int processed, int failed) {
    public static final BatchResult EMPTY = new BatchResult(0, 0, 0);
}
