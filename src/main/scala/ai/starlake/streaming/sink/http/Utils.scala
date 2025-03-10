package ai.starlake.streaming.sink.http

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.{JsonSetter, Nulls}
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect.runtime.universe

object Utils {
  def loadInstance[T](objectName: String): T = {
    val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
    val module = runtimeMirror.staticModule(objectName)
    val obj: universe.ModuleMirror = runtimeMirror.reflectModule(module)
    obj.instance.asInstanceOf[T]
  }

  def newJsonMapper(): ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper
      .registerModule(DefaultScalaModule)
      .setSerializationInclusion(Include.NON_EMPTY)
      .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY, Nulls.AS_EMPTY))
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper
  }
}
