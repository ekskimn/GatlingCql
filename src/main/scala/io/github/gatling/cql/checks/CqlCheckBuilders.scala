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
import io.gatling.core.check.{CheckProtocolProvider, _}
import io.github.gatling.cql.response.CqlResponse

object CqlCheckBuilders {
  val RowCountSpecializer = specializer()
  val ResultSetSpecializer = specializer()
  val ExecutionInfoSpecializer = specializer()
  val AppliedSpecializer = specializer()
  val ExhaustedSpecializer = specializer()
  val ColumnSpecializer = specializer()

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

  val ResponseExtender = responseExtender()

  val PassThroughResponsePreparer: Preparer[CqlResponse, CqlResponse] = (r: CqlResponse) => r.success

  class ColumnProvider(val name: String) extends CheckProtocolProvider[CqlCheck, CqlCheck, CqlResponse, Seq[Any]] {
    override def preparer: Preparer[CqlResponse, Seq[Any]] = ColumnPreparer(name)

    override val specializer: Specializer[CqlCheck, CqlResponse] = ColumnSpecializer
  }

  object ColumnProvider {
    def apply(name: String) = {
      new ColumnProvider(name)
    }
  }

  object CqlCheckProvider extends CheckProtocolProvider[CqlCheck, CqlCheck, CqlResponse, CqlResponse] {
    override val preparer: Preparer[CqlResponse, CqlResponse] = PassThroughResponsePreparer

    override val specializer: Specializer[CqlCheck, CqlResponse] = responseExtender()
  }

  object ExecutionInfoProvider extends CheckProtocolProvider[CqlCheck, CqlCheck, CqlResponse, ExecutionInfo] {
    override val preparer: Preparer[CqlResponse, ExecutionInfo] = ExecutionInfoPreparer

    override val specializer: Specializer[CqlCheck, CqlResponse] = ExecutionInfoSpecializer
  }

  object ResultSetProvider extends CheckProtocolProvider[CqlCheck, CqlCheck, CqlResponse, ResultSet] {
    override val preparer: Preparer[CqlResponse, ResultSet] = ResultSetPreparer

    override val specializer: Specializer[CqlCheck, CqlResponse] = ResultSetSpecializer
  }

  object RowCountProvider extends CheckProtocolProvider[CqlCheck, CqlCheck, CqlResponse, Int] {
    override val preparer: Preparer[CqlResponse, Int] = RowCountPreparer

    override val specializer: Specializer[CqlCheck, CqlResponse] = RowCountSpecializer
  }

  object AppliedProvider extends CheckProtocolProvider[CqlCheck, CqlCheck, CqlResponse, Boolean] {
    override val preparer: Preparer[CqlResponse, Boolean] = AppliedPreparer

    override val specializer: Specializer[CqlCheck, CqlResponse] = AppliedSpecializer
  }

  object ExhaustedProvider extends CheckProtocolProvider[CqlCheck, CqlCheck, CqlResponse, Boolean] {
    override val preparer: Preparer[CqlResponse, Boolean] = ExhaustedPreparer

    override val specializer: Specializer[CqlCheck, CqlResponse] = ExhaustedSpecializer
  }

}