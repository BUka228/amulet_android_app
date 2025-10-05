package com.example.amulet.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.isPublic

class AmuletRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "amulet"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            ViewModelDependsOnRepositoryRule(config),
            UseCaseNamingRule(config),
            FeatureDependsOnDataRule(config),
            DomainLayerAndroidDependencyRule(config),
            RepositoryNamingRule(config),
            StateFlowExposureRule(config),
            ErrorHandlingRule(config),
            KmpCompatibilityRule(config),
            PatternElementNamingRule(config),
            NavigationContractRule(config)
        )
    )
}

private const val PACKAGE_FEATURE = ".feature."
private const val PACKAGE_DATA = "com.example.amulet.data"
private const val PACKAGE_SHARED = "com.example.amulet.shared"

abstract class BaseAmuletRule(
    config: Config,
    ruleName: String,
    description: String,
    debt: Debt = Debt.TWENTY_MINS
) : Rule(config) {
    final override val issue: Issue = Issue(
        id = ruleName,
        severity = Severity.Maintainability,
        description = description,
        debt = debt
    )
}

class ViewModelDependsOnRepositoryRule(config: Config) : BaseAmuletRule(
    config,
    "ViewModelDependsOnRepositoryRule",
    "ViewModel classes must depend on abstractions (UseCase) instead of repository implementations."
) {
    override fun visitClass(klass: KtClass) {
        if (klass.name?.endsWith("ViewModel") != true) return
        val imports = klass.containingKtFile.importDirectives.mapNotNull { it.importPath?.pathStr }
        if (imports.any { it.contains(PACKAGE_DATA) || it.endsWith("RepositoryImpl") }) {
            emit(Entity.atName(klass), "ViewModel ${klass.name} imports data-layer implementation directly")
        }
        val parameters = klass.primaryConstructorParameters + klass.secondaryConstructors.flatMap { it.valueParameters }
        if (parameters.any { it.typeReference?.text?.contains("RepositoryImpl") == true || it.typeReference?.text?.contains(".data.") == true }) {
            emit(Entity.atName(klass), "ViewModel ${klass.name} depends on repository implementation")
        }
        super.visitClass(klass)
    }
}

class UseCaseNamingRule(config: Config) : BaseAmuletRule(
    config,
    "UseCaseNamingRule",
    "Use case classes and interfaces inside the shared domain layer must end with UseCase."
) {
    override fun visitClass(klass: KtClass) {
        val packageName = klass.containingKtFile.packageFqName.asString()
        if (packageName.contains(".shared.") && packageName.contains(".domain.")) {
            val name = klass.name ?: return
            if (!name.endsWith("UseCase") && !name.endsWith("Repository") && !name.endsWith("Mapper")) {
                emit(Entity.atName(klass), "Class $name inside shared domain layer must end with UseCase")
            }
        }
        super.visitClass(klass)
    }
}

class FeatureDependsOnDataRule(config: Config) : BaseAmuletRule(
    config,
    "FeatureDependsOnDataRule",
    "Feature layer must not import data layer implementations."
) {
    override fun visitKtFile(file: KtFile) {
        val packageName = file.packageFqName.asString()
        if (!packageName.contains(PACKAGE_FEATURE)) {
            super.visitKtFile(file)
            return
        }
        file.importDirectives.mapNotNull { it.importPath?.pathStr }.forEach { importPath ->
            if (importPath.startsWith(PACKAGE_DATA)) {
                emit(Entity.from(file), "Feature layer file imports data layer type: $importPath")
            }
        }
        super.visitKtFile(file)
    }
}

class DomainLayerAndroidDependencyRule(config: Config) : BaseAmuletRule(
    config,
    "DomainLayerAndroidDependencyRule",
    "Shared module must not depend on Android platform packages."
) {
    override fun visitKtFile(file: KtFile) {
        val packageName = file.packageFqName.asString()
        if (!packageName.startsWith(PACKAGE_SHARED)) {
            super.visitKtFile(file)
            return
        }
        file.importDirectives.mapNotNull { it.importPath?.pathStr }.forEach { importPath ->
            if (importPath.startsWith("android") || importPath.startsWith("androidx")) {
                emit(Entity.from(file), "Shared module file imports Android API: $importPath")
            }
        }
        super.visitKtFile(file)
    }
}

class RepositoryNamingRule(config: Config) : BaseAmuletRule(
    config,
    "RepositoryNamingRule",
    "Repository contracts must end with Repository, implementations with RepositoryImpl."
) {
    override fun visitClass(klass: KtClass) {
        val packageName = klass.containingKtFile.packageFqName.asString()
        val name = klass.name ?: return
        if (packageName.contains(".shared.") && packageName.contains(".domain.")) {
            if (klass.isInterface() && !name.endsWith("Repository")) {
                emit(Entity.atName(klass), "Repository interface $name must end with Repository")
            }
        }
        if (packageName.contains(".data.")) {
            if (!name.endsWith("RepositoryImpl")) {
                emit(Entity.atName(klass), "Repository implementation $name must end with RepositoryImpl")
            }
        }
        super.visitClass(klass)
    }
}

class StateFlowExposureRule(config: Config) : BaseAmuletRule(
    config,
    "StateFlowExposureRule",
    "MutableStateFlow must not be exposed directly from ViewModel."
) {
    override fun visitProperty(property: KtProperty) {
        if (property.isPublic && property.typeReference?.text?.contains("MutableStateFlow") == true) {
            emit(Entity.from(property), "Property ${property.name} exposes MutableStateFlow; expose StateFlow instead")
        }
        super.visitProperty(property)
    }
}

class ErrorHandlingRule(config: Config) : BaseAmuletRule(
    config,
    "ErrorHandlingRule",
    "Repository APIs must return Result or Flow with typed errors."
) {
    override fun visitNamedFunction(function: KtNamedFunction) {
        val containingClass = function.containingClassOrObject as? KtClass ?: return
        val name = containingClass.name ?: return
        if (!name.endsWith("Repository") && !name.endsWith("RepositoryImpl")) return
        val returnType = function.typeReference?.text?.replace("\n", "") ?: return
        val allowed = returnType.contains("Result<") || returnType.contains("Flow<")
        if (!allowed) {
            emit(Entity.atName(function), "Repository function ${function.name} must return Result or Flow")
        }
        super.visitNamedFunction(function)
    }
}

class KmpCompatibilityRule(config: Config) : BaseAmuletRule(
    config,
    "KmpCompatibilityRule",
    "Shared module must only use multiplatform-friendly packages."
) {
    override fun visitKtFile(file: KtFile) {
        val packageName = file.packageFqName.asString()
        if (!packageName.startsWith(PACKAGE_SHARED)) {
            super.visitKtFile(file)
            return
        }
        file.importDirectives.mapNotNull { it.importPath?.pathStr }.forEach { importPath ->
            if (importPath.startsWith("java.awt") || importPath.startsWith("javafx")) {
                emit(Entity.from(file), "Shared module imports JVM-only API: $importPath")
            }
        }
        super.visitKtFile(file)
    }
}

class PatternElementNamingRule(config: Config) : BaseAmuletRule(
    config,
    "PatternElementNamingRule",
    "Pattern elements should follow naming convention for clarity."
) {
    override fun visitClass(klass: KtClass) {
        val packageName = klass.containingKtFile.packageFqName.asString()
        if (packageName.contains(".pattern")) {
            val name = klass.name ?: return
            if (!name.startsWith("Pattern")) {
                emit(Entity.atName(klass), "Class $name inside pattern package should start with Pattern")
            }
        }
        super.visitClass(klass)
    }
}

class NavigationContractRule(config: Config) : BaseAmuletRule(
    config,
    "NavigationContractRule",
    "Navigation should use contract objects instead of raw route strings."
) {
    override fun visitNamedFunction(function: KtNamedFunction) {
        val bodyText = function.bodyExpression?.text ?: return
        if (bodyText.contains("navigate(\"")) {
            emit(Entity.atName(function), "Navigation call should use contract object instead of raw string route")
        }
        super.visitNamedFunction(function)
    }
}

private fun Rule.emit(entity: Entity, message: String) {
    report(CodeSmell(issue, entity, message))
}
