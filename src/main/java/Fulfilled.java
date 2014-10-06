class Fulfilled<A> implements PromiseStatus<A> {

    A solution;

    public Fulfilled(A solution) {
        this.solution = solution;
    }

    public void resolve(A solution) {
        throw new IllegalStateException("Promise is already fulfilled");
    }

    @Override
    public void error(Throwable t) {
        throw new IllegalStateException("Promise is already fulfilled");
    }

    @Override
    public boolean isFufilled() {
        return true;
    }

    @Override
    public boolean isPending() {
        return false;
    }

    @Override
    public boolean isRejected() {
        return false;
    }
}
