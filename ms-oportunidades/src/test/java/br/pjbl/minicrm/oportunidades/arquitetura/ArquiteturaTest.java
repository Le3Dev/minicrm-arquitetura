package br.pjbl.minicrm.oportunidades.arquitetura;

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

/** Mesmas regras do MS Clientes: o B ganha a verificação de graça ao criar novas slices. */
class ArquiteturaTest {

    private static final String BASE = "br.pjbl.minicrm.oportunidades";
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
                .layer("Api").definedBy(BASE + ".api..")
                .layer("Config").definedBy(BASE + ".config..")
                .whereLayer("Config").mayNotBeAccessedByAnyLayer()
                .whereLayer("Api").mayOnlyBeAccessedByLayers("Config")
                .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Config")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Api", "Config")
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
}
