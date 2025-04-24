// BadWordsLoader.java
package UI.Component;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import SEP490.G9.R;

/**
 * Utility class to load bad-word lists for English and Vietnamese.
 */
public final class BadWordsLoader {
    private static Set<String> englishBadWords = null;
    private static Set<String> vietnameseBadWords = null;

    private BadWordsLoader() { /* no-op */ }

    public static Set<String> loadEnglishBadWords(Context context) {
        if (englishBadWords != null) {
            return englishBadWords;
        }
        englishBadWords = new HashSet<>();
        try (InputStream inputStream = context.getResources().openRawResource(R.raw.bad_words_en);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    englishBadWords.add(line.toLowerCase());
                }
            }
        } catch (Exception e) {
            Log.e("BadWordsLoader", "Error loading English bad words: " + e.getMessage());
        }
        return englishBadWords;
    }

    public static Set<String> loadVietnameseBadWords(Context context) {
        if (vietnameseBadWords != null) {
            return vietnameseBadWords;
        }
        vietnameseBadWords = new HashSet<>();
        try (InputStream inputStream = context.getResources().openRawResource(R.raw.vn_offensive_words);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    vietnameseBadWords.add(line.toLowerCase());
                }
            }
        } catch (Exception e) {
            Log.e("BadWordsLoader", "Error loading Vietnamese bad words: " + e.getMessage());
        }
        return vietnameseBadWords;
    }
}
