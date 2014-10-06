import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A promise of a value.
 *
 * @param <A> The type of the value for this promise.
 */
public class Promise<A> {

    private PromiseStatus<A> status;

    private List<Promise> successCallbacks = new ArrayList<>();
    private List<Function> successFuns = new ArrayList<>();

    private List<Promise> errorCallbacks = new LinkedList<>();
    private List<Function> errorFuns = new LinkedList<>();
    private Thread thread;

    /**
     * Create a new Promise based on a function that will be executed asynchronously
     *
     * @param fun Function to be execute by this promise.
     */
    public Promise(Callable<A> fun) {
        this();
        setCall(fun);
    }

    /**
     * Register callbacks to error or success for this promise, and return a Promise that executes when this Promise
     * get rejected or resolved.
     *
     * @param funSucces Function to receive the result from this Promise and be used to create the Promise to be
     *                  returned.
     * @param funError  Function to receive the reason form this Promise be rejected and be used to create the Promise
     *                  to be returned.
     * @param <B>       Type of the Promise to be returned.
     * @return A Promise that will be executed when this Promise is resolved or rejected.
     */
    public <B> Promise<B> then(Function<A, B> funSucces, Function<? super Throwable, B> funError) {
        Promise<B> callback = new Promise<>();
        success(funSucces, callback);
        error(funError, callback);
        return callback;
    }

    /**
     * Register a error callback for this promise and return a promise that will be fulfilled when this promise be
     * rejected
     *
     * @param fun Function to map the reason for this promise to be rejected to the result of the returned promise.
     * @param <B> Type of the promise to be returned.
     * @return A promise that will be fulfilled when this promise be rejected.
     */
    public <B> Promise<B> error(Function<? super Throwable, B> fun) {
        Promise<B> callback = new Promise<>();
        return error(fun, callback);
    }

    /**
     * Register a error callback for this promise and return a promise that will be fulfilled when this promise be
     * rejected
     *
     * @param consumer Consumer to consume the reason for this promise to be rejected.
     * @return A promise that will be fulfilled when this promise be rejected.
     */
    public Promise<Throwable> error(Consumer<? super Throwable> consumer) {
        Promise<Throwable> callback = new Promise<>();
        return error((Throwable a) -> {
            consumer.accept(a);
            return a;
        }, callback);
    }

    /**
     * Register a success callback for this promise and return a promise that will be fulfilled when this promise be
     * fulfilled
     *
     * @param fun Function to map the result for this promise to the result of the returned promise.
     * @param <B> Type of the promise to be returned.
     * @return A promise that will be fulfilled when this promise be fulfilled.
     */
    public <B> Promise<B> success(Function<A, B> fun) {
        Promise<B> callback = new Promise<>();
        return success(fun, callback);
    }

    /**
     * Register a success callback for this promise and return a promise that will be fulfilled when this promise be
     * fulfilled
     *
     * @param consumer Consumer to consume the result for this promise.
     * @return A promise that will be fulfilled when this promise be fulfilled.
     */
    public Promise<A> success(Consumer<A> consumer) {
        Promise<A> callback = new Promise<>();
        return success((A a) -> {
            consumer.accept(a);
            return a;
        }, callback);
    }

    /**
     * Gets the value from this promise if it is fulfilled, otherwise returns the other value.
     *
     * @param other The default value for the promise if it is not fulfiled.
     * @return The value from this promise, or the other value is it is not fulfilled.
     */
    public A getOrElse(A other) {
        if (status.isFufilled()) {
            return ((Fulfilled<A>) status).solution;
        }
        return other;
    }

    /**
     * Gets the value from this promise. ATTEMP TO THE FACT THAT THIS METHOD IS SYNCHRONOUS.
     *
     * @return the value from this promise.
     * @throws java.lang.RuntimeException if this promisse is rejected.
     */
    public A get() {
        while (status.isPending()) {
        }
        if (status.isRejected()) {
            throw new RuntimeException(((Rejected) status).reason);
        }
        return ((Fulfilled<A>) status).solution;
    }

    /**
     * Creates a new promise already fulfilled with the value in solution.
     *
     * @param solution The solution for the created promise.
     * @param <T>      The type of the created promise.
     * @return A promise already resolved.
     */
    public static <T> Promise<T> resolved(T solution) {
        return new Promise<>(solution);
    }

    /**
     * Creates a new promise already rejected with the value in reason.
     *
     * @param reason The reason to the returned promise be rejected.
     * @return A promise already rejected.
     */
    public static Promise rejected(Throwable reason) {
        return new Promise<>(reason);
    }

    /**
     * Creates a new promise already fulfilled with the value in solution.
     *
     * @param solution The solution for this promise.
     */
    private Promise(A solution) {
        this();
        this.resolve(solution);
    }

    /**
     * Creates a new promise already rejected with the value in reason.
     *
     * @param reason The reason to this promise be rejected.
     */
    private Promise(Throwable reason) {
        this();
        this.reject(reason);
    }

    /**
     * Creates a pending promise.
     */
    private Promise() {
        setStatus(new Pending<>(this));
    }

    /**
     * Register a success callback for this promise and return a promise that will be fulfilled when this promise be
     * fulfilled
     *
     * @param fun      Function to map the result for this promise to the result of the returned promise.
     * @param callback The callback to be registered.
     * @param <B>      Type of the promise to be returned.
     * @return A promise that will be fulfilled when this promise be fulfilled.
     */
    private <B> Promise<B> success(Function<A, B> fun, Promise<B> callback) {
        successCallbacks.add(callback);
        successFuns.add(fun);
        if (status.isFufilled()) {
            A result = ((Fulfilled<A>) status).solution;
            callback.setCall(() -> {
                return fun.apply(result);
            });
        } else if (status.isRejected()) {
            Throwable reason = ((Rejected) status).reason;
            callback.reject(reason);
        }

        return callback;
    }

    /**
     * Register a error callback for this promise and return a promise that will be fulfilled when this promise be
     * rejected
     *
     * @param fun      Function to map the reason for this promise to be rejected to the result of the returned promise.
     * @param callback The callback to be registered.
     * @param <B>      Type of the promise to be returned.
     * @return A promise that will be fulfilled when this promise be rejected.
     */
    private <B> Promise<B> error(Function<? super Throwable, B> fun, Promise<B> callback) {
        errorCallbacks.add(callback);
        errorFuns.add(fun);
        if (status.isRejected()) {
            Throwable reason = ((Rejected) status).reason;
            callback.setCall(() -> {
                return fun.apply(reason);
            });
        }
        return callback;
    }

    /**
     * Resolve this promise.
     *
     * @param solution The solution for this promise.
     */
    private void resolve(A solution) {
        status.resolve(solution);
        Iterator<Promise> succesIt = successCallbacks.iterator();
        Iterator<Function> funIt = successFuns.iterator();
        while (succesIt.hasNext()) {
            succesIt.next().setCall(() -> {
                return funIt.next().apply(solution);
            });
        }
    }

    /**
     * Reject this promise.
     *
     * @param reason The reason for this promise be rejected.
     * @param <T>    The exception type for the reason.
     */
    private <T extends Throwable> void reject(T reason) {
        status.error(reason);
        successCallbacks.forEach((Promise p) -> {
            p.reject(reason);
        });
        Iterator<Promise> errorsIt = errorCallbacks.iterator();
        Iterator<Function> funsIt = errorFuns.iterator();
        while (errorsIt.hasNext()) {
            errorsIt.next().setCall(() -> {
                return funsIt.next().apply(reason);
            });
        }
    }

    /**
     * Set the status of this promise.
     *
     * @param status The new status for this promise.
     */
    final void setStatus(PromiseStatus<A> status) {
        this.status = status;
    }

    /**
     * Set the function to be executed by this promise.
     *
     * @param fun The function to be executed by this promise.
     */
    private void setCall(Callable<A> fun) {
        this.thread = new Thread(() -> {
            try {
                A solution = fun.call();
                this.resolve(solution);
            } catch (Exception e) {
                this.reject(e);
            }
        });
        thread.start();
    }
}