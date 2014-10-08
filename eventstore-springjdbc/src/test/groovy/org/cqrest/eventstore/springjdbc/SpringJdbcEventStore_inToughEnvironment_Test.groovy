package org.cqrest.eventstore.springjdbc

import org.junit.Test
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.PreparedStatementCreator
import org.springframework.jdbc.core.RowCallbackHandler
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.TransactionException
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate


class SpringJdbcEventStore_inToughEnvironment_Test {
    public static final DataAccessException DUMMY_DATA_ACCESS_EXCEPTION = new DummyDataAccessException()

    SpringJdbcEventStore eventStore
    org.cqrest.reactive.Observer observer


    @Test
    void observe__should_pass_data_access_exceptions_to_Observer() {
        JdbcOperations defectiveJdbcTemplate = [
          update: { String sql, Object... args -> },
          query: { PreparedStatementCreator psc, RowCallbackHandler rch -> throw DUMMY_DATA_ACCESS_EXCEPTION }
        ] as JdbcOperations
        eventStore = new SpringJdbcEventStore(
          defectiveJdbcTemplate,
          new NamedParameterJdbcTemplate(defectiveJdbcTemplate),
          new StubTransactionTemplate(),
          SpringJdbcEventStore.defaultEventBus(),
          SpringJdbcEventStore.DONT_CREATE_TABLE
        )
        observer = mock(org.cqrest.reactive.Observer)

        eventStore.observe().subscribe(observer)

        verify(observer).onError(SpringJdbcEventStore.createQueryException(DUMMY_DATA_ACCESS_EXCEPTION))
    }

}


class StubTransactionTemplate extends TransactionTemplate {

    @Override
    def <T> T execute(TransactionCallback<T> action) throws TransactionException {
        action.doInTransaction(null)
    }

}

class DummyDataAccessException extends DataAccessException {

    DummyDataAccessException() {
        super('DUMMY EXCEPTION THROWN IN SpringJdbcEventStore_inToughEnvironment_Test')
    }

}
