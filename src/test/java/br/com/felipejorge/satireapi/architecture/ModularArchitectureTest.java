package br.com.felipejorge.satireapi.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.felipejorge.satireapi.SatireApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularArchitectureTest {

    @Test
    void modularBoundariesAreValid() {
        var modules = ApplicationModules.of(SatireApiApplication.class);

        assertEquals(7, modules.stream().count());
        modules.verify();
    }
}
