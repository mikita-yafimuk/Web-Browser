package browser.utils.win32;

import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinBase.SYSTEMTIME;
import com.sun.jna.platform.win32.WinDef.HWND;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class NativeCalls {

    public void showLocalTime() {
        Kernel32 lib = Kernel32.INSTANCE;
        SYSTEMTIME systemtime = new SYSTEMTIME();
        lib.GetLocalTime(systemtime);

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText("Time information");
        alert.setContentText(systemtime.toString());

        alert.showAndWait();
    }

    public void showProcessInfo() {
        Kernel32 lib = Kernel32.INSTANCE;
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText("Thread and process information");
        alert.setContentText("Process id = " + lib.GetCurrentProcessId() + "\nThread id = "
                + lib.GetCurrentThreadId() + "\nTick count = " + lib.GetTickCount());

        alert.showAndWait();
    }

    public void showSystemInfo() {
        com.sun.jna.platform.win32.Kernel32 lib = com.sun.jna.platform.win32.Kernel32.INSTANCE;
        WinBase.SYSTEM_INFO systemInfo = new WinBase.SYSTEM_INFO();
        lib.GetSystemInfo(systemInfo);
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText("System information");
        alert.setContentText("Computer name = " + Kernel32Util.getComputerName() + "\nUser name = " + Advapi32Util.getUserName()
                + "\nNumber of processors = " + systemInfo.dwNumberOfProcessors + "\nPage size = "
                + systemInfo.dwPageSize + "\nProcessor Level = " + systemInfo.wProcessorLevel);

        alert.showAndWait();
    }

    public void closeWindow() {
        User32 lib = User32.INSTANCE;
        HWND window = lib.GetActiveWindow();
        lib.DestroyWindow(window);
    }

    public void minimizeWindow() {
        User32 lib = User32.INSTANCE;
        HWND window = lib.GetActiveWindow();
        lib.CloseWindow(window);
    }

    public void lockWorkstation() {
        User32 lib = User32.INSTANCE;
        lib.LockWorkStation();
    }

    public void makeScreenshoot() {
        User32 lib = User32.INSTANCE;
        HWND window = lib.GetActiveWindow();
        BufferedImage image = GDI32Util.getScreenshot(window);
        File outputfile = new File("src/resources/screenshots/screenshot.png");
        try {
            ImageIO.write(image, "png", outputfile);
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setHeaderText("Screenshot successfully saved!");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


