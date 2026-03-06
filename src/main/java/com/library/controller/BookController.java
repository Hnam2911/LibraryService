package com.library.controller;

// LƯU Ý: Import đúng đường dẫn package Model và Service của bạn
import com.library.model.Book;
import com.library.service.BookService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class BookController {
    @FXML private TextField txtSearchBook;
    // --- 1. ÁNH XẠ CÁC THÀNH PHẦN GIAO DIỆN (Đã đặt fx:id bên Scene Builder) ---
    @FXML private TableView<Book> tableBook;
    @FXML private TableColumn<Book, Integer> colSTT;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, Integer> colQuantity;
    @FXML private TableColumn<Book, Integer> colBorrowed; // Cột "Đang mượn"

    // --- 2. KHAI BÁO SERVICE & DANH SÁCH DỮ LIỆU ---
    private BookService bookService;
    private ObservableList<Book> bookList;

    public BookController() {
        bookService = new BookService();
        bookList = FXCollections.observableArrayList();
    }

    /**
     * Hàm này tự động chạy NGAY SAU KHI giao diện FXML được nạp xong.
     * Thường dùng để cấu hình bảng và tải dữ liệu ban đầu.
     */
    @FXML
    public void initialize() {
// 1. Cấu hình các cột (Giữ nguyên code cũ của bạn)
        setupColumns();

        // 2. Tải dữ liệu từ Service
        bookList.setAll(bookService.getAllBook());

        // 3. Tạo bộ lọc (FilteredList) bao quanh danh sách gốc
        FilteredList<Book> filteredData = new FilteredList<>(bookList, p -> true);

        // 4. Lắng nghe thay đổi trên ô tìm kiếm
        txtSearchBook.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(book -> {
                // Nếu ô tìm kiếm trống, hiển thị tất cả
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Lọc theo Tên sách hoặc Tác giả
                if (book.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (book.getAuthor().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(book.getQuantity()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(book.getBorrowedQuantity()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Không khớp
            });
        });

        // 5. Kết nối bộ lọc với khả năng sắp xếp của bảng (Sort)
        SortedList<Book> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableBook.comparatorProperty());

        // 6. Đổ dữ liệu đã lọc và sắp xếp vào bảng
        tableBook.setItems(sortedData);

        tableBook.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                System.out.println("Đang chọn sách: " + newSelection.getTitle());
            }
        });
    }
    private void setupColumns() {
        // Chuyển toàn bộ code colTitle.setCellValueFactory... vào đây cho gọn
        // CẤU HÌNH CỘT STT: Tự động tính toán vị trí index + 1
        colSTT.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(tableBook.getItems().indexOf(column.getValue()) + 1));

        // ÁNH XẠ CÁC CỘT DỮ LIỆU: Tên chuỗi ("title") phải khớp với tên biến trong class Book
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Lưu ý: Class Book của bạn cần có thuộc tính borrowedQuantity và hàm getBorrowedQuantity()
        colBorrowed.setCellValueFactory(new PropertyValueFactory<>("borrowedQuantity"));

    }


    // --- 4. CÁC HÀM XỬ LÝ SỰ KIỆN NÚT BẤM (Đã gắn On Action) ---
    @FXML
    public void onAddBook(ActionEvent event) {
        System.out.println("Sẽ mở form Thêm sách...");
    }

    @FXML
    public void onUpdateBook(ActionEvent event) {
        System.out.println("Sẽ mở form Sửa sách...");
    }

    @FXML
    public void onDeleteBook(ActionEvent event) {
        System.out.println("Sẽ chạy lệnh Xóa sách...");
    }

    @FXML
    public void onExportExcel(ActionEvent event) {
        System.out.println("Sẽ chạy hàm Xuất file Excel...");
    }
}
