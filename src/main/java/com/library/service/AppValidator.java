package com.library.service;

import java.util.regex.Pattern;

public class AppValidator {
    // 1. Khai báo các Regex làm hằng số (Private để bao đóng)
    private static final String EMAIL_REGEX = "^[A-Z0-9._%+-]+@(?:[A-Z0-9-]+\\.)+[A-Z]{2,6}$";
    private static final String PHONE_REGEX = "^(0|84)(3|5|7|8|9)([0-9]{8})$";

    // 2. Biên dịch sẵn Pattern để tối ưu hiệu suất (chỉ chạy 1 lần)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    // 3. Các phương thức kiểm tra tĩnh (Static methods)
    public static boolean isEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
}
