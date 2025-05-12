package com.yourcompany.tests.auth; // Đảm bảo package này khớp với cấu trúc thư mục

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List; // Thêm import này nếu chưa có

public class LoginTest {

    WebDriver driver;
    WebDriverWait wait;
    String baseUrl = "https://webuser-dev-ex.iigvietnam.com/";

    // --- Locators (CẦN ĐƯỢC KIỂM TRA VÀ CẬP NHẬT CHÍNH XÁC BẰNG INSPECT ELEMENT) ---
    By initialLoginButton = By.xpath("//button[contains(text(),'Đăng nhập')]");
    By loginModal = By.xpath("//div[contains(@class, 'modal-login')]"); // Cần tìm class/id đúng
    By emailInput = By.xpath("//input[@placeholder='Email hoặc số điện thoại đăng nhập']");
    By passwordInput = By.xpath("//input[@placeholder='Mật khẩu']");
    By rememberMeCheckbox = By.xpath("//input[@type='checkbox' and contains(@id,'remember')]"); // Tìm ID/XPath đúng
    By submitLoginButton = By.xpath("//div[contains(@class, 'modal-login')]//button[@type='submit' and contains(text(),'Đăng nhập')]");
    By closeModalButton = By.xpath("//div[contains(@class, 'modal-login')]//button[contains(@class,'close')]"); // Tìm nút đóng đúng
    By userProfileElement = By.xpath("//a[contains(@href,'/profile')] | //*[contains(text(),'Tài khoản của tôi')]"); // Ví dụ element sau khi login
    By logoutButton = By.xpath("//button[contains(text(),'Đăng xuất')] | //a[contains(text(),'Đăng xuất')]"); // Ví dụ nút đăng xuất
    By errorMessage = By.xpath("//div[contains(@class,'alert-danger')] | //*[contains(@class,'error-message')] | //*[contains(text(),'không đúng')]"); // Tìm vùng báo lỗi

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get(baseUrl);
        System.out.println("Đã truy cập trang: " + baseUrl);
    }

    @Test(description = "Đăng nhập thành công với tài khoản hợp lệ", priority = 1)
    public void testSuccessfulLogin() {
        String validEmail = "your_valid_email@example.com"; // *** THAY EMAIL HỢP LỆ ***
        String validPassword = "your_valid_password";       // *** THAY MẬT KHẨU HỢP LỆ ***

        System.out.println("Bắt đầu test: testSuccessfulLogin");

        try {
            wait.until(ExpectedConditions.elementToBeClickable(initialLoginButton)).click();
            System.out.println("Đã click nút Đăng nhập ban đầu.");
        } catch (Exception e) {
            Assert.fail("Không thể click nút Đăng nhập ban đầu. Locator: " + initialLoginButton, e);
        }

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(loginModal));
            System.out.println("Popup đăng nhập đã hiển thị.");
        } catch (TimeoutException e) {
            Assert.fail("Popup đăng nhập không hiển thị sau khi click. Locator modal: " + loginModal);
        }

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(validEmail);
            driver.findElement(passwordInput).sendKeys(validPassword);
            System.out.println("Đã nhập email và mật khẩu.");
        } catch (Exception e) {
             Assert.fail("Không thể nhập liệu vào form đăng nhập.", e);
        }

        try {
            wait.until(ExpectedConditions.elementToBeClickable(submitLoginButton)).click();
            System.out.println("Đã click nút Đăng nhập trong popup.");
        } catch (Exception e) {
             Assert.fail("Không thể click nút Đăng nhập trong popup. Locator: " + submitLoginButton, e);
        }

        try {
            wait.until(ExpectedConditions.or(
                 ExpectedConditions.visibilityOfElementLocated(userProfileElement),
                 ExpectedConditions.visibilityOfElementLocated(logoutButton)
            ));
            System.out.println("Đăng nhập thành công! Element xác nhận đã xuất hiện.");
            boolean isLoggedIn = driver.findElements(userProfileElement).size() > 0 || driver.findElements(logoutButton).size() > 0;
            Assert.assertTrue(isLoggedIn, "Đăng nhập thành công nhưng không tìm thấy element xác nhận (Profile/Logout).");
        } catch (TimeoutException e) {
             List<WebElement> errorMessages = driver.findElements(errorMessage);
             if(!errorMessages.isEmpty() && errorMessages.get(0).isDisplayed()){
                 Assert.fail("Đăng nhập thất bại, hiển thị thông báo lỗi: " + errorMessages.get(0).getText());
             } else {
                Assert.fail("Đăng nhập không thành công hoặc element xác nhận không xuất hiện trong thời gian chờ.");
             }
        }
    }

    @Test(description = "Đăng nhập thất bại với mật khẩu không đúng", priority = 2)
    public void testLoginWithInvalidPassword() {
        String validEmail = "your_valid_email@example.com"; // *** THAY EMAIL HỢP LỆ ***
        String invalidPassword = "wrong_password";         // *** THAY MẬT KHẨU SAI ***

        System.out.println("Bắt đầu test: testLoginWithInvalidPassword");

        wait.until(ExpectedConditions.elementToBeClickable(initialLoginButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginModal));
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(validEmail);
        driver.findElement(passwordInput).sendKeys(invalidPassword);
        System.out.println("Đã nhập email và mật khẩu không hợp lệ.");

        wait.until(ExpectedConditions.elementToBeClickable(submitLoginButton)).click();
        System.out.println("Đã click nút Đăng nhập trong popup.");

        try {
            WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
            Assert.assertTrue(errorElement.isDisplayed(), "Thông báo lỗi không hiển thị.");
            String errorText = errorElement.getText();
            System.out.println("Hiển thị thông báo lỗi: " + errorText);
            Assert.assertTrue(errorText.contains("không đúng") || errorText.contains("invalid") || errorText.contains("Incorrect"), "Nội dung thông báo lỗi không như mong đợi."); // Mở rộng kiểm tra lỗi
             Assert.assertTrue(driver.findElement(loginModal).isDisplayed(), "Popup đăng nhập đã biến mất sau khi nhập sai.");
        } catch (TimeoutException e) {
            Assert.fail("Không tìm thấy thông báo lỗi sau khi đăng nhập thất bại. Locator lỗi: " + errorMessage);
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println("Trình duyệt đã đóng.");
        }
    }
}