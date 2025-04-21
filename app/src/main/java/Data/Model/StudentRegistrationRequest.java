package Data.Model;

import com.google.gson.annotations.SerializedName;

public class StudentRegistrationRequest {
    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("dob")
    private String dob;

    @SerializedName("email")
    private String email;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    public StudentRegistrationRequest(String username, String password,
                                      String firstName, String lastName,
                                      String dob, String email,
                                      String phoneNumber) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Getters and setters omitted for brevity
}