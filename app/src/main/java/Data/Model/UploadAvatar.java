package Data.Model;

import com.google.gson.annotations.SerializedName;

public class UploadAvatar {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    // API trả về key "file" chứa URL của ảnh sau khi upload thành công
    @SerializedName("file")
    private String file;

    // Trường oldPath, kiểu String, mặc định là null
    @SerializedName("oldPath")
    private String oldPath = null;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }
}