/* Copyright (C) 2008-2014 University of Massachusetts Amherst.
   This file is part of "FACTORIE" (Factor graphs, Imperative, Extensible)
   http://factorie.cs.umass.edu, http://github.com/factorie
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */



package cc.factorie.tutorial
import cc.factorie._
import cc.factorie.model.{DotTemplate2, Parameters, TemplateModel}
import cc.factorie.variable.{DiscreteDomain, DiscreteVariable, Var, Vars}

import scala.collection.mutable.ArrayBuffer

/** Demonstrate how to create a factor that has a varying number of neighbors. */
object VarArgsDemo {
  def main(args:Array[String]): Unit = {
    object XDomain extends DiscreteDomain(10) // { def size = 10 }
    class X(i:Int) extends DiscreteVariable(i) { 
      def domain = XDomain
      val ys = new ArrayBuffer[Y]
      def addY(i:Int) = ys += new Y(i, this)
    }
    class Y(i:Int, val x:X) extends DiscreteVariable(i) {
      def domain = XDomain
    }

    val model = new TemplateModel with Parameters {
      this += new DotTemplate2[X,Vars[Y]] { // "Vars[]" indicates that there can be a variable number of these neighbors
        val weights = Weights(new la.DenseTensor1(XDomain.size))
        def unroll1(x:X) = Factor(x, Vars(x.ys))
        // The "Vars" container will not change...
        def unroll2(ys:Vars[Y]) = throw new Error
        // ...but this template will notice changes to the individual "Y" contents of "Vars"
        // While unroll1 and unroll2 do not need the "override" modifier,
        // (unfortunately, because of Scala limitations) unroll2s does.
        override def unroll(y:Var) = y match { case y:Y => Factor(y.x, Vars(y.x.ys)); case _ => Nil }
        override def statistics(v1:X#Value, v2:Seq[Y#Value]) = {
          val x: Int = v1.intValue
          val ys: Seq[Int] = v2.map(_.intValue)
          XDomain.apply(x % ys.foldLeft(0)(_+_))
        }
      }
    }

    // The "Vars" trait is defined in Variable.scala
    // It, in turn, is just one sub-trait of the "ContainerVariable" trait,
    // which is the one that Template looks for to know if it should unroll
    // for variables of its "ContainedVariableType".

    // You can make your own subclasses of "ContainerVariable", which 
    // might legitimately have diffs, in which case the analogue of "unroll2"
    // above would not simply throw an Error, but have a real implementation.

    // The alternative way to obtain some "var-args-like" functionality that may be
    // more convenient in certain models is to override "unrollCascade" in
    // some of your variables.  This method should return a collection of additional
    // variables that should also be unrolled whenever there is a request to unroll 
    // this variable.  (E.g. whenever variable "x" is changed, all the variables
    // returned by "x.unrollCascade" should also be considered to have changed.
    // Templates call this method on each variable they are asked to unroll.
    // The default implementation of this method returns "Nil".

    val x1 = new X(6); for (i <- 1 to 3) x1.addY(i)
    val x2 = new X(7); for (i <- 1 to 4) x1.addY(i)
    val x3 = new X(8); for (i <- 1 to 5) x1.addY(i)
    val x4 = new X(9); for (i <- 1 to 6) x1.addY(i)

//    println(model.factors(Seq(x1)))
//    println(model.factors(List(x1,x2,x3,x4)))
//    println(model.factors(Seq(x1)).head.currentScore)
  }
}
