/**
 * Created by igleson on 05/10/14.
 */
class Rejected implements PromiseStatus {
    Throwable reason;

    public Rejected(Throwable reason) {
        this.reason = reason;
    }

    @Override
    public void resolve(Object solution) {
        throw new IllegalStateException("Promise is already rejected");
    }

    @Override
    public void error(Throwable t) {
        throw new IllegalStateException("Promise is already rejected");
    }

    @Override
    public boolean isFufilled() {
        return false;
    }

    @Override
    public boolean isPending() {
        return false;
    }

    @Override
    public boolean isRejected() {
        return true;
    }
}
