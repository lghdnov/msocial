package lghdnov.msocial.common.exceptions;

public class AccessDeniedException extends BaseException {
    public AccessDeniedException(String code, String message) {
        super(code, message);
    }
}
