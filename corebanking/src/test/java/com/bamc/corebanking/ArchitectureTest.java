package com.bamc.corebanking;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.bamc.corebanking", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	// allowEmptyShould: the domain package does not exist yet; remove once it has classes
	@ArchTest
	static final ArchRule domain_has_no_framework_dependency = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"org.springframework..", "jakarta.persistence..", "org.apache.kafka..")
			.allowEmptyShould(true)
			.as("Domain rules broken in files (domain must not import Spring, JPA or Kafka)");
}
