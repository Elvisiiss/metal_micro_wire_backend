package com.mmw.metal_micro_wire_backend.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * 编码转换工具类
 */
@Slf4j
public class EncodingUtil {

    private static final Charset GBK = Charset.forName("GBK");

    /**
     * 将十六进制字符串解码为GBK编码的字符串，然后转换为UTF-8
     *
     * @param hexString 十六进制字符串
     * @return UTF-8编码的字符串
     */
    public static String decodeGbkHexToUtf8(String hexString) {
        if (hexString == null || hexString.trim().isEmpty()) {
            return null;
        }

        try {
            // 移除可能的空格和换行符
            hexString = hexString.replaceAll("\\s+", "");

            // 确保是偶数长度
            if (hexString.length() % 2 != 0) {
                log.warn("十六进制字符串长度不是偶数: {}", hexString);
                return null;
            }

            // 将十六进制字符串转换为字节数组
            byte[] bytes = hexStringToBytes(hexString);

            // 使用GBK编码解码字节数组
            String gbkString = new String(bytes, GBK);

            // 转换为UTF-8（Java字符串默认就是UTF-8）
            return gbkString;

        } catch (Exception e) {
            log.error("解码GBK十六进制字符串失败: {}", hexString, e);
            return null;
        }
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param hexString 十六进制字符串
     * @return 字节数组
     */
    private static byte[] hexStringToBytes(String hexString) {
        int length = hexString.length();
        byte[] bytes = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }

        return bytes;
    }

    /**
     * 解析生产信息字符串
     * 格式: 生产商_负责人_工艺类型_生产机器_联系方式
     * 混合格式：前4部分是十六进制GBK编码，最后一部分是普通字符串
     *
     * @param sourceOrigin 原始生产信息（混合编码）
     * @return 解析后的生产信息数组，包含5个元素
     */
    public static String[] parseSourceOrigin(String sourceOrigin) {
        try {
            if (sourceOrigin == null || sourceOrigin.trim().isEmpty()) {
                log.warn("生产信息为空");
                return new String[5]; // 返回空数组
            }

            // 按下划线分割原始字符串
            String[] rawParts = sourceOrigin.split("_");
            log.debug("原始分割后: {}", java.util.Arrays.toString(rawParts));

            String[] result = new String[5];

            // 所有5部分都是十六进制GBK编码，需要解码
            for (int i = 0; i < Math.min(rawParts.length, 5); i++) {
                String decoded = decodeGbkHexToUtf8(rawParts[i]);
                result[i] = decoded != null ? decoded.trim() : null;
                log.debug("解码部分 {}: {} -> {}", i, rawParts[i], result[i]);
                if (i==4) result[i] = rawParts[i];
            }

            log.debug("最终解析结果: {}", java.util.Arrays.toString(result));
            return result;

        } catch (Exception e) {
            log.error("解析生产信息失败: {}", sourceOrigin, e);
            return new String[5]; // 返回空数组
        }
    }

    /**
     * 将UTF-8字符串编码为GBK十六进制字符串
     *
     * @param utf8String UTF-8字符串
     * @return GBK编码的十六进制字符串
     */
    public static String encodeUtf8ToGbkHex(String utf8String) {
        if (utf8String == null || utf8String.isEmpty()) {
            return null;
        }

        try {
            // 将UTF-8字符串转换为GBK编码的字节数组
            byte[] gbkBytes = utf8String.getBytes(GBK);

            // 将字节数组转换为十六进制字符串
            return bytesToHexString(gbkBytes);

        } catch (Exception e) {
            log.error("编码UTF-8字符串为GBK十六进制失败: {}", utf8String, e);
            return null;
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex.toUpperCase());
        }
        return hexString.toString();
    }

    /**
     * 测试方法 - 验证编码转换功能
     */
    public static void main(String[] args) {
        // 测试示例
        String testHex = "C9FAB2FAC9CC_D5C5C8FD_B9A4D2D5_C9FAB2FABBFAC6F7_333130363133333231364071712E636F6D";
        String decoded = decodeGbkHexToUtf8(testHex);
        System.out.println("解码结果: " + decoded);

        String[] parsed = parseSourceOrigin(testHex);
        System.out.println("解析结果:");
        System.out.println("生产商: " + parsed[0]);
        System.out.println("负责人: " + parsed[1]);
        System.out.println("工艺类型: " + parsed[2]);
        System.out.println("生产机器: " + parsed[3]);
        System.out.println("联系方式: " + parsed[4]);

        // 测试编码功能
        String testMessage = "已收到您的问题";
        String encoded = encodeUtf8ToGbkHex(testMessage);
        System.out.println("编码结果: " + encoded);

        // 验证编码解码是否正确
        String decoded2 = decodeGbkHexToUtf8(encoded);
        System.out.println("验证解码: " + decoded2);
    }
}
