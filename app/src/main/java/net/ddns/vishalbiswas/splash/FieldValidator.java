package net.ddns.vishalbiswas.splash;

import java.util.regex.Pattern;

public class FieldValidator {
    public int validateUsername(final String username) {
        if (username.isEmpty()) {
            return R.string.errEmpty;
        }

        return -1;
    }

    public int validateEmail(final String email) {
        if (email.isEmpty()) {
            return R.string.errEmpty;
        }

        final Pattern emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        if (!(emailPattern.matcher(email).find())) {
            return R.string.errInvalidEmail;
        }

        return -1;
    }

    public int validatePassword(final String password) {
        if (password.isEmpty()) {
            return R.string.errEmpty;
        }

        if (password.length() < 8) {
            return R.string.errShortPass;
        }

        return -1;
    }
}
