package br.pjbl.minicrm.function.arquitetura;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Clean Architecture na Function: domain → application → infrastructure → functions
 * (os gatilhos são a camada de entrada e a raiz de composição).
 */
class ArquiteturaTest {

    private static final String BASE = "br.pjbl.minicrm.function";
    private static JavaClasses classes;

    @BeforeAll
    static void importar() {
        classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages(BASE);
    }

    @Test
    void camadas() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Domain").definedBy(BASE + ".domain..")
                .layer("Application").definedBy(BASE + ".application..")
                .layer("Infrastructure").definedBy(BASE + ".infrastructure..")
                .layer("Functions").definedBy(BASE + ".functions..")
                .whereLayer("Functions").mayNotBeAccessedByAnyLayer()
                .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Functions")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Functions")
                .check(classes);
    }

    @Test
    void domainEApplicationSaoJavaPuro() {
        classes().that().resideInAnyPackage(BASE + ".domain..", BASE + ".application..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(BASE + ".domain..", BASE + ".application..", "java..")
                .check(classes);
    }

    @Test
    void slicesNaoSeConhecem() {
        slices().matching(BASE + ".application.(*)..").should().notDependOnEachOther()
                .ignoreDependency(DescribedPredicate.alwaysTrue(), JavaClass.Predicates.resideInAPackage(BASE + ".application.common.."))
                .check(classes);
    }

    @Test
    void sdkDoAzureSoNosGatilhos() {
        classes().that().resideOutsideOfPackage(BASE + ".functions..")
                .should().onlyDependOnClassesThat().resideOutsideOfPackage("com.microsoft.azure.functions..")
                .check(classes);
    }
}
