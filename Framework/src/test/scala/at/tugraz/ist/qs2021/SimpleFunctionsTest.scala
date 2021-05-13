package at.tugraz.ist.qs2021

import at.tugraz.ist.qs2021.simple.SimpleFunctions._
//import at.tugraz.ist.qs2021.simple.SimpleFunctionsMutant1._
import at.tugraz.ist.qs2021.simple.SimpleJavaFunctions
import org.junit.runner.RunWith
import org.scalacheck.Prop.{forAll, propBoolean}
import org.scalacheck.{Arbitrary, Gen, Properties}

// Consult the following scalacheck documentation
// https://github.com/typelevel/scalacheck/blob/master/doc/UserGuide.md#concepts
// https://github.com/typelevel/scalacheck/blob/master/doc/UserGuide.md#generators

@RunWith(classOf[ScalaCheckJUnitPropertiesRunner])
class SimpleFunctionsTest extends Properties("SimpleFunctionsTest") {

  // Gen is some sort of function from scala check,
  // it is responsible to provide you random generated test data
  private val nonEmptyIntListGen: Gen[List[Int]] = Gen.nonEmptyListOf(Arbitrary.arbitrary[Int])

  // insertionSort Java style
  property("insertionSort Java: ordered") = forAll(nonEmptyIntListGen) { (xs: List[Int]) =>
    val sorted = SimpleJavaFunctions.insertionSort(xs.toArray)
    var correctFlag = true;
    if (xs.nonEmpty) {
      for (i <- 0 until sorted.length - 1) {
        if (sorted(i) > sorted(i + 1))
          correctFlag = false;
      }
      correctFlag // would be the return val
    }
    else
      false // returns false if xs is empty
  }

  // insertionSort the beautiful scala way
  property("insertionSort: ordered") = forAll(nonEmptyIntListGen) { (xs: List[Int]) =>
    val sorted = insertionSort(xs)
    xs.nonEmpty ==> xs.indices.tail.forall((i: Int) => sorted(i - 1) <= sorted(i))
  }
  property("insertionSort: permutation") = forAll { (xs: List[Int]) =>
    val sorted = insertionSort(xs)

    def count(a: Int, as: List[Int]) = as.count(_ == a)

    xs.forall((x: Int) => count(x, xs) == count(x, sorted))
  }


  // maximum
  property("maximum") = forAll(nonEmptyIntListGen) { (xs: List[Int]) =>
    val max_element = max(xs)
    xs.nonEmpty ==> xs.forall((x: Int) => x <= max_element)
  }

  // minimal index
  property("minimal Index") = forAll(nonEmptyIntListGen) { (xs: List[Int]) =>
    val minimumIndex = minIndex(xs)
    val minimumValue = xs(minimumIndex)
    xs.nonEmpty ==> xs.indexOf(minimumValue).equals(minimumIndex)
  }

  // symmetric difference
  property("symmetricDifference: order") = forAll(nonEmptyIntListGen, nonEmptyIntListGen) { (as: List[Int], bs: List[Int]) =>
    val asDifference= as.diff(bs)
    val bsDifference = bs.diff(as)
    val differrence = (asDifference++bsDifference).distinct
    val symmetric = symmetricDifference(as, bs)
    as.nonEmpty || bs.nonEmpty ==> symmetric.forall((x: Int) => differrence.contains(x))
  }

  property("symmetricDifference: permutation") = forAll(nonEmptyIntListGen, nonEmptyIntListGen) { (as: List[Int], bs: List[Int]) =>
    val asDifference= as.diff(bs)
    val bsDifference = bs.diff(as)
    val differrence = (asDifference++bsDifference).distinct
    val symmetric = symmetricDifference(as, bs)

    def count(a: Int, as: List[Int]) = as.count( _ == a )
    as.nonEmpty || bs.nonEmpty ==> symmetric.forall((x: Int) => count(x, symmetric) == count(x, differrence))
  }

  // intersection
  property("intersection: order") = forAll(nonEmptyIntListGen, nonEmptyIntListGen) { (as: List[Int], bs: List[Int]) =>
    val intersect = intersection(as, bs)
    val asIntersect = as.intersect(bs)
    val bsIntersect = bs.intersect(as)
    val merge = (asIntersect++bsIntersect).distinct
    as.nonEmpty || bs.nonEmpty ==> intersect.forall((x: Int) => merge.contains(x))
  }

  property("intersection: permutation") = forAll(nonEmptyIntListGen, nonEmptyIntListGen) { (as: List[Int], bs: List[Int]) =>
    val intersect = intersection(as, bs)
    val asIntersect = as.intersect(bs)
    val bsIntersect = bs.intersect(as)
    val merge = (asIntersect++bsIntersect).distinct
    def count(a: Int, as: List[Int]) = as.count( _ == a )
    as.nonEmpty || bs.nonEmpty ==>  intersect.forall((x: Int) => count(x, intersect) == count(x, merge))
  }

  // Smallest missing positive integer
  property("SMPI: order") = forAll(nonEmptyIntListGen, nonEmptyIntListGen) { (as: List[Int], bs: List[Int]) =>
    val merge = (as++bs).distinct
    val newMerge  = merge.filter(_ > 0)
    val smallestInteger = smallestMissingPositiveInteger(newMerge)
    as.nonEmpty || bs.nonEmpty ==> merge.forall((x: Int) => x > smallestInteger)
  }

  property("SMPI: permutation") = forAll(nonEmptyIntListGen, nonEmptyIntListGen) { (as: List[Int], bs: List[Int]) =>
    val merge = (as++bs).distinct
    val newMerge  = merge.filter(_ > 0)
    val smallestInteger = smallestMissingPositiveInteger(newMerge)

    def count(a: Int, as: List[Int]) = as.count( _ == a )
    as.nonEmpty || bs.nonEmpty ==> newMerge.forall((x: Int) => count(x, newMerge) != smallestInteger)
  }
}
