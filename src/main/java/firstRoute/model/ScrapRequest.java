package firstRoute.model;
import io.swagger.v3.oas.annotations.media.Schema;

public class ScrapRequest {

    @Schema(description = "Username used to authenticate the user")
    private String username;

    @Schema(description = "Password used to authenticate the user")
    private String password;

    @Schema(
            description = "Specifies whether the user is an existing user or a new user",
            example = "new",
            allowableValues = {"new", "existing"}
    )
    private String usertype;

    @Schema(
            description = "Action to be performed on the vehicle",
            example = "scrap",
            allowableValues = {"scrap"}
    )
    private String action;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
