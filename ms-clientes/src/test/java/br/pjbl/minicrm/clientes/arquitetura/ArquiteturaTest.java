package br.pjbl.minicrm.clientes.arquitetura;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de arquitetura (ArchUnit): falham se alguém quebrar as regras da
 * Clean Architecture ou acoplar duas vertical slices.
 */
class ArquiteturaTest {

    private static final String BASE = "br.pjbl.minicrm.clientes";
    private static JavaClasses classes;

    @BeforeAll
    static void importar() {
        classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages(BASE);
    }

    @Test
    void camadasRespeitamADirecaoDasDependencias() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Domain").definedBy(BASE + ".domain..")
                .layer("Application").definedBy(BASE + ".application..")
                .layer("Infrastructure").definedBy(BASE + ".infrastructure..")
                .layer("Api").definedBy(BASE + ".api..")
                .layer("Config").definedBy(BASE + ".config..")
                .whereLayer("Config").mayNotBeAccessedByAnyLayer()
                .whereLayer("Api").mayOnlyBeAccessedByLayers("Config")
                .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Config")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Api", "Config")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Api", "Config")
                .check(classes);
    }

    @Test
    void domainEhJavaPuro() {
        classes().that().resideInAPackage(BASE + ".domain..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(BASE + ".domain..", "java..")
                .check(classes);
    }

    @Test
    void applicationSoConheceDomainEJava() {
        classes().that().resideInAPackage(BASE + ".application..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(BASE + ".application..", BASE + ".domain..", "java..")
                .check(classes);
    }

    @Test
    void casosDeUsoNaoTemAnotacoesDoSpring() {
        noClasses().that().resideInAPackage(BASE + ".application..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .check(classes);
    }

    @Test
    void verticalSlicesNaoSeConhecem() {
        slices().matching(BASE + ".application.(*)..")
                .should().notDependOnEachOther()
                .ignoreDependency(com.tngtech.archunit.base.DescribedPredicate.alwaysTrue(),
                        com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage(BASE + ".application.common.."))
                .check(classes);
    }

    @Test
    void controllersFicamNaApiEChamamUmUnicoCasoDeUso() {
        classes().that().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage(BASE + ".api..")
                .andShould(dependerDeUmUnicoHandler())
                .check(classes);
    }

    @Test
    void cadaSliceTemUmHandler() {
        Set<String> slicesSemHandler = classes.stream()
                .filter(c -> c.getPackageName().startsWith(BASE + ".application.") && !c.getPackageName().endsWith(".common"))
                .collect(Collectors.groupingBy(JavaClass::getPackageName))
                .entrySet().stream()
                .filter(e -> e.getValue().stream().noneMatch(c -> c.getSimpleName().endsWith("Handler")))
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.toSet());

        assertThat(slicesSemHandler).isEmpty();
        assertThat(classes.stream().filter(c -> c.getSimpleName().endsWith("Handler")).count()).isGreaterThanOrEqualTo(7);
    }

    private static ArchCondition<JavaClass> dependerDeUmUnicoHandler() {
        return new ArchCondition<>("depender de exatamente um Handler") {
            @Override
            public void check(JavaClass controller, ConditionEvents events) {
                long handlers = controller.getDirectDependenciesFromSelf().stream()
                        .map(d -> d.getTargetClass())
                        .filter(c -> c.getSimpleName().endsWith("Handler"))
                        .map(JavaClass::getName)
                        .distinct()
                        .count();
                if (handlers != 1) {
                    events.add(SimpleConditionEvent.violated(controller, controller.getName() + " usa " + handlers + " handlers"));
                }
            }
        };
    }
}
