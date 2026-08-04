package br.com.api.satireapi.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import br.com.api.satireapi.SatireApiApplication;

class ModularArchitectureTest {

    @Test
    void modularBoundariesAreValid() {
        var modules = ApplicationModules.of(SatireApiApplication.class);

        assertEquals(7, modules.stream().count());
        modules.verify();
    }
}
