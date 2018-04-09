package browser.util.win32;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class SystemTime extends Structure {
    public short wYear;
    public short wMonth;
    public short wDayOfWeek;
    public short wDay;
    public short wHour;
    public short wMinute;
    public short wSecond;
    public short wMilliseconds;

    protected List getFieldOrder() {
        return Arrays.asList("wYear", "wMonth", "wDayOfWeek", "wDay", "wHour", "wMinute", "wSecond", "wMilliseconds");
    }

    @Override
    public String toString() {
        return "SystemTime {" +
                "\nwYear=" + wYear +
                ", \nwMonth=" + wMonth +
                ", \nwDayOfWeek=" + wDayOfWeek +
                ", \nwDay=" + wDay +
                ", \nwHour=" + wHour +
                ", \nwMinute=" + wMinute +
                ", \nwSecond=" + wSecond +
                ", \nwMilliseconds=" + wMilliseconds +
                "\n}";
    }
}
