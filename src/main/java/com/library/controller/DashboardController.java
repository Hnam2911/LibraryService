package com.library.controller;

import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Reader;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.ReaderService;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    // 1. CÁC THẺ KPI (Chỉ số tổng quan)
    @FXML private Label lblTotalBooks;
    @FXML private Label lblTotalReaders;
    @FXML private Label lblBorrowed;
    @FXML private Label lblOverdue;

    // 2. BẢNG CẢNH BÁO SÁCH SẮP HẾT
    @FXML private TableView<Book> tableLowStock;
    @FXML private TableColumn<Book, String> colLowStockTitle;
    @FXML private TableColumn<Book, Integer> colLowStockQuantity;

    // 3. BẢNG CẢNH BÁO ĐỘC GIẢ QUÁ HẠN
    @FXML private TableView<BorrowRecord> tableOverdue;
    @FXML private TableColumn<BorrowRecord, String> colOverdueReader;
    @FXML private TableColumn<BorrowRecord, String> colOverduePhone;
    @FXML private TableColumn<BorrowRecord, String> colOverdueBook;
    @FXML private TableColumn<BorrowRecord, LocalDate> colOverdueReturnDate;

    // 4. KHAI BÁO SERVICES
    private BookService bookService = new BookService();
    private ReaderService readerService = new ReaderService();
    private BorrowService borrowService = new BorrowService();

    @FXML
    public void initialize() {
        // Luôn kiểm tra quá hạn mới nhất trước khi lấy dữ liệu
        borrowService.checkOverdue();
        setupTables();
        loadDashboardData();
    }

    private void setupTables() {
        // Cột cho bảng Sách sắp hết
        colLowStockTitle.setPrefWidth(250);
        colLowStockQuantity.setPrefWidth(100);
        colLowStockTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colLowStockQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Cột cho bảng Độc giả quá hạn (Phải chui vào Object lồng nhau giống BorrowView)
        colOverdueReader.setPrefWidth(160);       // Tên độc giả
        colOverduePhone.setPrefWidth(110);        // SĐT
        colOverdueBook.setPrefWidth(220);         // Tựa sách (Rất dài)
        colOverdueReturnDate.setPrefWidth(100);   // Hạn trả

        colOverdueReader.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReader().getName()));
        colOverduePhone.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReader().getPhone()));
        colOverdueBook.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBook().getTitle()));
        colOverdueReturnDate.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getReturnDate()));
    }

    private void loadDashboardData() {
        // Lấy toàn bộ dữ liệu từ Database
        List<Book> allBooks = bookService.getAllBook();
        List<Reader> allReaders = readerService.getAllReader();
        List<BorrowRecord> allRecords = borrowService.getAllRecord();

        // 1. TÍNH TOÁN VÀ ĐỔ DỮ LIỆU LÊN THẺ KPI
        lblTotalBooks.setText(String.valueOf(allBooks.size()));
        lblTotalReaders.setText(String.valueOf(allReaders.size()));

        long borrowedCount = allRecords.stream().filter(r -> "borrowed".equalsIgnoreCase(r.getStatus())).count();
        long overdueCount = allRecords.stream().filter(r -> "overdue".equalsIgnoreCase(r.getStatus())).count();

        lblBorrowed.setText(String.valueOf(borrowedCount));
        lblOverdue.setText(String.valueOf(overdueCount));

        // 2. LỌC DỮ LIỆU CHO BẢNG "SÁCH SẮP HẾT KHO" (Số lượng <= 3)
        List<Book> lowStockBooks = allBooks.stream()
                .filter(b -> b.getQuantity() <= 3)
                .collect(Collectors.toList());
        tableLowStock.setItems(FXCollections.observableArrayList(lowStockBooks));

        // 3. LỌC DỮ LIỆU CHO BẢNG "ĐỘC GIẢ QUÁ HẠN" (Trạng thái = overdue)
        List<BorrowRecord> overdueRecords = allRecords.stream()
                .filter(r -> "overdue".equalsIgnoreCase(r.getStatus()))
                .collect(Collectors.toList());
        tableOverdue.setItems(FXCollections.observableArrayList(overdueRecords));
    }
}
