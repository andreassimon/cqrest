package de.oneos.projections


class ObserverAdapter<T> implements rx.Observer<T> {

    final Observer<T> observer

    ObserverAdapter(Observer<T> observer) {
        this.observer = observer
    }


    @Override
    void onCompleted() {
        observer.onCompleted()
    }

    @Override
    void onError(Throwable e) {
        observer.onError(e)
    }

    @Override
    void onNext(T args) {
        observer.onNext(args)
    }

}
