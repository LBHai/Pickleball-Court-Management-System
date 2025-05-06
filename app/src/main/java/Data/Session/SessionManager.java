package Data.Session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_PHONE_NUMBER = "phoneNumber";
    private static final String KEY_GUEST_PHONE = "guestPhone";
    private static final String KEY_HAS_SHOWN_GUEST_DIALOG = "hasShownGuestDialog";
    private static final String KEY_FAVORITE_COURTS = "favoriteCourts";
    private static final String KEY_STUDENT_STATUS = "isStudent";
    private static final String KEY_AVATAR_URL = "avatarUrl";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Gson gson = new Gson();

    public SessionManager(Context context) {
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    public void saveUserId(String userId) {
        editor.putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public void setPhoneNumber(String phoneNumber) {
        editor.putString(KEY_PHONE_NUMBER, phoneNumber).apply();
    }

    public String getPhoneNumber() {
        return pref.getString(KEY_PHONE_NUMBER, null);
    }

    public boolean isLoggedIn() {
        return getUserId() != null || getPhoneNumber() != null;
    }

    public void clearSession() {
        editor.remove(KEY_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_STUDENT_STATUS)
                .remove(KEY_AVATAR_URL)
                .apply();
    }

    public void saveStudentStatus(boolean isStudent) {
        editor.putBoolean(KEY_STUDENT_STATUS, isStudent).apply();
        Log.d("SessionManager", "Lưu isStudent: " + isStudent);
    }

    public boolean getStudentStatus() {
        boolean isStudent = pref.getBoolean(KEY_STUDENT_STATUS, false);
        Log.d("SessionManager", "Lấy isStudent: " + isStudent);
        return isStudent;
    }

    /** Avatar URL storage **/
    public void saveAvatarUrl(String url) {
        editor.putString(KEY_AVATAR_URL, url).apply();
        Log.d("SessionManager", "Lưu avatarUrl: " + url);
    }

    public String getAvatarUrl() {
        String url = pref.getString(KEY_AVATAR_URL, null);
        Log.d("SessionManager", "Lấy avatarUrl: " + url);
        return url;
    }

    public void setGuestPhone(String phone) {
        if (isUserPhone(phone)) {
            return;
        }
        editor.putString(KEY_GUEST_PHONE, phone).apply();
    }

    public String getGuestPhone() {
        return pref.getString(KEY_GUEST_PHONE, null);
    }

    public void clearGuestPhone() {
        editor.remove(KEY_GUEST_PHONE).apply();
    }

    public boolean isUserPhone(String phone) {
        String userPhone = getPhoneNumber();
        return userPhone != null && userPhone.equals(phone);
    }

    public void cleanupUserPhoneFromGuestList() {
        String userPhone = getPhoneNumber();
        String guestPhone = getGuestPhone();
        if (userPhone != null && !userPhone.isEmpty() &&
                guestPhone != null && guestPhone.equals(userPhone)) {
            clearGuestPhone();
        }
    }

    public void setHasShownGuestDialog(boolean hasShown) {
        editor.putBoolean(KEY_HAS_SHOWN_GUEST_DIALOG, hasShown).apply();
    }

    public boolean hasShownGuestDialog() {
        return pref.getBoolean(KEY_HAS_SHOWN_GUEST_DIALOG, false);
    }

    public void saveFavoriteCourts(List<String> courtIds) {
        editor.putStringSet(KEY_FAVORITE_COURTS, new HashSet<>(courtIds)).apply();
    }

    public List<String> getFavoriteCourts() {
        return new ArrayList<>(pref.getStringSet(KEY_FAVORITE_COURTS, new HashSet<>()));
    }

    public void addFavoriteCourt(String courtId) {
        Set<String> favorites = new HashSet<>(getFavoriteCourts());
        favorites.add(courtId);
        saveFavoriteCourts(new ArrayList<>(favorites));
    }

    public void removeFavoriteCourt(String courtId) {
        Set<String> favorites = new HashSet<>(getFavoriteCourts());
        favorites.remove(courtId);
        saveFavoriteCourts(new ArrayList<>(favorites));
    }

    public boolean isCourtFavorite(String courtId) {
        return getFavoriteCourts().contains(courtId);
    }
}