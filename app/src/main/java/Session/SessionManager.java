package Session;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_GUEST_PHONES = "guestPhones";
    private static final String KEY_HAS_SHOWN_GUEST_DIALOG = "hasShownGuestDialog";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    public void saveUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public void addGuestPhone(String phone) {
        List<String> guestPhones = getGuestPhones();
        if (!guestPhones.contains(phone)) {
            guestPhones.add(phone);
            editor.putString(KEY_GUEST_PHONES, gson.toJson(guestPhones));
            editor.apply();
        }
    }

    public List<String> getGuestPhones() {
        String json = pref.getString(KEY_GUEST_PHONES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        return gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
    }

    public void clearGuestPhones() {
        editor.remove(KEY_GUEST_PHONES);
        editor.apply();
    }

    public void setHasShownGuestDialog(boolean hasShown) {
        editor.putBoolean(KEY_HAS_SHOWN_GUEST_DIALOG, hasShown);
        editor.apply();
    }

    public boolean hasShownGuestDialog() {
        return pref.getBoolean(KEY_HAS_SHOWN_GUEST_DIALOG, false);
    }

    public void clearSession() {
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER_ID);
        // Không xóa KEY_HAS_SHOWN_GUEST_DIALOG để giữ trạng thái hiển thị dialog
        editor.apply();
    }
}