package com.library.util;

import com.library.model.Book;
import javafx.scene.control.*;
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
    // Hàm thiết lập tự động gợi ý và tự động điền (Autocomplete)
    public static void setupBookAutoComplete(TextField txtTitle,
                                             TextField txtAuthor,
                                             java.util.List<Book> bookList) {

        ContextMenu popupMenu = new ContextMenu();

        // 1. Lắng nghe khi gõ vào ô Tựa sách
        txtTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            // Chỉ hiển thị gợi ý khi ô này đang được trỏ chuột vào (đang gõ) và có chữ
            if (txtTitle.isFocused() && !newValue.trim().isEmpty()) {
                populateMenu(txtTitle, txtAuthor, popupMenu, bookList, newValue, true);
            } else {
                popupMenu.hide();
            }
        });

        // 2. Lắng nghe khi gõ vào ô Tác giả (Ngược lại)
        txtAuthor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txtAuthor.isFocused() && !newValue.trim().isEmpty()) {
                populateMenu(txtAuthor, txtTitle, popupMenu, bookList, newValue, false);
            } else {
                popupMenu.hide();
            }
        });
    }

    // Hàm lõi xử lý việc tìm kiếm và tạo danh sách thả xuống
    private static void populateMenu(TextField activeField,
                                     TextField passiveField,
                                     ContextMenu menu,
                                     java.util.List<Book> bookList,
                                     String searchStr, boolean isSearchingTitle) {
        menu.getItems().clear();
        String lowerSearch = searchStr.toLowerCase();

        for (Book book : bookList) {
            // Tìm kiếm tương đối theo Tên sách hoặc Tác giả
            boolean match = isSearchingTitle ?
                    book.getTitle().toLowerCase().contains(lowerSearch) :
                    book.getAuthor().toLowerCase().contains(lowerSearch);

            if (match) {
                // Tạo một dòng lựa chọn trượt xuống
                MenuItem item = new MenuItem(book.getTitle() + "  |  Tác giả: " + book.getAuthor());

                // Sự kiện KHI BẤM CHỌN VÀO GỢI Ý
                item.setOnAction(e -> {
                    // Tự động điền dữ liệu chuẩn từ Database vào CẢ 2 Ô cùng lúc
                    if (isSearchingTitle) {
                        activeField.setText(book.getTitle());
                        passiveField.setText(book.getAuthor());
                    } else {
                        activeField.setText(book.getAuthor());
                        passiveField.setText(book.getTitle());
                    }

                    // Đẩy con trỏ chuột về cuối dòng chữ
                    activeField.positionCaret(activeField.getText().length());
                });
                menu.getItems().add(item);
            }
        }

        // Hiện menu trượt xuống ở ngay mép dưới của ô đang nhập
        if (!menu.getItems().isEmpty()) {
            menu.show(activeField, javafx.geometry.Side.BOTTOM, 0, 0);
        } else {
            menu.hide(); // Ẩn đi nếu không tìm thấy cuốn sách nào khớp
        }
    }
}
