class Pending<A> implements PromiseStatus<A> {

    private Promise<A> promise;

    Pending(Promise<A> promise) {
        this.promise = promise;
    }

    @Override
    public void resolve(A solution) {
        promise.setStatus(new Fulfilled<A>(solution));
    }

    @Override
    public void error(Throwable t) {
        promise.setStatus(new Rejected(t));
    }

    @Override
    public boolean isFufilled() {
        return false;
    }

    @Override
    public boolean isPending() {
        return true;
    }

    @Override
    public boolean isRejected() {
        return false;
    }
}
