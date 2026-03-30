package org.li.hotpot_reservationsystem.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 唯一单号生成工具类
 * 提取公共的单号生成逻辑，避免代码重复
 */
public class UniqueNoGenerator {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int MAX_SEQUENCE = 9999;
    
    /**
     * 生成完整的单号
     * 
     * @param prefix 单号前缀（如：ORD, RES, PAY）
     * @param lastNo 当前最大的单号（可为null）
     * @return 生成的唯一单号
     */
    public static String generateNo(String prefix, String lastNo) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        String fullPrefix = prefix + date;
        
        int sequence = calculateNextSequence(lastNo, fullPrefix);
        
        return fullPrefix + String.format("%04d", sequence);
    }
    
    /**
     * 计算下一个序号
     * 
     * @param lastNo 当前最大的单号
     * @param prefix 完整前缀（包含日期，如：ORD20250120）
     * @return 下一个序号（1-9999）
     */
    public static int calculateNextSequence(String lastNo, String prefix) {
        if (lastNo == null || !lastNo.startsWith(prefix) || lastNo.length() != prefix.length() + 4) {
            return 1;
        }
        
        try {
            String seqStr = lastNo.substring(prefix.length());
            int sequence = Integer.parseInt(seqStr) + 1;
            // 确保不超过9999
            if (sequence > MAX_SEQUENCE) {
                sequence = 1;
            }
            return sequence;
        } catch (NumberFormatException e) {
            return 1;
        }
    }
    
    /**
     * 获取当天日期字符串（yyyyMMdd格式）
     */
    public static String getTodayDateString() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
    
    /**
     * 构建完整的前缀（前缀 + 日期）
     */
    public static String buildFullPrefix(String prefix) {
        return prefix + getTodayDateString();
    }
}

