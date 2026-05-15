package com.example.nexusa.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.nexusa")
public class NexusAArchitectureTest {

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories_directly =
            noClasses().that().areAnnotatedWith(RestController.class)
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
                    .because("Controllers must delegate to Services to maintain transactional boundaries and prevent data leaks");

    @ArchTest
    static final ArchRule services_should_be_transactional =
            classes().that().areAnnotatedWith(Service.class)
                    .should().beAnnotatedWith(Transactional.class)
                    .orShould().haveModifier(com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT)
                    .because("All service operations mutating the historical record/graphs must be highly transactional");

    @ArchTest
    static final ArchRule canonical_layer_must_not_depend_on_research_layer =
            noClasses().that().resideInAPackage("..CanonicalKnowledge..")
                    .should().dependOnClassesThat().resideInAPackage("..ResearchSubmissions..")
                    .because("The Canonical Data Model must remain isolated and purely abstracted from raw, unverified claims.");

    @ArchTest
    static final ArchRule testing_layer_independent =
            noClasses().that().resideInAPackage("..nexusa..")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .and().haveSimpleNameNotEndingWith("Tests")
                    .and().haveSimpleNameNotEndingWith("IT")
                    .should().dependOnClassesThat().resideInAPackage("org.junit..")
                    .because("Production code should never depend on testing libraries.");
}
