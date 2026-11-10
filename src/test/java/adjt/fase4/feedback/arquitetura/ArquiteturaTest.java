package adjt.fase4.feedback.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Trava a regra de dependência da arquitetura: as setas apontam para dentro
 * (handler e infraestrutura → aplicação → domínio) e só as bordas conhecem a AWS.
 */
@AnalyzeClasses(packages = "adjt.fase4.feedback", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

  private static final String DOMINIO = "adjt.fase4.feedback.domain..";
  private static final String APLICACAO = "adjt.fase4.feedback.application..";
  private static final String INFRAESTRUTURA = "adjt.fase4.feedback.infrastructure..";
  private static final String HANDLER = "adjt.fase4.feedback.handler..";

  @ArchTest
  static final ArchRule dominioSoDependeDeSiMesmoEDoJava = classes()
      .that().resideInAPackage(DOMINIO)
      .should().onlyDependOnClassesThat().resideInAnyPackage(DOMINIO, "java..");

  @ArchTest
  static final ArchRule aplicacaoSoDependeDoDominioEDoJava = classes()
      .that().resideInAPackage(APLICACAO)
      .should().onlyDependOnClassesThat().resideInAnyPackage(APLICACAO, DOMINIO, "java..");

  @ArchTest
  static final ArchRule infraestruturaNaoConheceOsHandlers = noClasses()
      .that().resideInAPackage(INFRAESTRUTURA)
      .should().dependOnClassesThat().resideInAPackage(HANDLER);

  @ArchTest
  static final ArchRule apenasAsBordasConhecemAAws = noClasses()
      .that().resideInAnyPackage(DOMINIO, APLICACAO)
      .should().dependOnClassesThat().resideInAnyPackage("software.amazon..", "com.amazonaws..");

  @ArchTest
  static final ArchRule adaptadoresDeSaidaImplementamPortasDaAplicacao = classes()
      .that().resideInAPackage(INFRAESTRUTURA).and().haveSimpleNameStartingWith("Dynamo")
      .or().haveSimpleNameStartingWith("Sns").or().haveSimpleNameStartingWith("Ses")
      .should().dependOnClassesThat().resideInAPackage("adjt.fase4.feedback.application.port..");
}
