package network.warzone.scaffold.utils.snap;

public class SnapException extends RuntimeException {
    public SnapException(String msg) {
        super(msg);
    }

    public SnapException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
