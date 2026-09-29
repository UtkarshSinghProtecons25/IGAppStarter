package firstRoute.dto;

import org.jspecify.annotations.NonNull;

public class LoginDTO {

    @NonNull
    private String email;

    @NonNull
    private String password;

    public LoginDTO(@NonNull String email, @NonNull String password) {
        this.email = email;
        this.password = password;
    }
}
