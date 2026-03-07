package com.library.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class UIUtils {

    // 1. Hàm hiển thị thông báo Popup dùng chung
    public static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // 2. Hàm bật/tắt viền đỏ và dòng chữ báo lỗi
    public static void setFieldError(TextField field, Label lbl, String message, boolean isError) {
        if (isError) {
            field.setStyle("-fx-border-color: red; -fx-border-radius: 3; -fx-border-width: 1;");
            lbl.setText(message);
        } else {
            field.setStyle("");
            lbl.setText(" "); // Khoảng trắng giữ chỗ chống nhảy form
        }
    }

    // 3. Hàm tạo TextField có sẵn validate rỗng
    public static VBox createValidatedTextField(TextField textField, String errorMessage) {
        Label lblError = new Label(" ");
        lblError.setStyle("-fx-text-fill: red; -fx-font-size: 10px; -fx-font-style: italic;");

        textField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                if (textField.getText().trim().isEmpty()) {
                    textField.setStyle("-fx-border-color: red; -fx-border-radius: 3; -fx-border-width: 1;");
                    lblError.setText(errorMessage);
                }
            }
        });

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                textField.setStyle("");
                lblError.setText(" ");
            }
        });

        VBox box = new VBox(3);
        box.getChildren().addAll(textField, lblError);
        return box;
    }
}
