package com.cyber.server.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.cyber.server.model.User;

import javafx.geometry.Insets;
import com.cyber.server.validation.UserNameValidator;

public class CustomerController {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> passwordColumn;

    @FXML
    private TableColumn<User, Double> balanceColumn;

    @FXML
    private TableColumn<User, String> phoneColumn; // Cột số điện thoại

    private ObservableList<User> userList;

    public void initialize() {
        // Khởi tạo bảng người dùng
        userList = FXCollections.observableArrayList(
                new User("Hoàng Văn Trầng", "password1", 100.0, "0123456789"),
                new User("Nguyễn Văn Hải", "password2", 50.0, "0987654321")
        );

        // Liên kết các cột với thuộc tính người dùng
        usernameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        passwordColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPassword()));
        balanceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getBalance()).asObject());
        phoneColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPhoneNumber())); // Liên kết cột số điện thoại

        // Hiển thị dữ liệu vào bảng
        userTable.setItems(userList);

    }



    @FXML
    private void napTienAction() {
        // Gọi phương thức napTien() để hiển thị mã QR
        napTien();

        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            TextInputDialog dialog = new TextInputDialog("0");
            dialog.setTitle("Nạp/Tạo tiền");
            dialog.setHeaderText("Nhập số tiền bạn muốn thay đổi:");
            dialog.setContentText("Số tiền (có thể âm):");

            dialog.showAndWait().ifPresent(amount -> {
                try {
                    double money = Double.parseDouble(amount);
                    selectedUser.setBalance(selectedUser.getBalance() + money); // Cộng hoặc trừ tiền
                    userTable.refresh(); // Cập nhật bảng sau khi thay đổi
                } catch (NumberFormatException e) {
                    showAlert("Vui lòng nhập số hợp lệ.");
                }
            });
        } else {
            showAlert("Vui lòng chọn một tài khoản để nạp tiền.");
        }
    }

    @FXML
    private void timKiemTaiKhoan() {
        // Hiển thị hộp thoại nhập chữ cái đầu tiên của tên
        TextInputDialog searchDialog = new TextInputDialog();
        searchDialog.setTitle("Tìm kiếm tài khoản");
        searchDialog.setHeaderText("Nhập chữ cái đầu tiên của tên người dùng:");
        searchDialog.setContentText("Chữ cái đầu tiên:");

        searchDialog.showAndWait().ifPresent(letter -> {
            if (letter != null && !letter.trim().isEmpty()) {
                char searchLetter = letter.trim().toUpperCase().charAt(0); // Lấy ký tự đầu tiên và chuyển thành chữ hoa
                ObservableList<User> filteredList = FXCollections.observableArrayList();

                // Lọc danh sách người dùng theo chữ cái đầu tiên của tên (tách họ và tên)
                for (User user : userList) {
                    // Tách họ và tên người dùng
                    String[] nameParts = user.getUsername().split("\\s+"); // Tách tên theo khoảng trắng
                    if (nameParts.length > 0 && nameParts[0].toUpperCase().charAt(0) == searchLetter) {
                        filteredList.add(user);
                    }
                }

                if (!filteredList.isEmpty()) {
                    userTable.setItems(filteredList); // Cập nhật bảng với danh sách đã lọc
                } else {
                    showAlert("Không tìm thấy tài khoản có chữ cái đầu tiên của tên là " + searchLetter);
                }
            }
        });
    }



    public void napTien() {
        try {
            // Tạo cửa sổ mới hiển thị mã QR
            Stage stage = new Stage();
            stage.setTitle("QR Code");

            // Tạo ImageView để hiển thị hình ảnh
            ImageView imageView = new ImageView();

            // Tải ảnh QR từ classpath
            Image qrImage = new Image(getClass().getResource("/quanlynet/img/qr.png").toExternalForm());
            imageView.setImage(qrImage);

            // Thiết lập kích thước cho ImageView (hình vuông)
            imageView.setFitWidth(200);  // Chiều rộng 200px
            imageView.setFitHeight(200); // Chiều cao 200px
            imageView.setPreserveRatio(false); // Không bảo toàn tỷ lệ để hình vuông

            // Sắp xếp nội dung trong VBox
            VBox vBox = new VBox(10, imageView);
            vBox.setAlignment(Pos.CENTER); // Căn giữa nội dung
            vBox.setPadding(new Insets(20)); // Tạo khoảng cách bên trong

            // Tạo Scene và gắn vào Stage
            Scene scene = new Scene(vBox, 300, 300); // Kích thước cửa sổ 300x300
            stage.setScene(scene);

            // Hiển thị cửa sổ
            stage.show();

        } catch (NullPointerException e) {
            // Thông báo nếu không tìm thấy hình ảnh
            showAlert("Không tìm thấy hình ảnh QR. Vui lòng kiểm tra đường dẫn.");
        }
    }

    @FXML
    private void themTaiKhoan() {
        TextInputDialog usernameDialog = new TextInputDialog();
        usernameDialog.setTitle("Thêm tài khoản");
        usernameDialog.setHeaderText("Nhập tên đăng nhập mới:");
        usernameDialog.setContentText("Tên đăng nhập:");

        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Thêm tài khoản");
        passwordDialog.setHeaderText("Nhập mật khẩu mới:");
        passwordDialog.setContentText("Mật khẩu:");

        TextInputDialog phoneDialog = new TextInputDialog();
        phoneDialog.setTitle("Thêm tài khoản");
        phoneDialog.setHeaderText("Nhập số điện thoại:");
        phoneDialog.setContentText("Số điện thoại:");

        TextInputDialog balanceDialog = new TextInputDialog("0");
        balanceDialog.setTitle("Thêm tài khoản");
        balanceDialog.setHeaderText("Nhập số dư tài khoản:");
        balanceDialog.setContentText("Số tiền:");

        usernameDialog.showAndWait().ifPresent(newUsername -> {
            if (!UserNameValidator.isValidUsername(newUsername)) {
                showAlert("Tên đăng nhập không được chứa dấu cách.");
                return;
            }

            passwordDialog.showAndWait().ifPresent(newPassword -> {
                balanceDialog.showAndWait().ifPresent(balance -> {
                    phoneDialog.showAndWait().ifPresent(phoneNumber -> {
                        try {
                            double initialBalance = Double.parseDouble(balance);
                            User newUser = new User(newUsername, newPassword, initialBalance, phoneNumber);
                            userList.add(newUser);
                            userTable.refresh();
                        } catch (NumberFormatException e) {
                            showAlert("Vui lòng nhập số dư hợp lệ.");
                        }
                    });
                });
            });
        });
    }


    @FXML
    private void xoaTaiKhoan() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            userList.remove(selectedUser);
        } else {
            showAlert("Vui lòng chọn một tài khoản để xóa.");
        }
    }

    @FXML
    private void suaTaiKhoan() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            TextInputDialog usernameDialog = new TextInputDialog(selectedUser.getUsername());
            usernameDialog.setTitle("Sửa tài khoản");
            usernameDialog.setHeaderText("Nhập tên đăng nhập mới:");
            usernameDialog.setContentText("Tên đăng nhập:");

            TextInputDialog passwordDialog = new TextInputDialog(selectedUser.getPassword());
            passwordDialog.setTitle("Sửa tài khoản");
            passwordDialog.setHeaderText("Nhập mật khẩu mới:");
            passwordDialog.setContentText("Mật khẩu:");

            TextInputDialog phoneDialog = new TextInputDialog(selectedUser.getPhoneNumber());
            phoneDialog.setTitle("Sửa tài khoản");
            phoneDialog.setHeaderText("Nhập số điện thoại mới:");
            phoneDialog.setContentText("Số điện thoại:");

            usernameDialog.showAndWait().ifPresent(newUsername -> {
                passwordDialog.showAndWait().ifPresent(newPassword -> {
                    phoneDialog.showAndWait().ifPresent(newPhoneNumber -> {
                        // Cập nhật tên, mật khẩu và số điện thoại
                        selectedUser.setUsername(newUsername);
                        selectedUser.setPassword(newPassword);
                        selectedUser.setPhoneNumber(newPhoneNumber);
                        userTable.refresh();
                    });
                });
            });
        } else {
            showAlert("Vui lòng chọn một tài khoản để sửa.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}