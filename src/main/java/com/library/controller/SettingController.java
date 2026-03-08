package com.library.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Label;
import com.library.util.UIUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SettingController {

    // TAB 1: NGHIỆP VỤ
    @FXML private Spinner<Integer> spinDefaultDays;
    @FXML private Spinner<Integer> spinMaxBooks;

    // TAB 2: HỆ THỐNG
    @FXML private Label lblDbStatus;

    // Tên file cấu hình sẽ lưu trong thư mục dự án
    private final String CONFIG_FILE = "config.properties";
    private Properties properties = new Properties();

    @FXML
    public void initialize() {
        setupSpinners();
        loadSettings();
        checkDatabaseStatus();
    }

    private void setupSpinners() {
        // Cài đặt giới hạn cho các ô nhập số (Min, Max, Default)
        spinDefaultDays.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 14));
        spinMaxBooks.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));
    }

    // ĐỌC CẤU HÌNH TỪ FILE
    private void loadSettings() {
        try {
            File file = new File(CONFIG_FILE);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                properties.load(fis);
                fis.close();

                // Nạp giá trị lên giao diện
                int defaultDays = Integer.parseInt(properties.getProperty("DEFAULT_BORROW_DAYS", "14"));
                int maxBooks = Integer.parseInt(properties.getProperty("MAX_BORROW_BOOKS", "3"));

                spinDefaultDays.getValueFactory().setValue(defaultDays);
                spinMaxBooks.getValueFactory().setValue(maxBooks);
            }
        } catch (Exception e) {
            System.err.println("Không thể đọc file cấu hình: " + e.getMessage());
        }
    }

    // LƯU CẤU HÌNH XUỐNG FILE (Gắn vào nút Lưu ở Tab 1)
    @FXML
    public void onSaveSettings(ActionEvent event) {
        try {
            properties.setProperty("DEFAULT_BORROW_DAYS", String.valueOf(spinDefaultDays.getValue()));
            properties.setProperty("MAX_BORROW_BOOKS", String.valueOf(spinMaxBooks.getValue()));

            FileOutputStream fos = new FileOutputStream(CONFIG_FILE);
            properties.store(fos, "Library Management System Configuration");
            fos.close();

            UIUtils.showAlert("Thành công", "Đã lưu cấu hình hệ thống! Các quy tắc mới sẽ được áp dụng cho các lần mượn tiếp theo.");
        } catch (IOException e) {
            UIUtils.showAlert("Lỗi", "Không thể lưu cấu hình: " + e.getMessage());
        }
    }

    // KIỂM TRA TRẠNG THÁI DB (Chạy lúc khởi tạo)
    private void checkDatabaseStatus() {
        // Giả sử DatabaseHelper của bạn có hàm getConnection()
        try {
            // Thay bằng logic check Connection thật của bạn
            boolean isConnected = true; // DatabaseHelper.getInstance().getConnection() != null;
            if (isConnected) {
                lblDbStatus.setText("Kết nối ổn định (PostgreSQL)");
                lblDbStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                lblDbStatus.setText("Mất kết nối Cơ sở dữ liệu!");
                lblDbStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            lblDbStatus.setText("Lỗi kiểm tra trạng thái");
        }
    }

    // NÚT BACKUP DATABASE (Tab 2)
    @FXML
    public void onBackupDatabase(ActionEvent event) {
        // Tính năng này chúng ta sẽ viết logic riêng ở bước sau
        UIUtils.showAlert("Thông báo", "Tính năng sao lưu CSDL đang được chuẩn bị khởi chạy...");
    }
}