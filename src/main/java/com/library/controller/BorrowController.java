package com.library.controller;

import com.library.model.BorrowRecord;
import com.library.model.Book;
import com.library.model.Reader;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.ReaderService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.library.util.UIUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.LocalDate;

public class BorrowController {

    // 1. KHAI BÁO CÁC THÀNH PHẦN GIAO DIỆN (@FXML)
    @FXML
    private TableView<BorrowRecord> tableBorrow;
    @FXML
    private TableColumn<BorrowRecord, Integer> colSTT;
    @FXML
    private TableColumn<BorrowRecord, String> colName;   // Họ và tên
    @FXML
    private TableColumn<BorrowRecord, String> colPhone;  // SĐT
    @FXML
    private TableColumn<BorrowRecord, String> colTitle;    // Tựa sách
    @FXML
    private TableColumn<BorrowRecord, String> colAuthor;   // Tác giả
    @FXML
    private TableColumn<BorrowRecord, LocalDate> colBorrowDate;// Ngày mượn
    @FXML
    private TableColumn<BorrowRecord, LocalDate> colReturnDate;// Hạn trả
    @FXML
    private TableColumn<BorrowRecord, String> colStatus;       // Trạng thái

    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnReturn;
    @FXML
    private Button btnExcel;

    // 2. BIẾN TOÀN CỤC
    private ObservableList<BorrowRecord> recordList = FXCollections.observableArrayList();
    private BorrowService borrowService = new BorrowService();
    private BookService bookService = new BookService();
    private ReaderService readerService = new ReaderService();

    // 3. HÀM KHỞI TẠO (CHẠY ĐẦU TIÊN)
    @FXML
    public void initialize() {
        // [PRODUCTION STANDARD] Bắt buộc kiểm tra quá hạn trước khi đổ dữ liệu ra UI
        borrowService.checkOverdue();

        setupColumns();
        setupSearchAndFilter();
        setupButtonBindings();
        setupContextMenu();

        // Tránh lỗi bốc hơi dòng của JavaFX
        tableBorrow.setFixedCellSize(40);

        refreshTable();
    }

    // 4. THIẾT LẬP CÁC CỘT DỮ LIỆU (Xử lý Object lồng nhau)
    private void setupColumns() {
        colSTT.setPrefWidth(40);           // STT chỉ cần rất nhỏ
        colName.setPrefWidth(160);   // Tên người thường khá dài
        colPhone.setPrefWidth(110);  // SĐT cố định 10 số
        colTitle.setPrefWidth(220);    // [Quan trọng] Tựa sách là dài nhất!
        colAuthor.setPrefWidth(160);   // Tác giả cũng cần rộng rãi
        colBorrowDate.setPrefWidth(100);   // Ngày tháng (yyyy-mm-dd) cố định
        colReturnDate.setPrefWidth(100);   // Ngày tháng cố định
        colStatus.setPrefWidth(90);        // Trạng thái ngắn gọn
        // Cột STT
        colSTT.setSortable(false);
        colSTT.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        // TRÍCH XUẤT TỪ OBJECT READER
        colName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReader().getName()));
        colPhone.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReader().getPhone()));

        // TRÍCH XUẤT TỪ OBJECT BOOK
        colTitle.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBook().getTitle()));
        colAuthor.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBook().getAuthor()));

        // TRÍCH XUẤT DỮ LIỆU GỐC CỦA BORROW_RECORD
        colBorrowDate.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getBorrowDate()));
        colReturnDate.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getReturnDate()));
        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));
    }

    // 5. THIẾT LẬP TÌM KIẾM REAL-TIME
    private void setupSearchAndFilter() {
        FilteredList<BorrowRecord> filteredData = new FilteredList<>(recordList, b -> true);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(record -> {
                if (newValue == null || newValue.trim().isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();

                // Tìm theo Tên, SĐT, Tựa sách hoặc Trạng thái
                if (record.getReader().getName().toLowerCase().contains(lowerCaseFilter)) return true;
                if (record.getReader().getPhone().contains(lowerCaseFilter)) return true;
                if (record.getBook().getTitle().toLowerCase().contains(lowerCaseFilter)) return true;
                if (record.getBook().getAuthor().toLowerCase().contains(lowerCaseFilter)) return true;
                if (record.getStatus().toLowerCase().contains(lowerCaseFilter)) return true;
                if (record.getBorrowDate().toString().contains(lowerCaseFilter)) return true;

                return false;
            });
        });

        SortedList<BorrowRecord> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableBorrow.comparatorProperty());
        tableBorrow.setItems(sortedData);
    }

    // 6. RÀNG BUỘC CÁC NÚT BẤM (Vô hiệu hóa nếu không chọn dòng)
    private void setupButtonBindings() {
        // Sửa, Xóa, Trả sách chỉ sáng lên khi có 1 dòng được click chọn
        btnUpdate.disableProperty().bind(tableBorrow.getSelectionModel().selectedItemProperty().isNull());
        btnDelete.disableProperty().bind(tableBorrow.getSelectionModel().selectedItemProperty().isNull());

        // Ràng buộc kép cho nút Trả sách: Vừa phải chọn dòng, và trạng thái không được là "returned"
        btnReturn.disableProperty().bind(
                tableBorrow.getSelectionModel().selectedItemProperty().isNull()
                        .or(javafx.beans.binding.Bindings.createBooleanBinding(() -> {
                            BorrowRecord selected = tableBorrow.getSelectionModel().getSelectedItem();
                            return selected != null && "returned".equalsIgnoreCase(selected.getStatus());
                        }, tableBorrow.getSelectionModel().selectedItemProperty()))
        );
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem itemAdd = new MenuItem("Thêm mới");
        itemAdd.setOnAction(e -> onAddRecord(null));

        MenuItem itemUpdate = new MenuItem("Sửa");
        itemUpdate.setOnAction(e -> onUpdateRecord(null));
        itemUpdate.disableProperty().bind(tableBorrow.getSelectionModel().selectedItemProperty().isNull());

        // NÚT TRẢ SÁCH (Ràng buộc: Bị mờ nếu chưa chọn, hoặc phiếu đó đã có trạng thái "returned")
        MenuItem itemReturn = new MenuItem("Trả sách");
        itemReturn.setOnAction(e -> onReturnBook(null));
        itemReturn.disableProperty().bind(
                tableBorrow.getSelectionModel().selectedItemProperty().isNull()
                        .or(Bindings.createBooleanBinding(() -> {
                            BorrowRecord selected = tableBorrow.getSelectionModel().getSelectedItem();
                            return selected != null && "returned".equalsIgnoreCase(selected.getStatus());
                        }, tableBorrow.getSelectionModel().selectedItemProperty()))
        );

        MenuItem itemDelete = new MenuItem("Xóa");
        itemDelete.setOnAction(e -> onDeleteRecord(null));
        itemDelete.disableProperty().bind(tableBorrow.getSelectionModel().selectedItemProperty().isNull());

        MenuItem itemExport = new MenuItem("Xuất Excel");
        itemExport.setOnAction(e -> onExportExcel(null));

        MenuItem itemRefresh = new MenuItem("Làm mới");
        itemRefresh.setOnAction(e -> refreshTable());

        contextMenu.getItems().addAll(itemAdd, itemUpdate, itemReturn, itemDelete, new SeparatorMenuItem(), itemExport, itemRefresh);
        tableBorrow.setContextMenu(contextMenu);
    }

    // 7. LÀM MỚI BẢNG
    private void refreshTable() {
        recordList.setAll(borrowService.getAllRecord());
        tableBorrow.getSelectionModel().clearSelection();
    }

    // ==========================================
    // CÁC HÀM SỰ KIỆN NÚT BẤM (STUBS ĐỂ KHÔNG BÁO LỖI IDE)
    // ==========================================

    @FXML
    public void onDeleteRecord(ActionEvent e) {
        BorrowRecord selectedRecord = tableBorrow.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cảnh báo Xóa");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa vĩnh viễn phiếu mượn này?");
        confirmAlert.setContentText("Nếu phiếu đang ở trạng thái mượn/quá hạn, sách sẽ tự động được trả lại kho.\nHành động này không thể hoàn tác!");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                boolean isDeleted = borrowService.deleteRecord(selectedRecord.getId());
                if (isDeleted) {
                    UIUtils.showAlert("Thành công", "Đã xóa phiếu mượn và cập nhật lại kho (nếu có).");
                    refreshTable();
                } else {
                    UIUtils.showAlert("Lỗi", "Không thể xóa phiếu mượn này. Vui lòng thử lại!");
                }
            } catch (Exception ex) {
                UIUtils.showAlert("Lỗi hệ thống", ex.getMessage());
            }
        }
    }

    @FXML
    public void onReturnBook(ActionEvent e) {
        BorrowRecord selectedRecord = tableBorrow.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) return;

        // Bọc lót an toàn lớp 2 (Tránh trường hợp lách qua UI bằng phím tắt)
        if ("returned".equalsIgnoreCase(selectedRecord.getStatus())) {
            UIUtils.showAlert("Thông báo", "Phiếu mượn này đã được hoàn tất trả sách từ trước!");
            return;
        }

        // HỘP THOẠI XÁC NHẬN (Theo đúng yêu cầu của bạn)
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận Trả sách");
        confirmAlert.setHeaderText("Hệ thống sẽ cập nhật trạng thái và cộng lại sách vào kho.");
        confirmAlert.setContentText("Xác nhận Độc giả: " + selectedRecord.getReader().getName() +
                "\nĐã trả cuốn: " + selectedRecord.getBook().getTitle() + "?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Nhận boolean từ Service
                boolean isSuccess = borrowService.returnBook(selectedRecord.getId());

                if (isSuccess) {
                    UIUtils.showAlert("Thành công", "Đã trả sách thành công! Số lượng sách trong kho đã được cộng lại.");
                    refreshTable();
                } else {
                    UIUtils.showAlert("Lỗi nghiệp vụ", "Không thể thực hiện trả sách. Dữ liệu phiếu mượn có thể đã bị thay đổi.");
                }
            } catch (Exception ex) {
                UIUtils.showAlert("Lỗi hệ thống", "Lỗi: " + ex.getMessage());
            }
        }
    }

    @FXML
    public void onExportExcel(ActionEvent e) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Lưu file Excel - Danh sách Phiếu mượn");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("DanhSachPhieuMuon.xlsx");

        java.io.File file = fileChooser.showSaveDialog(tableBorrow.getScene().getWindow());

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Phiếu mượn");

                Row headerRow = sheet.createRow(0);
                String[] columns = {"STT", "Họ và Tên", "Số điện thoại", "Tựa sách", "Tác giả", "Ngày mượn", "Hạn trả", "Trạng thái"};

                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);

                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowIndex = 1;
                // Duyệt qua danh sách đang hiển thị trên bảng (đã được lọc bởi thanh tìm kiếm)
                for (BorrowRecord record : tableBorrow.getItems()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(rowIndex - 1);
                    row.createCell(1).setCellValue(record.getReader().getName());
                    row.createCell(2).setCellValue(record.getReader().getPhone());
                    row.createCell(3).setCellValue(record.getBook().getTitle());
                    row.createCell(4).setCellValue(record.getBook().getAuthor());
                    row.createCell(5).setCellValue(record.getBorrowDate() != null ? record.getBorrowDate().toString() : "");
                    row.createCell(6).setCellValue(record.getReturnDate() != null ? record.getReturnDate().toString() : "");
                    row.createCell(7).setCellValue(record.getStatus());
                }

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                UIUtils.showAlert("Thành công", "Đã xuất dữ liệu ra file Excel thành công!");

            } catch (Exception ex) {
                UIUtils.showAlert("Lỗi xuất file", "Đã xảy ra lỗi: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @FXML
    public void onAddRecord(ActionEvent e) {
        showBorrowFormDialog(null);
    }

    @FXML
    public void onUpdateRecord(ActionEvent e) {
        BorrowRecord selected = tableBorrow.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showBorrowFormDialog(selected);
        }
    }

    // Lõi xử lý Giao diện kép (Thêm / Sửa)
    private void showBorrowFormDialog(BorrowRecord recordToEdit) {
        boolean isUpdate = (recordToEdit != null);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isUpdate ? "Cập nhật Phiếu mượn" : "Tạo Phiếu mượn mới");
        dialog.getDialogPane().setPrefWidth(480);

        ButtonType btnSaveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(btnSaveType);
        saveButton.setDisable(true);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20, 40, 10, 20));

        // 1. CÁC Ô NHẬP LIỆU CƠ BẢN
        TextField txtTitle = new TextField();
        txtTitle.setPrefWidth(250);
        TextField txtAuthor = new TextField();
        txtAuthor.setPrefWidth(250);
        TextField txtPhone = new TextField();
        txtPhone.setPrefWidth(250);
        txtPhone.setPromptText("Nhập SĐT độc giả đã đăng ký");

        // [TÍNH NĂNG ĐẶC BIỆT] - Gắn Autocomplete thông minh
        // (Giả định bạn đã khai báo private BookService bookService = new BookService(); ở đầu class)
        UIUtils.setupBookAutoComplete(txtTitle, txtAuthor, bookService.getAllBook());

        grid.add(new Label("Tựa sách (*):"), 0, 0);
        grid.add(txtTitle, 1, 0);
        grid.add(new Label("Tác giả (*):"), 0, 1);
        grid.add(txtAuthor, 1, 1);
        grid.add(new Label("SĐT Độc giả (*):"), 0, 2);
        grid.add(txtPhone, 1, 2);

        // 2. KHỞI TẠO CÁC Ô ĐẶC THÙ THEO TRẠNG THÁI (THÊM HOẶC SỬA)
        Spinner<Integer> spinDays;
        DatePicker dpBorrow;
        DatePicker dpReturn;
        ComboBox<String> cbStatus;

        if (!isUpdate) {
            cbStatus = null;
            dpReturn = null;
            dpBorrow = null;
            // NẾU LÀ THÊM MỚI: Chỉ cần nhập số ngày mượn
            spinDays = new Spinner<>(1, 60, 14); // Cho mượn từ 1 đến 60 ngày, mặc định 14 ngày
            spinDays.setEditable(true);
            grid.add(new Label("Số ngày mượn:"), 0, 3);
            grid.add(spinDays, 1, 3);
        } else {
            spinDays = null;
            // NẾU LÀ CẬP NHẬT: Hiện đầy đủ Ngày tháng và Trạng thái
            dpBorrow = new DatePicker(recordToEdit.getBorrowDate());
            dpReturn = new DatePicker(recordToEdit.getReturnDate());
            cbStatus = new ComboBox<>(FXCollections.observableArrayList("borrowed", "overdue", "returned"));
            cbStatus.setValue(recordToEdit.getStatus());

            grid.add(new Label("Ngày mượn:"), 0, 3);
            grid.add(dpBorrow, 1, 3);
            grid.add(new Label("Hạn trả:"), 0, 4);
            grid.add(dpReturn, 1, 4);
            grid.add(new Label("Trạng thái:"), 0, 5);
            grid.add(cbStatus, 1, 5);

            // Đổ dữ liệu cũ vào các ô Text
            txtTitle.setText(recordToEdit.getBook().getTitle());
            txtAuthor.setText(recordToEdit.getBook().getAuthor());
            txtPhone.setText(recordToEdit.getReader().getPhone());
        }

        dialog.getDialogPane().setContent(grid);

        // 3. TRẠM KIỂM DUYỆT UI (Mở khóa nút Lưu khi nhập đủ 3 ô cơ bản)
        Runnable validateInput = () -> {
            boolean isValid = !txtTitle.getText().trim().isEmpty() &&
                    !txtAuthor.getText().trim().isEmpty() &&
                    !txtPhone.getText().trim().isEmpty();
            saveButton.setDisable(!isValid);
        };
        txtTitle.textProperty().addListener((o, old, newVal) -> validateInput.run());
        txtAuthor.textProperty().addListener((o, old, newVal) -> validateInput.run());
        txtPhone.textProperty().addListener((o, old, newVal) -> validateInput.run());
        validateInput.run(); // Chạy ngay lần đầu để khóa nút Lưu

        // 4. XỬ LÝ LƯU & BẮT LỖI TỪ ENUM (Có chặn đóng Form)
        // Dùng addEventFilter thay vì đợi dialog.showAndWait() để có thể can thiệp không cho form đóng
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            String title = txtTitle.getText().trim();
            String author = txtAuthor.getText().trim();
            String phone = txtPhone.getText().trim();

            BorrowService.BorrowStatus statusResult;

            // Gọi xuống Service
            if (isUpdate) {
                statusResult = borrowService.updateRecord(
                        recordToEdit.getId(), title, author, phone,
                        dpBorrow.getValue(), dpReturn.getValue(), cbStatus.getValue()
                );
            } else {
                statusResult = borrowService.borrowBook(title, author, phone, spinDays.getValue());
            }

            // Dịch Enum thành Thông báo bằng switch-case
            switch (statusResult) {
                case SUCCESS:
                    UIUtils.showAlert("Thành công", isUpdate ? "Cập nhật phiếu mượn thành công!" : "Tạo phiếu mượn thành công!");
                    refreshTable();
                    // Không consume event -> Form sẽ tự động đóng lại an toàn
                    break;
                case BOOK_NOT_FOUND:
                    UIUtils.showAlert("Lỗi dữ liệu", "Không tìm thấy tựa sách này của tác giả trên trong hệ thống.");
                    event.consume(); // Chặn đóng Form
                    break;
                case BOOK_OUT_OF_STOCK:
                    UIUtils.showAlert("Kho hết sách", "Cuốn sách này hiện đã được mượn hết, số lượng trong kho bằng 0.");
                    event.consume();
                    break;
                case READER_NOT_FOUND:
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Độc giả chưa đăng ký");
                    confirmAlert.setHeaderText("Số điện thoại " + phone + " chưa có trong hệ thống!");
                    confirmAlert.setContentText("Bạn có muốn đăng ký tài khoản mới cho độc giả này ngay bây giờ không?");

                    if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        // TRUYỀN HẲN Ô TEXTFIELD SĐT CỦA FORM MƯỢN VÀO ĐÂY
                        showQuickAddReaderDialog(txtPhone);
                    }
                    event.consume();
                    break;
                case BORROW_DATE_ERROR:
                    UIUtils.showAlert("Lỗi Ngày tháng", "Ngày mượn không được lớn hơn ngày hiện tại.");
                    event.consume();
                    break;
                case RETURN_DATE_ERROR:
                    UIUtils.showAlert("Lỗi Ngày tháng", "Hạn trả sách không được nhỏ hơn ngày mượn.");
                    event.consume();
                    break;
                case STATUS_ERROR:
                    UIUtils.showAlert("Lỗi Trạng thái", "Trạng thái không hợp lệ, hoặc bạn đang cố chuyển thành 'returned' sai quy trình.");
                    event.consume();
                    break;
                case RECORD_NOT_FOUND:
                case ADD_ERROR:
                case ERROR:
                default:
                    UIUtils.showAlert("Lỗi Hệ thống", "Đã xảy ra lỗi không xác định. Vui lòng thử lại!");
                    event.consume();
                    break;
            }
        });

        // Cuối cùng mới hiển thị Form lên màn hình
        dialog.showAndWait();
    }

    // Nhận tham số là TextField thay vì String
    private void showQuickAddReaderDialog(TextField parentPhoneField) {
        String currentPhone = parentPhoneField.getText().trim();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đăng ký Độc giả nhanh");
        dialog.setHeaderText("Tạo tài khoản mới");
        dialog.getDialogPane().setPrefWidth(380);

        ButtonType btnSaveType = new ButtonType("Đăng ký", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 10, 10, 10));

        TextField txtName = new TextField();
        txtName.setPrefWidth(200);

        TextField txtNewPhone = new TextField(currentPhone);
        txtNewPhone.setPrefWidth(200);

        TextField txtEmail = new TextField();
        txtEmail.setPrefWidth(200);

        grid.add(new Label("Họ và Tên (*):"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Số điện thoại (*):"), 0, 1);
        grid.add(txtNewPhone, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);

        // Ép kiểu Node sang Button để có thể gọi addEventFilter
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(btnSaveType);
        saveBtn.setDisable(true);

        Runnable validateInput = () -> {
            boolean isValid = !txtName.getText().trim().isEmpty() && !txtNewPhone.getText().trim().isEmpty();
            saveBtn.setDisable(!isValid);
        };
        txtName.textProperty().addListener((obs, oldV, newV) -> validateInput.run());
        txtNewPhone.textProperty().addListener((obs, oldV, newV) -> validateInput.run());
        validateInput.run();

        dialog.getDialogPane().setContent(grid);

// 3. SỬ DỤNG EVENT FILTER ĐỂ BẮT ENUM VÀ CHỐNG ĐÓNG FORM
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String name = txtName.getText().trim();
            String finalPhone = txtNewPhone.getText().trim();
            String email = txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim();

            // Hứng kết quả Enum thay vì boolean
            com.library.service.ReaderService.ReaderStatus statusResult = readerService.addReader(name, finalPhone, email);

            switch (statusResult) {
                case SUCCESS:
                    // Đồng bộ SĐT ra Form Phiếu mượn
                    parentPhoneField.setText(finalPhone);
                    UIUtils.showAlert("Thành công", "Đã đăng ký tài khoản Độc giả mới!\nSĐT trên phiếu mượn đã được tự động cập nhật.");
                    break;
                case PHONE_EXIST:
                    UIUtils.showAlert("Lỗi trùng lặp", "Số điện thoại này đã tồn tại trong hệ thống!");
                    event.consume();
                    break;
                case EMAIL_EXIST:
                    UIUtils.showAlert("Lỗi trùng lặp", "Email này đã tồn tại!");
                    event.consume();
                    break;
                case FORMAT_ERROR:
                    UIUtils.showAlert("Sai định dạng", "Số điện thoại (10 chữ số) hoặc Email không hợp lệ!");
                    event.consume();
                    break;
                case ERROR:
                default:
                    UIUtils.showAlert("Lỗi hệ thống", "Không thể tạo tài khoản lúc này!");
                    event.consume();
                    break;
            }
        });

        dialog.showAndWait();
    }
}