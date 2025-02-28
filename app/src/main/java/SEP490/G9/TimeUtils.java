package SEP490.G9;


import java.util.ArrayList;
import java.util.List;

public class TimeUtils {
    /**
     * Tạo danh sách mốc giờ từ startTime đến endTime (không bao gồm endTime),
     * mỗi stepMinutes là 1 cột.
     * Vd: generateTimeSlots("05:00:00","20:00:00",30) -> 05:00:00,05:30:00,06:00:00,...
     */
    public static List<String> generateTimeSlots(String startTime, String endTime, int stepMinutes) {
        List<String> result = new ArrayList<>();

        int startMin = toMinutes(startTime); // 05:00 -> 300
        int endMin   = toMinutes(endTime);   // 20:00 -> 1200

        for (int current = startMin; current < endMin; current += stepMinutes) {
            result.add(minutesToString(current)); // "HH:mm:ss"
        }

        return result;
    }

    private static int toMinutes(String hhmmss) {
        String[] parts = hhmmss.split(":");
        int hh = Integer.parseInt(parts[0]);
        int mm = Integer.parseInt(parts[1]);
        return hh*60 + mm;
    }

    private static String minutesToString(int totalMin) {
        int hh = totalMin/60;
        int mm = totalMin%60;
        return String.format("%02d:%02d:00", hh, mm);
    }
}
