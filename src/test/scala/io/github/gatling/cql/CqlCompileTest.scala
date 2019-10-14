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
package io.github.gatling.cql

import com.datastax.driver.core

import scala.concurrent.duration.DurationInt
import com.datastax.driver.core.{Cluster, ConsistencyLevel, PreparedStatement}
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.github.gatling.cql.Predef._
import io.github.gatling.cql.request.CqlProtocolBuilder

import scala.util.Random

class CqlCompileTest extends Simulation {
  val keyspace: String = "test"
  val table_name: String = "test_table"
  val cluster: Cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
  val session: core.Session = cluster.connect(s"$keyspace")
  val cqlConfig: CqlProtocolBuilder = cql.session(session)

  session.execute(s"CREATE KEYSPACE IF NOT EXISTS $keyspace WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor': '1'}")
  session.execute(s"""CREATE TABLE IF NOT EXISTS $table_name (
		  id timeuuid,
		  num int,
		  str text,
		  PRIMARY KEY (id)
		);
      """)
  session.execute(f"CREATE INDEX IF NOT EXISTS $table_name%s_num_idx ON $table_name%s (num)")

  val prepared: PreparedStatement = session.prepare(s"INSERT INTO $table_name (id, num, str) values (now(), ?, ?)")

  val random: Random = new util.Random
  val feeder: Iterator[Map[String, Any]] = Iterator.continually(
      Map(
        "randomString" -> random.nextString(20),
        "randomNum" -> random.nextInt(),
        "randomParams" -> Seq[AnyRef](Integer.valueOf(random.nextInt()), random.nextString(20))
      )
  )

  val scn: ScenarioBuilder = scenario(scenarioName = "Two statements").repeat(times = 1) {
    feed(feeder)
    .exec(http(requestName = "GET").get("http://foo.bar/")
      .check(bodyString.transform(string => string.length).lt(expected = 100000))
    )
    .exec(cql(tag = "simple SELECT")
        .execute(statement = "SELECT * FROM test_table WHERE num = ${randomNum}")
        .check(exhausted is false)
        .check(applied is false)
        .check(columnValue(columnName = "num").count.gt(expected = 1))
    )

/*
        .check { result =>
          if (result.all().isEmpty) {
            Failure("failed test")
          } else {
            Success(true)
          }
        }
        .postProcess {
          param => {
            if (!param.result.isExhausted)
              param.session.set("row-of-" + param.tag, param.result.one().getInt("num"))
            else
              param.session
          }
        })
*/

    .exec(cql(tag = "prepared INSERT")
        .execute(prepared)
        .withParams(Integer.valueOf(random.nextInt()), "${randomString}")
        .consistencyLevel(ConsistencyLevel.ANY)
    )

    .exec(cql(tag = "adhoc statements with params")
      .execute(statement = "INSERT INTO $table_name (id, num, str) values (now(), ?, ?)", params = "${randomParams}")
      .consistencyLevel(ConsistencyLevel.ANY)
    )
  }

  setUp(scn.inject(rampUsersPerSec(rate1 = 10) to 100 during (30 seconds)))
    .protocols(cqlConfig)
  after(cluster.close())
}
