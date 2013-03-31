//
// Copyright 2013 Vibul Imtarnasan, David Bolton and Socko contributors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.mashupbots.socko.rest

import org.json4s.NoTypeHints
import org.json4s.native.{Serialization => json}
import org.mashupbots.socko.infrastructure.Logger
import org.scalatest.GivenWhenThen
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class RestRegistrySpec extends WordSpec with MustMatchers with GivenWhenThen with Logger {

  "RestRegistrySpec" must {

  val cfg = RestConfig("1.0", "/api")
    
    "correctly find operations" in {
      val r = RestRegistry("org.mashupbots.socko.rest.test1", cfg)
      r.operations.length must be(3)

      r.operations.exists(op => op.definition.method == "GET" &&
        op.definition.urlTemplate == "/pets" &&
        op.definition.dispatcherClass == "" &&
        op.deserializer.requestClass.fullName == "org.mashupbots.socko.rest.test1.GetPetsRequest" &&
        op.serializer.responseClass.fullName == "org.mashupbots.socko.rest.test1.GetPetsResponse") must be(true)

      r.operations.exists(op => op.definition.method == "GET" &&
        op.definition.urlTemplate == "/dogs1" &&
        op.definition.dispatcherClass == "GetPetsDispatcher" &&
        op.deserializer.requestClass.fullName == "org.mashupbots.socko.rest.test1.GetDogs1Request" &&
        op.serializer.responseClass.fullName == "org.mashupbots.socko.rest.test1.GetFunnyNameDogResponse") must be(true)

      r.operations.exists(op => op.definition.method == "GET" &&
        op.definition.urlTemplate == "/dogs2" &&
        op.definition.dispatcherClass == "org.mashupbots.socko.rest.test1.GetPetsDispatcher" &&
        op.definition.errorResponses.size == 2 &&
        op.deserializer.requestClass.fullName == "org.mashupbots.socko.rest.test1.GetDogs2Request" &&
        op.serializer.responseClass.fullName == "org.mashupbots.socko.rest.test1.GetFunnyNameDogResponse") must be(true)
    }

    "catch duplcate operation addresses" in {
      val thrown = intercept[RestDefintionException] {
        val r = RestRegistry("org.mashupbots.socko.rest.test2", cfg)
      }
      thrown.getMessage must be ("Operation 'GET /pets' for 'org.mashupbots.socko.rest.test2.GetPets1Request' resolves to the same address as 'GET /pets' for 'org.mashupbots.socko.rest.test2.GetPets2Request'")
    }

    "correctly bind parameters" in {
      val r = RestRegistry("org.mashupbots.socko.rest.test3", cfg)
      r.operations.length must be(1)

      r.operations.exists(op => op.definition.method == "GET" &&
        op.definition.urlTemplate == "/pets/{id}" &&
        op.definition.dispatcherClass == "" &&
        op.deserializer.requestClass.fullName == "org.mashupbots.socko.rest.test3.GetPetRequest" &&
        op.serializer.responseClass.fullName == "org.mashupbots.socko.rest.test3.GetPetResponse") must be(true)
    }
    
    "test deserialize" in {
      
      val s = "{\"name\":\"Boo\",\"age\":5}"
      val formats = json.formats(NoTypeHints)

      val clz = Class.forName("org.mashupbots.socko.rest.Horse")
      val y = org.json4s.reflect.Reflector.scalaTypeOf(clz)
      val z = org.json4s.reflect.ManifestFactory.manifestOf(y)
      val x = json.read(s)(formats, z)
      
      val hh = x.asInstanceOf[Horse]
      hh.name must be ("Boo")
    }
    
  }
}

case class Horse(name: String, age: Int)
