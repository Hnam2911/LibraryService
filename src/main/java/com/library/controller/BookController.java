package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;
import static com.library.util.UIUtils.*;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Optional;



public class BookController {
    @FXML private TextField txtSearchBook;

    @FXML private Button btnDelete;
    @FXML private Button btnUpdate;
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
        // Cấu hình các cột (Giữ nguyên code cũ của bạn)
        setupColumns();

        //  Tạo bộ lọc (FilteredList) bao quanh danh sách gốc
        FilteredList<Book> filteredData = new FilteredList<>(bookList, p -> true);

        //  Lắng nghe thay đổi trên ô tìm kiếm
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
        // BINDING RÀNG BUỘC TRẠNG THÁI NÚT BẤM (Tự động disable nếu chưa chọn dòng)
        btnUpdate.disableProperty().bind(tableBook.getSelectionModel().selectedItemProperty().isNull());
        btnDelete.disableProperty().bind(tableBook.getSelectionModel().selectedItemProperty().isNull());

        //  Thiết lập Menu chuột phải (Context Menu)
        setupContextMenu();

        //  Tải dữ liệu lần đầu
        refreshTable();
    }
    private void setupColumns() {
        // Chuyển toàn bộ code colTitle.setCellValueFactory... vào đây cho gọn
        // CẤU HÌNH CỘT STT: Tự động tính toán vị trí index + 1
        colSTT.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(tableBook.getItems().indexOf(column.getValue()) + 1));
        colSTT.setSortable(false);
        // ÁNH XẠ CÁC CỘT DỮ LIỆU: Tên chuỗi ("title") phải khớp với tên biến trong class Book
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Lưu ý: Class Book của bạn cần có thuộc tính borrowedQuantity và hàm getBorrowedQuantity()
        colBorrowed.setCellValueFactory(new PropertyValueFactory<>("borrowedQuantity"));

        tableBook.setFixedCellSize(35);

    }
    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem itemAdd = new MenuItem("Thêm mới");
        itemAdd.setOnAction(e -> onAddBook(null)); // Tái sử dụng hàm onAddBook đã có

        MenuItem itemUpdate = new MenuItem("Sửa");
        itemUpdate.setOnAction(e -> onUpdateBook(null));
        // Ràng buộc Disable cho menu Sửa giống hệt nút bấm
        itemUpdate.disableProperty().bind(tableBook.getSelectionModel().selectedItemProperty().isNull());

        MenuItem itemDelete = new MenuItem("Xóa");
        itemDelete.setOnAction(e -> onDeleteBook(null));
        // Ràng buộc Disable cho menu Xóa giống hệt nút bấm
        itemDelete.disableProperty().bind(tableBook.getSelectionModel().selectedItemProperty().isNull());

        MenuItem itemExport = new MenuItem("Xuất Excel");
        itemExport.setOnAction(e -> onExportExcel(null));

        MenuItem itemRefresh = new MenuItem("Làm mới");
        itemRefresh.setOnAction(e -> refreshTable());

        // Thêm vào menu (có sử dụng SeparatorMenuItem để tạo vạch kẻ ngang phân cách cho đẹp)
        contextMenu.getItems().addAll(itemAdd, itemUpdate, itemDelete, new javafx.scene.control.SeparatorMenuItem(), itemExport, itemRefresh);

        // Gắn ContextMenu vào bảng
        tableBook.setContextMenu(contextMenu);
    }

    // Hàm tiện ích: Tải lại toàn bộ dữ liệu bảng
    private void refreshTable() {
        bookList.setAll(bookService.getAllBook());
        tableBook.getSelectionModel().clearSelection(); // Bỏ chọn dòng cũ sau khi làm mới
    }


    // --- 4. CÁC HÀM XỬ LÝ SỰ KIỆN NÚT BẤM (Đã gắn On Action) ---
    @FXML
    public void onAddBook(ActionEvent event) {
        showBookFormDialog(null);
    }

    @FXML
    public void onUpdateBook(ActionEvent event) {
        Book selectedBook=tableBook.getSelectionModel().getSelectedItem();
        if(selectedBook!=null){
            showBookFormDialog(selectedBook);
        }
    }

    @FXML
    public void onDeleteBook(ActionEvent event){
        Book selectedBook = tableBook.getSelectionModel().getSelectedItem();

        // Code bọc lót an toàn dù nút bấm đã bị disable
        if (selectedBook == null) {
            return;
        }

        // 1. KIỂM TRA NGHIỆP VỤ: Đang có người mượn thì KHÔNG ĐƯỢC XÓA
        if (selectedBook.getBorrowedQuantity() > 0) {
            showAlert("Lỗi nghiệp vụ", "Không thể xóa sách này vì đang có " + selectedBook.getBorrowedQuantity() + " quyển được mượn!");
            return;
        }

        // 2. HIỂN THỊ HỘP THOẠI XÁC NHẬN (Confirmation Alert)
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa cuốn sách này?");
        confirmAlert.setContentText("Tựa sách: " + selectedBook.getTitle());

        // 3. XỬ LÝ KẾT QUẢ TỪ NGƯỜI DÙNG
        if (confirmAlert.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK) {
            // Gọi Service xóa (Giả định bookService của bạn trả về boolean)
            boolean isDeleted = bookService.deleteBook(selectedBook.getId());

            if (isDeleted) {
                showAlert("Thành công", "Đã xóa sách khỏi thư viện.");
                refreshTable(); // Làm mới bảng ngay lập tức
            } else {
                showAlert("Lỗi hệ thống", "Đã xảy ra lỗi khi xóa sách. Vui lòng thử lại!");
            }
        }
    }

    @FXML
    public void onExportExcel(ActionEvent event) {
        // 1. Mở hộp thoại chọn nơi lưu file (FileChooser)
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file Excel - Danh sách sách");
        // Chỉ cho phép lưu định dạng xlsx
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("DanhSachSach.xlsx"); // Tên file mặc định

        // Lấy Cửa sổ (Stage) hiện tại để hiển thị hộp thoại
        File file = fileChooser.showSaveDialog(tableBook.getScene().getWindow());

        // Nếu người dùng bấm "Cancel" không lưu nữa thì file sẽ bị null
        if (file != null) {
            // 2. Tạo Workbook (File Excel) và Sheet (Trang tính)
            // Dùng try-with-resources để tự động đóng bộ nhớ của POI
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Danh sách sách");

                // 3. Tạo dòng tiêu đề (Header Row - Dòng số 0)
                Row headerRow = sheet.createRow(0);
                String[] columns = {"STT", "Tựa sách", "Tác giả", "Tổng kho", "Đang mượn"};

                // Định dạng in đậm cho tiêu đề (Tùy chọn cho chuyên nghiệp)
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);

                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                }

                // 4. Đổ dữ liệu từ Bảng JavaFX vào Excel
                int rowIndex = 1;
                // Dùng tableBook.getItems() để CHỈ xuất những dữ liệu đang hiển thị (hỗ trợ tính năng tìm kiếm)
                for (Book book : tableBook.getItems()) {
                    Row row = sheet.createRow(rowIndex++);

                    row.createCell(0).setCellValue(rowIndex - 1); // STT
                    row.createCell(1).setCellValue(book.getTitle());
                    row.createCell(2).setCellValue(book.getAuthor());
                    row.createCell(3).setCellValue(book.getQuantity());
                    row.createCell(4).setCellValue(book.getBorrowedQuantity());
                }

                // Tự động căn chỉnh độ rộng các cột cho vừa với chữ
                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // 5. Ghi dữ liệu ra file vật lý trên ổ cứng
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                showAlert("Thành công", "Đã xuất dữ liệu ra file Excel thành công!");

            } catch (Exception e) {
                showAlert("Lỗi xuất file", "Đã xảy ra lỗi khi tạo file Excel: " + e.getMessage());
                e.printStackTrace(); // In lỗi ra console để lập trình viên dễ fix
            }
        };
    }
    private void showBookFormDialog(Book bookToEdit) {
        boolean isUpdate = (bookToEdit != null);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isUpdate ? "Cập nhật thông tin sách" : "Thêm sách mới");

        ButtonType btnSaveType = new ButtonType("Lưu", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        // FIX LỖI GIAO DIỆN: Cài đặt chiều rộng dài hơn và cho phép chiều cao tự động co giãn
        dialog.getDialogPane().setPrefWidth(450);

        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(btnSaveType);
        saveButton.setDisable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 40, 10, 10)); // Giảm padding bên phải để form cân đối hơn

        // 1. TẠO CÁC Ô NHẬP LIỆU
        TextField txtTitle = new TextField();
        txtTitle.setPromptText("Nhập tựa sách");
        txtTitle.setPrefWidth(300);
        javafx.scene.layout.VBox boxTitle = createValidatedTextField(txtTitle, "Tựa sách không được để trống!");

        TextField txtAuthor = new TextField();
        txtAuthor.setPromptText("Nhập tên tác giả");
        txtTitle.setPrefWidth(300);
        javafx.scene.layout.VBox boxAuthor = createValidatedTextField(txtAuthor, "Tác giả không được để trống!");

        TextField txtQuantity = new TextField();
        txtQuantity.setPromptText("Mặc định là 0 nếu để trống");// Sửa lại câu nhắc
        txtTitle.setPrefWidth(300);
        Label lblQuantityError = new Label();
        lblQuantityError.setStyle("-fx-text-fill: red; -fx-font-size: 11px; -fx-font-style: italic;");
        lblQuantityError.setVisible(false); lblQuantityError.setManaged(false);

        // 2. ĐƯA VÀO LAYOUT
        grid.add(new Label("Tựa sách (*):"), 0, 0);
        grid.add(boxTitle, 1, 0);
        grid.add(new Label("Tác giả (*):"), 0, 1);
        grid.add(boxAuthor, 1, 1);
        grid.add(new Label("Số lượng tổng:"), 0, 2); // Bỏ dấu (*) vì không bắt buộc

        javafx.scene.layout.VBox boxQuantity = new javafx.scene.layout.VBox(3, txtQuantity, lblQuantityError);
        grid.add(boxQuantity, 1, 2);

        // 3. ĐỔ DỮ LIỆU CŨ NẾU LÀ UPDATE
        if (isUpdate) {
            txtTitle.setText(bookToEdit.getTitle());
            txtAuthor.setText(bookToEdit.getAuthor());
            txtQuantity.setText(String.valueOf(bookToEdit.getQuantity()));
        }

        dialog.getDialogPane().setContent(grid);

        // 4. TRẠM KIỂM DUYỆT (Cập nhật logic mặc định = 0)
        Runnable validateInput = () -> {
            boolean isTitleValid = !txtTitle.getText().trim().isEmpty();
            boolean isAuthorValid = !txtAuthor.getText().trim().isEmpty();
            boolean isQuantityValid = false;

            String qText = txtQuantity.getText().trim();
            // Nếu trống -> coi như = 0. Nếu có chữ số -> ép kiểu. Nếu nhập bậy -> -1 để bắt lỗi.
            int q = qText.isEmpty() ? 0 : (qText.matches("\\d+") ? Integer.parseInt(qText) : -1);

            if (qText.isEmpty() || qText.matches("\\d+")) {
                if (isUpdate) {
                    if (q >= bookToEdit.getBorrowedQuantity()) {
                        isQuantityValid = true;
                        setFieldError(txtQuantity, lblQuantityError, "", false);
                    } else {
                        setFieldError(txtQuantity, lblQuantityError, "Phải >= số sách đang mượn (" + bookToEdit.getBorrowedQuantity() + ")", true);
                    }
                } else {
                    isQuantityValid = true;
                    setFieldError(txtQuantity, lblQuantityError, "", false);
                }
            } else {
                setFieldError(txtQuantity, lblQuantityError, "Số lượng phải là chữ số!", true);
            }

            saveButton.setDisable(!(isTitleValid && isAuthorValid && isQuantityValid));

        };

        // 5. GẮN CẢM BIẾN
        txtTitle.textProperty().addListener((obs, oldV, newV) -> validateInput.run());
        txtAuthor.textProperty().addListener((obs, oldV, newV) -> validateInput.run());
        txtQuantity.textProperty().addListener((obs, oldV, newV) -> validateInput.run());

        validateInput.run();

        // 6. XỬ LÝ KHI BẤM NÚT LƯU
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == btnSaveType) {
            String title = txtTitle.getText().trim();
            String author = txtAuthor.getText().trim();

            // Xử lý giá trị khi bấm Lưu: Nếu trống thì lấy số 0
            String qStr = txtQuantity.getText().trim();
            int quantity = qStr.isEmpty() ? 0 : Integer.parseInt(qStr);

            try {
                if (isUpdate) {
                    bookService.updateBook(bookToEdit.getId(), title, author, quantity);
                    showAlert("Thành công", "Đã cập nhật thông tin sách!");
                } else {
                    bookService.addBook(title, author, quantity);
                    showAlert("Thành công", "Đã thêm sách mới vào thư viện!");
                }
                refreshTable();
            } catch (Exception ex) {
                showAlert("Lỗi hệ thống", ex.getMessage());
            }
        }
    }
}
