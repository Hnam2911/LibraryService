package com.library.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;

public class MainController {

    // Ánh xạ cái "sân khấu" ở giữa màn hình mà chúng ta vừa đặt id
    @FXML
    private StackPane contentArea;
    @FXML private VBox sidebar;

    // Nút dùng để bật/tắt menu (Bạn gắn fx:id này cho cái nút 3 gạch của bạn)
    @FXML private ToggleButton btnToggleMenu;

    // 5 nút chức năng chính (Đã bỏ chữ 's' và thêm Tổng quan, Cài đặt)
    @FXML private Button btnOverview;
    @FXML private Button btnBook;
    @FXML private Button btnReader;
    @FXML private Button btnBorrow;
    @FXML private Button btnSetting;

    // Trạng thái mặc định ban đầu là mở rộng
    private boolean isExpanded = true;

    @FXML
    public void initialize() {
        // 1. Gắn Tooltip (Chú thích khi trỏ chuột) cho các nút
        btnOverview.setTooltip(new Tooltip("Tổng quan"));
        btnBook.setTooltip(new Tooltip("Quản lý Sách"));
        btnReader.setTooltip(new Tooltip("Quản lý Độc giả"));
        btnBorrow.setTooltip(new Tooltip("Quản lý Phiếu mượn"));
        btnSetting.setTooltip(new Tooltip("Cài đặt"));
        loadView("DashboardView.fxml");

    }

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
    @FXML
    public void onToggleSidebar() {
        // 1. Đảo ngược trạng thái
        isExpanded = !isExpanded;

        // 2. Thiết lập kích thước (Thay đổi con số này cho khớp với Design của bạn)
        double expandedWidth = 175.0; // Chiều rộng khi mở (có chữ)
        double collapsedWidth = 50.0; // Chiều rộng khi thu gọn (chỉ có icon)

        sidebar.setPrefWidth(isExpanded ? expandedWidth : collapsedWidth);

        // 3. Chế độ hiển thị: LEFT (Có chữ) hoặc GRAPHIC_ONLY (Giấu chữ)
        ContentDisplay displayMode = isExpanded ?
                ContentDisplay.LEFT :
                ContentDisplay.GRAPHIC_ONLY;

        // 4. Gom tất cả các nút vào 1 mảng và dùng vòng lặp để set (Code sạch & ngắn)
        Button[] menuButtons = {
                btnOverview, btnBook, btnReader, btnBorrow, btnSetting
        };

        for (Button btn : menuButtons) {
            btn.setContentDisplay(displayMode);
        }
    }

    // Các hàm xử lý khi bấm nút (Tên hàm phải khớp chính xác với thẻ On Action)
    @FXML
    public void showDashboard(ActionEvent event) {
        loadView("DashboardView.fxml");
    }

    @FXML
    public void showBookView(ActionEvent event) {
        loadView("BookView.fxml");
    }

    @FXML
    public void showReaderView(ActionEvent event) {
        loadView("ReaderView.fxml");
    }

    @FXML
    public void showBorrowView(ActionEvent event) {
        loadView("BorrowView.fxml");
    }

    @FXML
    public void showSettings(ActionEvent event) {
        loadView("SettingView.fxml");
    }
}
