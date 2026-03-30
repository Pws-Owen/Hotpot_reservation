package org.li.hotpot_reservationsystem.utils;

import org.springframework.dao.DuplicateKeyException;

import java.util.function.Supplier;

/**
 * 重试工具类
 * 用于处理可能因并发冲突导致的操作重试
 */
public class RetryUtil {

    private static final int DEFAULT_MAX_RETRIES = 10;
    private static final int MIN_DELAY_MS = 10;
    private static final int MAX_DELAY_MS = 30;

    /**
     * 执行可能抛出DuplicateKeyException的操作，带重试机制
     *
     * @param supplier 要执行的操作
     * @param errorMessage 失败时的错误消息
     * @param <T> 返回类型
     * @return 操作结果
     * @throws RuntimeException 如果重试次数耗尽
     */
    public static <T> T executeWithRetry(Supplier<T> supplier, String errorMessage) {
        return executeWithRetry(supplier, DEFAULT_MAX_RETRIES, errorMessage);
    }

    /**
     * 执行可能抛出DuplicateKeyException的操作，带重试机制
     *
     * @param supplier 要执行的操作
     * @param maxRetries 最大重试次数
     * @param errorMessage 失败时的错误消息
     * @param <T> 返回类型
     * @return 操作结果
     * @throws RuntimeException 如果重试次数耗尽
     */
    public static <T> T executeWithRetry(Supplier<T> supplier, int maxRetries, String errorMessage) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return supplier.get();
            } catch (DuplicateKeyException e) {
                // 如果是最后一次重试，抛出异常
                if (i == maxRetries - 1) {
                    throw new RuntimeException(errorMessage, e);
                }
                // 等待一小段时间后重试（避免立即重试导致相同结果）
                sleepWithRandomDelay();
            }
        }
        throw new RuntimeException(errorMessage);
    }

    /**
     * 执行可能抛出Exception的操作，带重试机制（用于单号生成）
     *
     * @param supplier 要执行的操作
     * @param maxRetries 最大重试次数
     * @param errorMessage 失败时的错误消息
     * @param <T> 返回类型
     * @return 操作结果
     * @throws RuntimeException 如果重试次数耗尽
     */
    public static <T> T executeWithRetryForException(Supplier<T> supplier, int maxRetries, String errorMessage) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return supplier.get();
            } catch (Exception e) {
                // 如果是最后一次重试，抛出异常
                if (i == maxRetries - 1) {
                    throw new RuntimeException(errorMessage, e);
                }
                // 等待一小段时间后重试
                sleepWithRandomDelay();
            }
        }
        throw new RuntimeException(errorMessage);
    }

    /**
     * 随机延迟等待（10-30ms）
     */
    private static void sleepWithRandomDelay() {
        try {
            long delay = MIN_DELAY_MS + (long)(Math.random() * (MAX_DELAY_MS - MIN_DELAY_MS));
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("操作被中断", ie);
        }
    }
}

