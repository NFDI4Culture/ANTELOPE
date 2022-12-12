package org.tib.osl.annotationservice;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.belongToAnyOf;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packagesOf = AnnotationServiceApp.class, importOptions = DoNotIncludeTests.class)
class TechnicalStructureTest {

    // prettier-ignore
    @ArchTest
    static final ArchRule respectsTechnicalArchitectureLayers = layeredArchitecture()
        .layer("Config").definedBy("..config..")
        .layer("Web").definedBy("..web..")
        .optionalLayer("Service").definedBy("..service..")
        .layer("Security").definedBy("..security..")
        .layer("Persistence").definedBy("..repository..")
        .layer("Domain").definedBy("..domain..")

        .whereLayer("Config").mayNotBeAccessedByAnyLayer()
        // we made "web" accessible from underlying layer "service" because the api-first (openAPI) generator of jhipster
        // generates a interface to implement for the service backend that is in a subpackage of the annotationservice.service package but is named "web"
        .whereLayer("Web").mayOnlyBeAccessedByLayers("Config", "Service")
        .whereLayer("Service").mayOnlyBeAccessedByLayers("Web", "Config", "Service")
        .whereLayer("Security").mayOnlyBeAccessedByLayers("Config", "Service", "Web")
        .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service", "Security", "Web", "Config")
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Persistence", "Service", "Security", "Web", "Config")

        .ignoreDependency(belongToAnyOf(AnnotationServiceApp.class), alwaysTrue())
        .ignoreDependency(alwaysTrue(), belongToAnyOf(
            org.tib.osl.annotationservice.config.Constants.class,
            org.tib.osl.annotationservice.config.ApplicationProperties.class,
            org.tib.osl.annotationservice.service.AnnotationService.class
            
        ))
        ;
}
