package de.oneos.projections


interface Observer<T> {

    /**
     * Notifies the Observer that the {@link Observable} has finished sending push-based notifications.
     * <p>
     * The {@link Observable} will not call this closure if it calls <code>onError</code>.
     */
    void onCompleted()

    /**
     * Notifies the Observer that the {@link Observable} has experienced an error condition.
     * <p>
     * If the {@link Observable} calls this closure, it will not thereafter call <code>onNext</code> or <code>onCompleted</code>.
     *
     * @param e
     */
    void onError(Throwable e)

    /**
     * Provides the Observer with new data.
     * <p>
     * The {@link Observable} calls this closure 1 or more times, unless it calls <code>onError</code> in which case this closure may never be called.
     * <p>
     * The {@link Observable} will not call this closure again after it calls either <code>onCompleted</code> or <code>onError</code>.
     *
     * @param args
     */
    void onNext(T args)

}
