package io.github.gatling.cql

import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.check.{CheckBuilder, CheckProtocolProvider, FindCheckBuilder, ValidatorCheckBuilder}
import io.github.gatling.cql.checks.{CqlCheck, CqlCheckSupport}
import io.github.gatling.cql.request.{CqlProtocol, CqlProtocolBuilder, CqlRequestBuilder, CqlRequestBuilderBase}
import io.github.gatling.cql.response.CqlResponse

import scala.annotation.implicitNotFound

trait CqlDsl extends CqlCheckSupport {
  val cql = CqlProtocolBuilder

  def cql(tag: String) = CqlRequestBuilderBase(tag)

  implicit def cqlProtocolBuilder2cqlProtocol(builder: CqlProtocolBuilder): CqlProtocol = builder.build
  implicit def cqlRequestBuilder2ActionBuilder(builder: CqlRequestBuilder): ActionBuilder = builder.build()

  @implicitNotFound("Could not find a CheckProtocolProvider. This check might not be a valid CQL one.")
  implicit def checkBuilder2CqlCheck[A, P, X](checkBuilder: CheckBuilder[A, P, X])(implicit provider: CheckProtocolProvider[A, CqlCheck, CqlResponse, P]): CqlCheck =
    checkBuilder.build(provider)

  @implicitNotFound("Could not find a CheckProtocolProvider. This check might not be a valid CQL one.")
  implicit def validatorCheckBuilder2HttpCheck[A, P, X](validatorCheckBuilder: ValidatorCheckBuilder[A, P, X])(implicit provider: CheckProtocolProvider[A, CqlCheck, CqlResponse, P]): CqlCheck =
    validatorCheckBuilder.exists

  @implicitNotFound("Could not find a CheckProtocolProvider. This check might not be a valid CQL one.")
  implicit def findCheckBuilder2HttpCheck[A, P, X](findCheckBuilder: FindCheckBuilder[A, P, X])(implicit provider: CheckProtocolProvider[A, CqlCheck, CqlResponse, P]): CqlCheck =
    findCheckBuilder.find.exists


}
