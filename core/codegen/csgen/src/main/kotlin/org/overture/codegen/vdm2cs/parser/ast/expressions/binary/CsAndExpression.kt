package org.overture.codegen.vdm2cs.parser.ast.expressions.binary

import org.overture.codegen.vdm2cs.parser.ast.expressions.CsExpression

data class CsAndExpression(override val left: CsExpression,
                           override val right: CsExpression) : CsBinaryOperatorExpression
