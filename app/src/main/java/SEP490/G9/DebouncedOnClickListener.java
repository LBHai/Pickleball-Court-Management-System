package SEP490.G9;

import android.os.SystemClock;
import android.view.View;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Một OnClickListener với chức năng chống spam click
 * Loại bỏ các click quá gần nhau về mặt thời gian
 * Class này an toàn để sử dụng cho nhiều view khác nhau
 */
public abstract class DebouncedOnClickListener implements View.OnClickListener {
    private final long minimumInterval;
    private Map<View, Long> lastClickMap;

    /**
     * Thực hiện phương thức này thay vì onClick
     * @param v View đã được click
     */
    public abstract void onDebouncedClick(View v);

    /**
     * Constructor
     * @param minimumIntervalMsec Khoảng thời gian tối thiểu giữa các lần click (mili giây)
     */
    public DebouncedOnClickListener(long minimumIntervalMsec) {
        this.minimumInterval = minimumIntervalMsec;
        this.lastClickMap = new WeakHashMap<View, Long>();
    }

    @Override
    public void onClick(View clickedView) {
        Long previousClickTimestamp = lastClickMap.get(clickedView);
        long currentTimestamp = SystemClock.uptimeMillis();
        lastClickMap.put(clickedView, currentTimestamp);

        if(previousClickTimestamp == null || (currentTimestamp - previousClickTimestamp.longValue() > minimumInterval)) {
            onDebouncedClick(clickedView);
        }
    }
}
