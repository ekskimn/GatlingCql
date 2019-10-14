/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 GatlingCql developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.gatling.cql.checks

import com.datastax.driver.core.{ExecutionInfo, ResultSet}
import io.gatling.commons.validation.SuccessWrapper
import io.gatling.core.check.{Check, CheckMaterializer, Preparer, Specializer}
import io.github.gatling.cql.response.CqlResponse

object CqlCheckBuilders {
  val RowCountSpecializer: Specializer[CqlCheck, CqlResponse] = specializer()
  val ResultSetSpecializer: Specializer[CqlCheck, CqlResponse] = specializer()
  val ExecutionInfoSpecializer: Specializer[CqlCheck, CqlResponse] = specializer()
  val AppliedSpecializer: Specializer[CqlCheck, CqlResponse] = specializer()
  val ExhaustedSpecializer: Specializer[CqlCheck, CqlResponse] = specializer()
  val ColumnSpecializer: Specializer[CqlCheck, CqlResponse] = specializer()

  val RowCountPreparer: Preparer[CqlResponse, Int] = _.rowCount.success
  val ResultSetPreparer: Preparer[CqlResponse, ResultSet] = _.resultSet.success
  val ExecutionInfoPreparer: Preparer[CqlResponse, ExecutionInfo] = _.resultSet.getExecutionInfo.success
  val AppliedPreparer: Preparer[CqlResponse, Boolean] = _.resultSet.wasApplied.success
  val ExhaustedPreparer: Preparer[CqlResponse, Boolean] = _.resultSet.isExhausted.success

  def ColumnPreparer(name: String): Preparer[CqlResponse, Seq[Any]] = _.column(name).success

  private def specializer(): Specializer[CqlCheck, CqlResponse] =
    (wrapped: Check[CqlResponse]) => CqlCheck(wrapped)

  private def responseExtender(): Specializer[CqlCheck, CqlResponse] =
    (wrapped: Check[CqlResponse]) => CqlCheck(wrapped)

  val ResponseExtender: Specializer[CqlCheck, CqlResponse] = responseExtender()

  val PassThroughResponsePreparer: Preparer[CqlResponse, CqlResponse] = (r: CqlResponse) => r.success

  class ColumnProvider(val name: String) extends CheckMaterializer[CqlCheck, CqlCheck, CqlResponse, Seq[Any]](specializer = ColumnSpecializer) {
    override def preparer: Preparer[CqlResponse, Seq[Any]] = ColumnPreparer(name)
  }

  object ColumnProvider {
    def apply(name: String): ColumnProvider = {
      new ColumnProvider(name)
    }
  }

  object CqlCheckProvider extends CheckMaterializer[CqlCheck, CqlCheck, CqlResponse, CqlResponse](specializer = responseExtender()) {
    override val preparer: Preparer[CqlResponse, CqlResponse] = PassThroughResponsePreparer
  }

  object ExecutionInfoProvider extends CheckMaterializer[CqlCheck, CqlCheck, CqlResponse, ExecutionInfo](specializer = ExecutionInfoSpecializer) {
    override val preparer: Preparer[CqlResponse, ExecutionInfo] = ExecutionInfoPreparer
  }

  object ResultSetProvider extends CheckMaterializer[CqlCheck, CqlCheck, CqlResponse, ResultSet](specializer = ResultSetSpecializer) {
    override val preparer: Preparer[CqlResponse, ResultSet] = ResultSetPreparer
  }

  object RowCountProvider extends CheckMaterializer[CqlCheck, CqlCheck, CqlResponse, Int](specializer = RowCountSpecializer) {
    override val preparer: Preparer[CqlResponse, Int] = RowCountPreparer
  }

  object AppliedProvider extends CheckMaterializer[CqlCheck, CqlCheck, CqlResponse, Boolean](specializer = AppliedSpecializer) {
    override val preparer: Preparer[CqlResponse, Boolean] = AppliedPreparer
  }

  object ExhaustedProvider extends CheckMaterializer[CqlCheck, CqlCheck, CqlResponse, Boolean](specializer = ExhaustedSpecializer) {
    override val preparer: Preparer[CqlResponse, Boolean] = ExhaustedPreparer
  }
}