package org.sangyunpark.pointpay.domain.user.dto.request;

import jakarta.validation.constraints.*;

public class UserCreateRequest {

    @NotBlank
    @Size(min = 3, max = 10)
    private String username;

    @NotNull
    @Min(value = 0)
    @Max(value = 2_100_000_000)
    private int pointBalance;

    public UserCreateRequest(final String username, final int pointBalance) {
        this.username = username;
        this.pointBalance = pointBalance;
    }

    public String getUsername() {
        return this.username;
    }

    public int getPointBalance() {
        return this.pointBalance;
    }
}
