package com.library.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Chỉ định đường dẫn tới bản vẽ FXML
        // LƯU Ý CHÍNH MẠNG: Đường dẫn này phụ thuộc vào nơi bạn lưu file FXML!
        URL fxmlLocation = getClass().getResource("/view/MainLayout.fxml");

        if (fxmlLocation == null) {
            System.out.println("❌ LỖI NGHIÊM TRỌNG: Không tìm thấy file FXML. Hãy kiểm tra lại đường dẫn!");
            return;
        }

        // 2. Gọi thợ xây FXMLLoader ra đọc bản vẽ
        FXMLLoader loader = new FXMLLoader(fxmlLocation);

        // 3. Xây nhà (Tạo ra đối tượng Parent chứa toàn bộ giao diện)
        Parent root = loader.load();

        // 4. Đặt ngôi nhà lên một cái bãi đất (Scene)
        // Kích thước 1000x700 là kích thước mặc định lúc khởi động
        Scene scene = new Scene(root, 1000, 700);

        // 5. Cấu hình Cửa sổ ứng dụng (Stage)
        primaryStage.setTitle("Hệ thống Quản lý Thư viện v1.0"); // Tiêu đề cửa sổ
        primaryStage.setScene(scene); // Lắp Scene vào Stage
        primaryStage.centerOnScreen(); // Hiển thị ở chính giữa màn hình
        primaryStage.show(); // Lệnh cuối cùng: Bật cửa sổ lên!
    }

    public static void main(String[] args) {
        // Kích hoạt chu trình sống của JavaFX
        launch(args);
    }
}