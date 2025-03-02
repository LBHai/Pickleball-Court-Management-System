package Session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "app_session";
    private static final String KEY_TOKEN = "token";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Lưu token vào session
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.commit();
    }

    // Lấy token đã lưu
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // Xóa session khi logout
    public void clearSession() {
        editor.clear();
        editor.commit();
    }
}
