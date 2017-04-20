package vishal.chetan.splash;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import vishal.chetan.splash.asyncs.AsyncHelper;

public class FieldValidator {
    public interface ErrorProvider {
        void setErrorUsername(@StringRes int resId);
        void setErrorEmail(@StringRes int resId);
        void setErrorPassword(@StringRes int resId);
        void setErrorSnack(@StringRes int resId);
    }

    public final ErrorProvider errorProvider;
    private final int serverIndex;
    private String username;
    private String email;
    private String password;

    public FieldValidator(int serverIndex, ErrorProvider errorProvider) {
        this.errorProvider = errorProvider;
        this.serverIndex = serverIndex;
    }

    public void validateUsername(@NonNull final String username) {
        if (this.username == null || !(username.equals(this.username))) {
            if (username.isEmpty()) {
                errorProvider.setErrorUsername(R.string.errEmpty);
                return;
            }

            GlobalFunctions.setRegNameStatus(GlobalFunctions.HTTP_CODE.BUSY);
            new CheckAvailable(serverIndex, "check/" + username).execute();
            this.username = username;
        }
    }

    public void validateEmail(@NonNull final String email) {
        if (this.email == null || !email.equals(this.email)) {
            if (email.isEmpty()) {
                errorProvider.setErrorEmail(R.string.errEmpty);
                return;
            }

            final Pattern emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
            if (!(emailPattern.matcher(email).find())) {
                errorProvider.setErrorEmail(R.string.errInvalidEmail);
                return;
            }

            GlobalFunctions.setRegNameStatus(GlobalFunctions.HTTP_CODE.BUSY);
            new CheckAvailable(serverIndex, "check/" + email).execute();
            this.email = email;
        }
    }

    public void validatePassword(@NonNull final String password) {
        if (this.password == null || !password.equals(this.password)) {
            if (password.isEmpty()) {
                errorProvider.setErrorPassword(R.string.errEmpty);
                return;
            }

            if (password.length() < 8) {
                errorProvider.setErrorPassword(R.string.errShortPass);
                return;
            }
            this.password = password;
        }
    }


    private class CheckAvailable extends AsyncHelper {
        @NonNull
        private Boolean checkForUser = true;

        CheckAvailable(int serverIndex, @NonNull String pageUrl) {
            super(serverIndex, pageUrl);
            if (pageUrl.contains("@")) {
                checkForUser = false;
            }
        }

        @Override
        protected void onPostExecute(@Nullable JSONObject jsonObject) {
            GlobalFunctions.HTTP_CODE status = GlobalFunctions.HTTP_CODE.UNKNOWN;
            if (jsonObject != null) {
                try {
                    Boolean isAvailable;
                    if (checkForUser) {
                        isAvailable = jsonObject.getBoolean("user");
                    } else {
                        isAvailable = jsonObject.getBoolean("email");
                    }


                    if (isAvailable) {
                        status = GlobalFunctions.HTTP_CODE.SUCCESS;
                    } else {
                        status = GlobalFunctions.HTTP_CODE.FAILED;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (checkForUser) {
                        status = GlobalFunctions.HTTP_CODE.REQUEST_FAILED;
                    }
                }
            } else {
                status = GlobalFunctions.HTTP_CODE.NO_ACCESS;
            }
            checkStatus(checkForUser, status);
        }
    }


    private void checkStatus(boolean checkForUser, @NonNull GlobalFunctions.HTTP_CODE code) {
        if (checkForUser) {
            GlobalFunctions.setRegNameStatus(code);
        } else {
            GlobalFunctions.setRegEmailStatus(code);
        }
        switch (code) {
            case SUCCESS:
                return;
            case FAILED:
                if (checkForUser) {
                    errorProvider.setErrorUsername(R.string.errNameUsed);
                } else {
                    errorProvider.setErrorEmail(R.string.errEmailUsed);
                }
                break;
            case NO_ACCESS:
                errorProvider.setErrorSnack(R.string.errNoAccess);
                break;
            case REQUEST_FAILED:
                errorProvider.setErrorSnack(R.string.errConFailed);
                break;
            case UNKNOWN:
                errorProvider.setErrorSnack(R.string.errUnknown);
        }
    }

}
