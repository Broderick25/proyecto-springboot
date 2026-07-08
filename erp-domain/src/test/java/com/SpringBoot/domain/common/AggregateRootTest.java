package com.SpringBoot.domain.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AggregateRootTest {

    record DummyEvent(String data) implements DomainEvent {}

    static class DummyAggregate extends AggregateRoot<String> {
        private final String id;

        DummyAggregate(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        void doSomething() {
            registerEvent(new DummyEvent("algo pasó"));
        }
    }

    @Test
    void registerEvent_acumulaEventosInternos() {
        DummyAggregate aggregate = new DummyAggregate("1");

        aggregate.doSomething();
        aggregate.doSomething();

        assertThat(aggregate.getDomainEvents()).hasSize(2);
    }

    @Test
    void clearDomainEvents_vaciaLaLista() {
        DummyAggregate aggregate = new DummyAggregate("1");
        aggregate.doSomething();

        aggregate.clearDomainEvents();

        assertThat(aggregate.getDomainEvents()).isEmpty();
    }
}
