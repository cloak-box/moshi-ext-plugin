package com.black.cat.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class UniversalSymbolProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    return UniversalSymbolProcessor(environment)
  }
}

private class UniversalSymbolProcessor(
  val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
  companion object {
    private val JSON_CLASS_NAME = JsonClass::class.qualifiedName!!
    private val ADAPTER_CLASS_NAME = ClassName.bestGuess(JsonAdapter.Factory::class.qualifiedName!!)
    private val JSON_ADAPTER_CLASS_NAME = ClassName.bestGuess(JsonAdapter::class.qualifiedName!!)
    private val TYPES_CLASS_NAME = ClassName.bestGuess(Types::class.qualifiedName!!)
    private const val UNIVERSAL_ADAPTER_CLASS_NAME = "UniversalAdapterFactory"
    private const val TAG = "UniversalSymbolProcessor"
  }
  val logger = environment.logger

  @OptIn(KspExperimental::class)
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val ksFiles = mutableListOf<KSFile>()
    val classesType = mutableListOf<Triple<ClassName, ClassName, Int>>()
    resolver
      .getSymbolsWithAnnotation(JSON_CLASS_NAME)
      .filter { type ->
        if (type is KSClassDeclaration) {
          val jsonClass =
            type.getAnnotationsByType(JsonClass::class).firstOrNull() ?: return@filter false
          return@filter jsonClass.generator.isEmpty() && jsonClass.generateAdapter
        }
        return@filter false
      }
      .forEach { type ->
        val kSClassDeclaration = type as KSClassDeclaration
        val className = kSClassDeclaration.toClassName()
        val adapterName = "${className.simpleNames.joinToString(separator = "_")}JsonAdapter"
        classesType.add(
          Triple(
            ClassName.bestGuess(className.canonicalName),
            ClassName.bestGuess("${className.packageName}.${adapterName}"),
            kSClassDeclaration.typeParameters.size
          )
        )
        type.containingFile?.let { ksFiles.add(it) }
      }
    if (classesType.isNotEmpty()) {
      classesType.sortBy { it.first }

      val universalAdapterClass =
        TypeSpec.classBuilder(UNIVERSAL_ADAPTER_CLASS_NAME).addSuperinterface(ADAPTER_CLASS_NAME)
      val createFun =
        FunSpec.builder("create")
          .addModifiers(KModifier.OVERRIDE)
          .addParameter("type", Type::class)
          .addParameter(
            "p1",
            MutableSet::class.asTypeName().plusParameter(Annotation::class.asTypeName())
          )
          .addParameter("moshi", Moshi::class)
          .returns(
            JsonAdapter::class.asTypeName().plusParameter(TypeVariableName.invoke("*")).copy(true)
          )
          .addStatement("if (p1.isNotEmpty()) return null")
          .addStatement("return if (type is %T) {", ParameterizedType::class)
          .addStatement("getJsonAdapterClass(type, moshi)")
          .addStatement("} else { ")
          .addStatement("getJsonAdapterClass(type, moshi)")
          .addStatement("}")

      universalAdapterClass.addFunction(createFun.build())

      val getJsonAdapterClassFun =
        FunSpec.builder("getJsonAdapterClass")
          .addParameter("type", Type::class.java)
          .addParameter("moshi", Moshi::class.java)
          .addCode(CodeBlock.of("return when(%T.getRawType(type)){ \n", TYPES_CLASS_NAME))
      classesType
        .filter { it.third == 0 }
        .forEach {
          getJsonAdapterClassFun.addCode(
            CodeBlock.of("%T::class.java -> %T(moshi) \n", it.first, it.second)
          )
        }
      getJsonAdapterClassFun
        .addCode(CodeBlock.of("else -> null \n"))
        .addCode(CodeBlock.of("}"))
        .returns(JSON_ADAPTER_CLASS_NAME.plusParameter(TypeVariableName.invoke("*")).copy(true))
      universalAdapterClass.addFunction(getJsonAdapterClassFun.build())

      fun anyT(size: Int): String {
        var result = ""
        for (index in 0 until size) {
          result += if (index != size - 1) "Any," else "Any"
        }
        return result
      }
      val getJsonAdapterForParameterizedTypeFun =
        FunSpec.builder("getJsonAdapterClass")
          .addParameter("type", ParameterizedType::class.java)
          .addParameter("moshi", Moshi::class.java)
          .addCode(CodeBlock.of("return when(%T.getRawType(type)){ \n", TYPES_CLASS_NAME))
      classesType
        .filter { it.third != 0 }
        .forEach {
          getJsonAdapterForParameterizedTypeFun.addCode(
            "%T::class.java -> %T<${anyT(it.third)}>(moshi,type.actualTypeArguments) \n",
            it.first,
            it.second
          )
        }

      getJsonAdapterForParameterizedTypeFun
        .addCode(CodeBlock.of("else -> null \n"))
        .addCode(CodeBlock.of("}"))
        .returns(JSON_ADAPTER_CLASS_NAME.plusParameter(TypeVariableName.invoke("*")).copy(true))
      universalAdapterClass.addFunction(getJsonAdapterForParameterizedTypeFun.build())
      FileSpec.builder(ADAPTER_CLASS_NAME.packageName, UNIVERSAL_ADAPTER_CLASS_NAME)
        .addType(universalAdapterClass.build())
        .build()
        .writeTo(environment.codeGenerator, Dependencies(false, *ksFiles.toTypedArray()))
    }

    return emptyList()
  }
}
