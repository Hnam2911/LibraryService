module com.library.main{
    // 1. Khai báo các thư viện hệ thống và bên thứ ba cần dùng
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.collections4;
    requires org.apache.poi.poi;
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;
    requires atlantafx.base;
    requires org.apache.poi.ooxml; // Nếu bạn vẫn đang dùng thư viện giao diện này

    // 2. CHO PHÉP JavaFX truy cập vào các Controller (để xử lý file FXML)
    // Nếu không có dòng này, JavaFX sẽ báo lỗi không thể khởi tạo Controller
    opens com.library.controller to javafx.fxml;

    // 3. CHO PHÉP JavaFX đọc dữ liệu từ các Model (để hiển thị lên TableView)
    opens com.library.model to javafx.base;

    opens com.library.main to javafx.fxml;
    // Mở thư mục chứa resources cho javafx.graphics để nó load ảnh
    opens icons to javafx.graphics;

    // NẾU thư mục view nằm ngoài cùng trong resources:
    opens view to javafx.fxml;

    // MỞ KHÓA THƯ MỤC ẢNH: Lệnh quan trọng nhất để sửa lỗi này

    // 4. Công khai package chính để khởi chạy ứng dụng
    exports com.library.main;
}