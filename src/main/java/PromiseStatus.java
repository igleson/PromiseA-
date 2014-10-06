interface PromiseStatus<A> {
    void resolve(A solution);

    void error(Throwable t);

    boolean isFufilled();
    boolean isPending();
    boolean isRejected();
}
