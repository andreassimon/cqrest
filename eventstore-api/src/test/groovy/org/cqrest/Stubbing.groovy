package org.cqrest

import org.mockito.stubbing.*
import org.mockito.invocation.*


class Stubbing {

    static answer(Closure answerClosure) {
        return new Answer() {
            @Override
            Object answer(InvocationOnMock invocation) {
                return answerClosure.call(invocation)
            }
        }
    }

}
