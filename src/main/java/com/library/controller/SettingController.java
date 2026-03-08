package com.library.controller;

import io.github.cdimascio.dotenv.Dotenv;
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
    public void onBackupDatabase(javafx.event.ActionEvent event) {
        // Mở cửa sổ chọn nơi lưu file
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Chọn nơi lưu file Sao lưu CSDL");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("SQL File", "*.sql"));

        // Tự động gợi ý tên file kèm ngày tháng hiện tại
        fileChooser.setInitialFileName("Backup_ThuVien_" + java.time.LocalDate.now() + ".sql");

        // Lấy Window hiện tại từ một thành phần UI bất kỳ (ví dụ lblDbStatus)
        java.io.File saveFile = fileChooser.showSaveDialog(lblDbStatus.getScene().getWindow());

        if (saveFile != null) {
            // Hiển thị thông báo đang xử lý (vì backup có thể mất vài giây)
            UIUtils.showAlert("Đang xử lý", "Hệ thống đang tiến hành sao lưu. Vui lòng đợi trong giây lát...");

            // Gọi hàm thực thi tiến trình sao lưu ngầm
            boolean success = performBackup(saveFile.getAbsolutePath());

            if (success) {
                UIUtils.showAlert("Thành công", "Đã sao lưu dữ liệu an toàn ra file:\n" + saveFile.getAbsolutePath());
            } else {
                UIUtils.showAlert("Lỗi hệ thống", "Quá trình sao lưu thất bại.\nHãy chắc chắn máy tính đã cài đặt PostgreSQL và công cụ 'pg_dump' đã được thêm vào biến môi trường (Environment Variables).");
            }
        }
    }
    // 2. LÕI XỬ LÝ GỌI TIẾN TRÌNH `pg_dump` CỦA POSTGRESQL (ĐÃ FIX BẢO MẬT)
    private boolean performBackup(String filePath) {
        try {
            // ĐỌC BẢO MẬT TỪ FILE .ENV (Sử dụng dotenv-java)
            Dotenv dotenv = Dotenv.configure().directory("./").load();

            // Lấy thông tin từ file môi trường thay vì hardcode
            String dbUser = dotenv.get("DB_USER");
            String dbPassword = dotenv.get("DB_PASSWORD");
            String dbName = dotenv.get("DB_NAME");

            // Kiểm tra an toàn: Nếu file .env bị thiếu cấu hình thì dừng ngay
            if (dbUser == null || dbPassword == null || dbName == null) {
                System.err.println("Lỗi bảo mật: Không tìm thấy cấu hình DB trong file .env!");
                return false;
            }

// 1. Cấu hình lệnh chạy pg_dump (BỎ CỜ "-f" VÀ FILE PATH ĐI)
            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump",
                    "-U", dbUser,
                    "-d", dbName
                    // Tuyệt đối không truyền filePath vào đây nữa
            );

            // 2. Truyền mật khẩu
            pb.environment().put("PGPASSWORD", dbPassword);

            // 3. ĐÂY LÀ PHÉP MÀU: Để Java tự đứng ra tạo file và hứng dữ liệu
            // Java không bị lỗi font Tiếng Việt với đường dẫn của Windows
            pb.redirectOutput(new java.io.File(filePath));

            // 4. Bắt lỗi (Giữ nguyên như bạn vừa thêm để phòng hờ)
            pb.redirectError(ProcessBuilder.Redirect.PIPE);

            Process process = pb.start();

            // Đọc lỗi nếu có
            java.io.BufferedReader errorReader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getErrorStream())
            );
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("PG_DUMP BÁO LỖI: " + errorLine);
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Tiến trình kết thúc với mã lỗi: " + exitCode);
            }

            return exitCode == 0;
        } catch(Exception e){
                System.err.println("Lỗi khi chạy pg_dump: " + e.getMessage());
                return false;
        }
    }
}
