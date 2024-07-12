package org.acme.vehiclerouting;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(VehicleRoutingSpringBootApp.MyRuntimeHints.class)
public class VehicleRoutingSpringBootApp {

    public static void main(String[] args) {
        SpringApplication.run(VehicleRoutingSpringBootApp.class, args);
    }

    static class MyRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register serialization
            hints.reflection().registerType(TypeReference.of("org.acme.vehiclerouting.domain.Visit"),
                    builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
            hints.reflection().registerType(TypeReference.of("ai.timefold.solver.core.impl.domain.common.accessor.ReflectionMethodMemberAccessor"),
                    builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
        }
    }
}
