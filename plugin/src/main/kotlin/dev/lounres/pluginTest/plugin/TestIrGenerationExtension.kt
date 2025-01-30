package dev.lounres.pluginTest.plugin

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name


class TestIrGenerationExtension(
    private val messageCollector: MessageCollector,
    private val string: String,
    private val file: String,
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val typeAnyNullable = pluginContext.irBuiltIns.anyNType
        
        val debugLogAnnotation = pluginContext.referenceClass(ClassId(packageFqName = FqName.ROOT, relativeClassName = FqName("DebugLog"), isLocal = false))!!
        val funPrintln = pluginContext.referenceFunctions(CallableId(packageName = FqName("kotlin.io"), className = null, callableName = Name.identifier("println")))
            .single {
                val parameters = it.owner.valueParameters
                parameters.size == 1 && parameters[0].type == typeAnyNullable
            }
        
        moduleFragment.transform(DebugLogTransformer(pluginContext, debugLogAnnotation, funPrintln), null)
    }
}

class DebugLogTransformer(
    private val pluginContext: IrPluginContext,
    private val annotationClass: IrClassSymbol,
    private val logFunction: IrSimpleFunctionSymbol,
) : IrElementTransformerVoidWithContext() {
    private val typeUnit = pluginContext.irBuiltIns.unitType
    private val typeThrowable = pluginContext.irBuiltIns.throwableType
    
    private val classMonotonic =
        pluginContext.referenceClass(ClassId(packageFqName = FqName("kotlin.time"), relativeClassName = FqName("TimeSource.Monotonic"), false))!!
    
    private val funMarkNow =
        pluginContext.referenceFunctions(
            CallableId(
                packageName = FqName("kotlin.time"),
                className = FqName("TimeSource"),
                callableName = Name.identifier("markNow"),
            )
        ).single()

    private val funElapsedNow =
        pluginContext.referenceFunctions(
            CallableId(
                packageName = FqName("kotlin.time"),
                className = FqName("TimeMark"),
                callableName = Name.identifier("elapsedNow"),
            )
        ).single()
    
    private fun IrBuilderWithScope.irDebugEnter(
        function: IrFunction
    ): IrCall {
        val file = function.file.fileEntry
        val concat = irConcat()
        concat.addArgument(irString("⇢ ${function.kotlinFqName} (${file.name.substringAfterLast('/')}:${file.getLineNumber(function.startOffset) + 1}) ("))
        for ((index, valueParameter) in function.valueParameters.withIndex()) {
            if (index > 0) concat.addArgument(irString(", "))
            concat.addArgument(irString("${valueParameter.name}="))
            concat.addArgument(irGet(valueParameter))
        }
        concat.addArgument(irString(")"))
        
        return irCall(logFunction).also { call ->
            call.putValueArgument(0, concat)
        }
    }
    
    private fun IrBuilderWithScope.irDebugExit(
        function: IrFunction,
        startTime: IrValueDeclaration,
        result: IrExpression? = null
    ): IrCall {
        val concat = irConcat()
        concat.addArgument(irString("⇠ ${function.kotlinFqName} ["))
        concat.addArgument(irCall(funElapsedNow).also { call ->
            call.dispatchReceiver = irGet(startTime)
        })
        if (result != null) {
            concat.addArgument(irString("] = "))
            concat.addArgument(result)
        } else {
            concat.addArgument(irString("]"))
        }
        
        return irCall(logFunction).also { call ->
            call.putValueArgument(0, concat)
        }
    }
    
    inner class DebugLogReturnTransformer(
        private val function: IrFunction,
        private val startTime: IrVariable
    ) : IrElementTransformerVoidWithContext() {
        override fun visitReturn(expression: IrReturn): IrExpression {
            if (expression.returnTargetSymbol != function.symbol) return super.visitReturn(expression)
            
            return DeclarationIrBuilder(pluginContext, function.symbol).irBlock {
                val result = irTemporary(expression.value)
                +irDebugExit(function, startTime, irGet(result))
//                +irReturn(irGet(result))
                +expression.apply {
                    value = irGet(result)
                }
            }
        }
    }
    
    private fun irDebug(
        function: IrFunction,
        body: IrBody
    ): IrBlockBody {
        return DeclarationIrBuilder(pluginContext, function.symbol).irBlockBody {
            +irDebugEnter(function)
            
            val startTime = irTemporary(irCall(funMarkNow).also { call ->
                call.dispatchReceiver = irGetObject(classMonotonic)
            })
            
            val tryBlock = irBlock(resultType = function.returnType) {
                for (statement in body.statements) +statement
                if (function.returnType == typeUnit) +irDebugExit(function, startTime)
            }.transform(DebugLogReturnTransformer(function, startTime), null)
            
            val throwable = buildVariable(
                scope.getLocalDeclarationParent(), startOffset, endOffset, IrDeclarationOrigin.CATCH_PARAMETER,
                Name.identifier("t"), typeThrowable
            )
            
            +IrTryImpl(startOffset, endOffset, tryBlock.type).also { irTry ->
                irTry.tryResult = tryBlock
                irTry.catches += irCatch(throwable, irBlock {
                    +irDebugExit(function, startTime, irGet(throwable))
                    +irThrow(irGet(throwable))
                })
            }
        }
    }
    
    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        val body = declaration.body
        if (body != null && declaration.hasAnnotation(annotationClass)) {
            declaration.body = irDebug(declaration, body)
        }
        return super.visitFunctionNew(declaration)
    }
}

