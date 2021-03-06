package com.zpf.download;

import static com.zpf.download.Downloads.Impl.STATUS_UNHANDLED_HTTP_CODE;
import static com.zpf.download.Downloads.Impl.STATUS_UNHANDLED_REDIRECT;

/**
 * Raised to indicate that the current request should be stopped immediately.
 *
 * Note the message passed to this exception will be logged and therefore must be guaranteed
 * not to contain any PII, meaning it generally can't include any information about the request
 * URI, headers, or destination filename.
 */
class StopRequestException extends Exception {
    private final int mFinalStatus;

    public StopRequestException(int finalStatus, String message) {
        super(message);
        mFinalStatus = finalStatus;
    }

    public StopRequestException(int finalStatus, Throwable t) {
        this(finalStatus, t.getMessage());
        initCause(t);
    }

    public StopRequestException(int finalStatus, String message, Throwable t) {
        this(finalStatus, message);
        initCause(t);
    }

    public int getFinalStatus() {
        return mFinalStatus;
    }

    public static StopRequestException throwUnhandledHttpError(int code, String message)
            throws StopRequestException {
        final String error = "Unhandled HTTP response: " + code + " " + message;
        if (code >= 400 && code < 600) {
            throw new StopRequestException(code, error);
        } else if (code >= 300 && code < 400) {
            throw new StopRequestException(STATUS_UNHANDLED_REDIRECT, error);
        } else {
            throw new StopRequestException(STATUS_UNHANDLED_HTTP_CODE, error);
        }
    }
}