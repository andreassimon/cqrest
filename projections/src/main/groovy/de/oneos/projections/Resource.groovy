package de.oneos.projections

class Resource<T> {
    UUID correlationId
    UUID aggregateId
    int version
    Date lastModified

    T body

    public <R> Resource<R> transform(Closure<R> function) {
        return new Resource<R>(
            correlationId: correlationId,
            aggregateId: aggregateId,
            version: version,
            lastModified: lastModified,
            body: function(body)
        )
    }

    public <R> Resource<R> updateVersion(int sequenceNumber) {
        version = [version, sequenceNumber].max()
        return this
    }

    public <R> Resource<R> updateLastModified(Date timestamp) {
        lastModified = [lastModified, timestamp].max()
        return this
    }

    public <R> Resource<R> updateCorrelationId(UUID correlationId) {
        this.correlationId = correlationId
        return this
    }

}
