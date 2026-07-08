package com.SpringBoot.domain.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityTest {

    // Subclase mínima de prueba
    static class DummyEntity extends Entity<String> {
        private final String id;
        private String otherField;

        DummyEntity(String id, String otherField) {
            this.id = id;
            this.otherField = otherField;
        }

        @Override
        public String getId() {
            return id;
        }
    }

    @Test
    void equals_esBasadoUnicamenteEnId() {
        DummyEntity a = new DummyEntity("1", "campoA");
        DummyEntity b = new DummyEntity("1", "campoDistinto");

        assertThat(a).isEqualTo(b); // mismo id, campos distintos -> iguales
    }

    @Test
    void equals_esFalsoConIdsDistintos() {
        DummyEntity a = new DummyEntity("1", "x");
        DummyEntity b = new DummyEntity("2", "x");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void hashCode_esConsistenteConId() {
        DummyEntity a = new DummyEntity("1", "x");
        DummyEntity b = new DummyEntity("1", "y");

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
