package com.library.controller;

import com.library.model.Reader;
import com.library.service.ReaderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.library.util.UIUtils;
import java.util.Optional;

public class ReaderController {

    @FXML
    private TableView<Reader> tableReader;
    @FXML
    private TableColumn<Reader, Integer> colSTT;
    @FXML
    private TableColumn<Reader, String> colName;
    @FXML
    private TableColumn<Reader, String> colPhone;
    @FXML
    private TableColumn<Reader, String> colEmail;
    @FXML
    private TableColumn<Reader, Integer> colBorrowed; // Cột Đang mượn

    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnExcel;

    private ObservableList<Reader> readerList = FXCollections.observableArrayList();
    private ReaderService readerService = new ReaderService();

    @FXML
    public void initialize() {
        // 1. Cài đặt Cột
        colSTT.setSortable(false);
        colSTT.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colBorrowed.setCellValueFactory(new PropertyValueFactory<>("borrowedQuantity"));

        tableReader.setFixedCellSize(35); // Chống lỗi bốc hơi dòng

        // 2. Tái sử dụng logic Tìm kiếm Real-time (Tìm theo Tên hoặc SĐT)
        FilteredList<Reader> filteredData = new FilteredList<>(readerList, b -> true);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(reader -> {
                if (newValue == null || newValue.trim().isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();

                if (reader.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                if (reader.getPhone().contains(lowerCaseFilter)) return true;
                if (reader.getEmail().contains(lowerCaseFilter)) return true;
                return false;
            });
        });

        SortedList<Reader> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableReader.comparatorProperty());
        tableReader.setItems(sortedData);

        // 3. Ràng buộc Nút bấm (Chỉ Disable Sửa/Xóa khi chưa chọn)
        btnUpdate.disableProperty().bind(tableReader.getSelectionModel().selectedItemProperty().isNull());
        btnDelete.disableProperty().bind(tableReader.getSelectionModel().selectedItemProperty().isNull());

        // 4. Khởi tạo Menu chuột phải (Tái sử dụng cấu trúc từ BookController)
        setupContextMenu();

        refreshTable();
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem itemAdd = new MenuItem("Thêm mới");
        itemAdd.setOnAction(e -> onAddReader(null));

        MenuItem itemUpdate = new MenuItem("Sửa");
        itemUpdate.setOnAction(e -> onUpdateReader(null));
        itemUpdate.disableProperty().bind(tableReader.getSelectionModel().selectedItemProperty().isNull());

        MenuItem itemDelete = new MenuItem("Xóa");
        itemDelete.setOnAction(e -> onDeleteReader(null));
        itemDelete.disableProperty().bind(tableReader.getSelectionModel().selectedItemProperty().isNull());

        MenuItem itemExport = new MenuItem("Xuất Excel");
        itemExport.setOnAction(e -> onExportExcel(null));

        MenuItem itemRefresh = new MenuItem("Làm mới");
        itemRefresh.setOnAction(e -> refreshTable());

        contextMenu.getItems().addAll(itemAdd, itemUpdate, itemDelete, new SeparatorMenuItem(), itemExport, itemRefresh);
        tableReader.setContextMenu(contextMenu);
    }

    private void refreshTable() {
        readerList.setAll(readerService.getAllReader());
        tableReader.getSelectionModel().clearSelection();
    }
    @FXML
    public void onAddReader(javafx.event.ActionEvent event) {
        // Gọi thẳng đến Form Dialog và truyền null để báo hiệu đây là Thêm Mới
        showReaderFormDialog(null);
    }

    @FXML
    public void onUpdateReader(javafx.event.ActionEvent event) {
        Reader selectedReader = tableReader.getSelectionModel().getSelectedItem();
        if (selectedReader != null) {
            // Truyền đối tượng đã chọn vào Form Dialog để nó tự động đổ dữ liệu cũ
            showReaderFormDialog(selectedReader);
        }
    }
    @FXML
    public void onDeleteReader(javafx.event.ActionEvent event) {
        Reader selectedReader = tableReader.getSelectionModel().getSelectedItem();

        // Bọc lót an toàn: Tránh lỗi NullPointerException nếu chưa chọn dòng
        if (selectedReader == null) {
            return;
        }

        // 1. KIỂM TRA NGHIỆP VỤ: Đang mượn sách thì KHÔNG ĐƯỢC XÓA
        if (selectedReader.getBorrowedQuantity() > 0) {
            UIUtils.showAlert("Lỗi nghiệp vụ", "Không thể xóa độc giả này vì họ đang cầm " + selectedReader.getBorrowedQuantity() + " cuốn sách chưa trả!");
            return;
        }

        // 2. HIỂN THỊ HỘP THOẠI XÁC NHẬN
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa độc giả này?");
        confirmAlert.setContentText("Họ và Tên: " + selectedReader.getName() + "\nSố điện thoại: " + selectedReader.getPhone());

        // 3. XỬ LÝ KẾT QUẢ
        if (confirmAlert.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK) {
            try {
                // Giả định hàm deleteReader của Service nhận vào ID và trả về boolean
                boolean isDeleted = readerService.deleteReader(selectedReader.getId());

                if (isDeleted) {
                    UIUtils.showAlert("Thành công", "Đã xóa độc giả khỏi hệ thống.");
                    refreshTable(); // Làm mới bảng ngay lập tức
                } else {
                    UIUtils.showAlert("Lỗi hệ thống", "Đã xảy ra lỗi khi xóa. Vui lòng thử lại!");
                }
            } catch (Exception ex) {
                UIUtils.showAlert("Lỗi hệ thống", ex.getMessage());
            }
        }
    }

    @FXML
    public void onExportExcel(javafx.event.ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Lưu file Excel - Danh sách Độc giả");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("DanhSachDocGia.xlsx");

        java.io.File file = fileChooser.showSaveDialog(tableReader.getScene().getWindow());

        if (file != null) {
            try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Độc giả");

                org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
                // Các cột tương ứng với ReaderView
                String[] columns = {"STT", "Họ và Tên", "Số điện thoại", "Email", "Đang mượn"};

                org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);

                for (int i = 0; i < columns.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowIndex = 1;
                // Dùng tableReader.getItems() để chỉ xuất những người đang được lọc bởi ô Tìm kiếm
                for (Reader reader : tableReader.getItems()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(rowIndex - 1);
                    row.createCell(1).setCellValue(reader.getName());
                    row.createCell(2).setCellValue(reader.getPhone());
                    row.createCell(3).setCellValue(reader.getEmail() != null ? reader.getEmail() : "");
                    row.createCell(4).setCellValue(reader.getBorrowedQuantity());
                }

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                UIUtils.showAlert("Thành công", "Đã xuất dữ liệu ra file Excel thành công!");

            } catch (Exception e) {
                UIUtils.showAlert("Lỗi xuất file", "Đã xảy ra lỗi: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    private void showReaderFormDialog(Reader readerToEdit) {
        boolean isUpdate = (readerToEdit != null);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isUpdate ? "Cập nhật Độc giả" : "Thêm Độc giả mới");

        ButtonType btnSaveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        // TỐI ƯU GIAO DIỆN: Cửa sổ rộng rãi
        dialog.getDialogPane().setPrefWidth(450);

        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(btnSaveType);
        saveButton.setDisable(true);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 40, 10, 10));

        // TẠO CÁC Ô NHẬP LIỆU (Kéo dài 300px và chống nhảy Form bằng UIUtils)
        TextField txtName = new TextField();
        txtName.setPrefWidth(300);
        javafx.scene.layout.VBox boxName = UIUtils.createValidatedTextField(txtName, "Tên không được để trống!");

        TextField txtPhone = new TextField();
        txtPhone.setPrefWidth(300);
        javafx.scene.layout.VBox boxPhone = UIUtils.createValidatedTextField(txtPhone, "Số điện thoại không được để trống!");

        TextField txtEmail = new TextField();
        txtEmail.setPrefWidth(300);
        // Email có thể null nên không cần validate bắt buộc gõ, nhưng vẫn cho vào VBox để giữ layout cân xứng
        Label lblEmailError = new Label(" "); // Khoảng trắng giữ chỗ
        lblEmailError.setStyle("-fx-text-fill: red; -fx-font-size: 10px; -fx-font-style: italic;");
        javafx.scene.layout.VBox boxEmail = new javafx.scene.layout.VBox(3, txtEmail, lblEmailError);

        grid.add(new Label("Họ và Tên (*):"), 0, 0);
        grid.add(boxName, 1, 0);
        grid.add(new Label("Số điện thoại (*):"), 0, 1);
        grid.add(boxPhone, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(boxEmail, 1, 2); // ĐÃ XÓA hoàn toàn ô "Đang mượn"

        // ĐỔ DỮ LIỆU CŨ NẾU LÀ UPDATE
        if (isUpdate) {
            txtName.setText(readerToEdit.getName());
            txtPhone.setText(readerToEdit.getPhone());
            txtEmail.setText(readerToEdit.getEmail() != null ? readerToEdit.getEmail() : "");
        }

        dialog.getDialogPane().setContent(grid);

        // TRẠM KIỂM DUYỆT UI (Chỉ check rỗng)
        Runnable validateInput = () -> {
            boolean isNameValid = !txtName.getText().trim().isEmpty();
            boolean isPhoneValid = !txtPhone.getText().trim().isEmpty();
            saveButton.setDisable(!(isNameValid && isPhoneValid));
        };

        txtName.textProperty().addListener((obs, o, n) -> validateInput.run());
        txtPhone.textProperty().addListener((obs, o, n) -> validateInput.run());
        validateInput.run();

        // XỬ LÝ LƯU
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == btnSaveType) {
            String name = txtName.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim();

            try {
                if (isUpdate) {
                    if(!readerService.updateReader(readerToEdit.getId(), name, phone, email)){
                        UIUtils.showAlert("Lỗi","Hãy kiểm tra định dạng email hoặc sđt!");
                    }
                    else UIUtils.showAlert("Thành công", "Đã cập nhật thông tin Độc giả!");
                } else {
                    if(!readerService.addReader(name, phone, email)){
                        UIUtils.showAlert("Lỗi","Hãy kiểm tra định dạng email hoặc sđt!");
                    }
                    else UIUtils.showAlert("Thành công", "Đã thêm Độc giả mới!");
                }
                refreshTable();
            } catch (Exception ex) {
                // Hiển thị trực tiếp message lỗi từ Service (ví dụ: "Số điện thoại đã tồn tại")
                UIUtils.showAlert("Lỗi nghiệp vụ", ex.getMessage());
            }
        }
    }
}