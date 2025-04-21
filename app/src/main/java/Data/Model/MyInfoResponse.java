package Data.Model;

public class MyInfoResponse {
    private String code;
    private String message; // Thêm trường message
    private MyInfo result;

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public MyInfo getResult() {
        return result;
    }
    public void setResult(MyInfo result) {
        this.result = result;
    }
}