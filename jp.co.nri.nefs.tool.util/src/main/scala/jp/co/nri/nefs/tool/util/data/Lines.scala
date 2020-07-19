package jp.co.nri.nefs.tool.util.data

import jp.co.nri.nefs.tool.util.data.format._


object Lines {

  def mapping[R, A1](a1: (String, Mapping[A1]))(apply: A1 => R)(unapply: R => Option[A1]): Mapping[R] = {
    new ObjectMapping1(apply, unapply, a1)
  }

  def mapping[R, A1, A2](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]))(apply: (A1, A2) => R)(unapply: R => Option[(A1, A2)]): Mapping[R] = {
    new ObjectMapping2(apply, unapply, a1, a2)
  }

  def mapping[R, A1, A2, A3](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]))(apply: (A1, A2, A3) => R)(unapply: R => Option[(A1, A2, A3)]): Mapping[R] = {
    new ObjectMapping3(apply, unapply, a1, a2, a3)
  }

  def mapping[R, A1, A2, A3, A4](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]))(apply: (A1, A2, A3, A4) => R)(unapply: R => Option[(A1, A2, A3, A4)]): Mapping[R] = {
    new ObjectMapping4(apply, unapply, a1, a2, a3, a4)
  }

  def mapping[R, A1, A2, A3, A4, A5](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]))(apply: (A1, A2, A3, A4, A5) => R)(unapply: R => Option[(A1, A2, A3, A4, A5)]): Mapping[R] = {
    new ObjectMapping5(apply, unapply, a1, a2, a3, a4, a5)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]))(apply: (A1, A2, A3, A4, A5, A6) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6)]): Mapping[R] = {
    new ObjectMapping6(apply, unapply, a1, a2, a3, a4, a5, a6)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]))(apply: (A1, A2, A3, A4, A5, A6, A7) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7)]): Mapping[R] = {
    new ObjectMapping7(apply, unapply, a1, a2, a3, a4, a5, a6, a7)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8)]): Mapping[R] = {
    new ObjectMapping8(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9)]): Mapping[R] = {
    new ObjectMapping9(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)]): Mapping[R] = {
    new ObjectMapping10(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)]): Mapping[R] = {
    new ObjectMapping11(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]), a12: (String, Mapping[A12]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)]): Mapping[R] = {
    new ObjectMapping12(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]), a12: (String, Mapping[A12]), a13: (String, Mapping[A13]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)]): Mapping[R] = {
    new ObjectMapping13(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]), a12: (String, Mapping[A12]), a13: (String, Mapping[A13]), a14: (String, Mapping[A14]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)]): Mapping[R] = {
    new ObjectMapping14(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]), a12: (String, Mapping[A12]), a13: (String, Mapping[A13]), a14: (String, Mapping[A14]), a15: (String, Mapping[A15]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)]): Mapping[R] = {
    new ObjectMapping15(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]), a12: (String, Mapping[A12]), a13: (String, Mapping[A13]), a14: (String, Mapping[A14]), a15: (String, Mapping[A15]), a16: (String, Mapping[A16]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)]): Mapping[R] = {
    new ObjectMapping16(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]), a12: (String, Mapping[A12]), a13: (String, Mapping[A13]), a14: (String, Mapping[A14]), a15: (String, Mapping[A15]), a16: (String, Mapping[A16]), a17: (String, Mapping[A17]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)]): Mapping[R] = {
    new ObjectMapping17(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]), a12: (String, Mapping[A12]), a13: (String, Mapping[A13]), a14: (String, Mapping[A14]), a15: (String, Mapping[A15]), a16: (String, Mapping[A16]), a17: (String, Mapping[A17]), a18: (String, Mapping[A18]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)]): Mapping[R] = {
    new ObjectMapping18(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]), a12: (String, Mapping[A12]), a13: (String, Mapping[A13]), a14: (String, Mapping[A14]), a15: (String, Mapping[A15]), a16: (String, Mapping[A16]), a17: (String, Mapping[A17]), a18: (String, Mapping[A18]), a19: (String, Mapping[A19]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)]): Mapping[R] = {
    new ObjectMapping19(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]), a12: (String, Mapping[A12]), a13: (String, Mapping[A13]), a14: (String, Mapping[A14]), a15: (String, Mapping[A15]), a16: (String, Mapping[A16]), a17: (String, Mapping[A17]), a18: (String, Mapping[A18]), a19: (String, Mapping[A19]), a20: (String, Mapping[A20]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)]): Mapping[R] = {
    new ObjectMapping20(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]), a12: (String, Mapping[A12]), a13: (String, Mapping[A13]), a14: (String, Mapping[A14]), a15: (String, Mapping[A15]), a16: (String, Mapping[A16]), a17: (String, Mapping[A17]), a18: (String, Mapping[A18]), a19: (String, Mapping[A19]), a20: (String, Mapping[A20]), a21: (String, Mapping[A21]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)]): Mapping[R] = {
    new ObjectMapping21(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]), a12: (String, Mapping[A12]), a13: (String, Mapping[A13]), a14: (String, Mapping[A14]), a15: (String, Mapping[A15]), a16: (String, Mapping[A16]), a17: (String, Mapping[A17]), a18: (String, Mapping[A18]), a19: (String, Mapping[A19]), a20: (String, Mapping[A20]), a21: (String, Mapping[A21]), a22: (String, Mapping[A22]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22)]): Mapping[R] = {
    new ObjectMapping22(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22)
  }

  def list[A](mapping: Mapping[A]): Mapping[List[A]] = RepeatedMapping(mapping)

  import Formats._

  val text: Mapping[String] = of[String]

  val number: Mapping[Int] = of[Int]

  def of[T](implicit binder: Formatter[T]): FieldMapping[T] = FieldMapping[T]()(binder)

}
