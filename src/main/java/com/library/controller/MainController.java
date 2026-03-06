package com.library.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.net.URL;

public class MainController {

    // Ánh xạ cái "sân khấu" ở giữa màn hình mà chúng ta vừa đặt id
    @FXML
    private StackPane contentArea;

    private void loadView(String fxmlFileName) {
        try {
            // 1. Tìm bản vẽ của màn hình con
            URL fxmlLocation = getClass().getResource("/view/" + fxmlFileName);
            if (fxmlLocation == null) {
                System.out.println("❌ Không tìm thấy file: " + fxmlFileName);
                return;
            }

            // 2. Nhờ thợ xây đọc bản vẽ
            Parent view = FXMLLoader.load(fxmlLocation);

            // 3. Xóa sạch màn hình cũ trên sân khấu
            contentArea.getChildren().clear();

            // 4. Đẩy màn hình mới lên sân khấu
            contentArea.getChildren().add(view);

        } catch (Exception e) {
            System.err.println("Lỗi khi tải màn hình: " + fxmlFileName);
            e.printStackTrace();
        }
    }

    // Các hàm xử lý khi bấm nút (Tên hàm phải khớp chính xác với thẻ On Action)
    @FXML
    public void showDashboard(ActionEvent event) {
        System.out.println("Đã bấm: Chuyển sang màn hình Tổng quan");
        // Logic nhét DashboardView.fxml vào contentArea sẽ viết ở đây
    }

    @FXML
    public void showBookView(ActionEvent event) {
        loadView("BookView.fxml");
    }

    @FXML
    public void showReaderView(ActionEvent event) {
        System.out.println("Đã bấm: Chuyển sang màn hình Độc giả");
    }

    @FXML
    public void showBorrowView(ActionEvent event) {
        System.out.println("Đã bấm: Chuyển sang màn hình Mượn/Trả");
    }

    @FXML
    public void showSettings(ActionEvent event) {
        System.out.println("Đã bấm: Mở Cài đặt");
    }
}
