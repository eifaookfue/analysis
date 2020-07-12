package jp.co.nri.nefs.tool.util.data
import org.apache.poi.ss.usermodel.Row

class ObjectMapping1[R, A1](apply: A1 => R, unapply: R => Option[A1], f1: (Int, Mapping[A1]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val v1 = unapply(value).get
    field1.unbind(v1, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping1[R, A1] = {
    new ObjectMapping1(apply, unapply, f1, prefix)
  }

}

class ObjectMapping2[R, A1, A2](apply: (A1, A2) => R, unapply: R => Option[(A1, A2)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping2[R, A1, A2] = {
    new ObjectMapping2(apply, unapply, f1, f2, prefix)
  }

}

class ObjectMapping3[R, A1, A2, A3](apply: (A1, A2, A3) => R, unapply: R => Option[(A1, A2, A3)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping3[R, A1, A2, A3] = {
    new ObjectMapping3(apply, unapply, f1, f2, f3, prefix)
  }

}

class ObjectMapping4[R, A1, A2, A3, A4](apply: (A1, A2, A3, A4) => R, unapply: R => Option[(A1, A2, A3, A4)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping4[R, A1, A2, A3, A4] = {
    new ObjectMapping4(apply, unapply, f1, f2, f3, f4, prefix)
  }

}

class ObjectMapping5[R, A1, A2, A3, A4, A5](apply: (A1, A2, A3, A4, A5) => R, unapply: R => Option[(A1, A2, A3, A4, A5)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping5[R, A1, A2, A3, A4, A5] = {
    new ObjectMapping5(apply, unapply, f1, f2, f3, f4, f5, prefix)
  }

}

class ObjectMapping6[R, A1, A2, A3, A4, A5, A6](apply: (A1, A2, A3, A4, A5, A6) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping6[R, A1, A2, A3, A4, A5, A6] = {
    new ObjectMapping6(apply, unapply, f1, f2, f3, f4, f5, f6, prefix)
  }

}

class ObjectMapping7[R, A1, A2, A3, A4, A5, A6, A7](apply: (A1, A2, A3, A4, A5, A6, A7) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping7[R, A1, A2, A3, A4, A5, A6, A7] = {
    new ObjectMapping7(apply, unapply, f1, f2, f3, f4, f5, f6, f7, prefix)
  }

}

class ObjectMapping8[R, A1, A2, A3, A4, A5, A6, A7, A8](apply: (A1, A2, A3, A4, A5, A6, A7, A8) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping8[R, A1, A2, A3, A4, A5, A6, A7, A8] = {
    new ObjectMapping8(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, prefix)
  }

}

class ObjectMapping9[R, A1, A2, A3, A4, A5, A6, A7, A8, A9](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping9[R, A1, A2, A3, A4, A5, A6, A7, A8, A9] = {
    new ObjectMapping9(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, prefix)
  }

}

class ObjectMapping10[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping10[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10] = {
    new ObjectMapping10(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, prefix)
  }

}

class ObjectMapping11[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), f11: (Int, Mapping[A11]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)
  val field11: Mapping[A11] = f11._2.withPrefix(f11._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row), field11.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10], values(10).asInstanceOf[A11]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
    field11.unbind(v11, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping11[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11] = {
    new ObjectMapping11(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, prefix)
  }

}

class ObjectMapping12[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), f11: (Int, Mapping[A11]), f12: (Int, Mapping[A12]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)
  val field11: Mapping[A11] = f11._2.withPrefix(f11._1)
  val field12: Mapping[A12] = f12._2.withPrefix(f12._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row), field11.bind(row), field12.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10], values(10).asInstanceOf[A11], values(11).asInstanceOf[A12]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
    field11.unbind(v11, row)
    field12.unbind(v12, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping12[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12] = {
    new ObjectMapping12(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, prefix)
  }

}

class ObjectMapping13[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), f11: (Int, Mapping[A11]), f12: (Int, Mapping[A12]), f13: (Int, Mapping[A13]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)
  val field11: Mapping[A11] = f11._2.withPrefix(f11._1)
  val field12: Mapping[A12] = f12._2.withPrefix(f12._1)
  val field13: Mapping[A13] = f13._2.withPrefix(f13._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row), field11.bind(row), field12.bind(row), field13.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10], values(10).asInstanceOf[A11], values(11).asInstanceOf[A12], values(12).asInstanceOf[A13]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
    field11.unbind(v11, row)
    field12.unbind(v12, row)
    field13.unbind(v13, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping13[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13] = {
    new ObjectMapping13(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, prefix)
  }

}

class ObjectMapping14[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), f11: (Int, Mapping[A11]), f12: (Int, Mapping[A12]), f13: (Int, Mapping[A13]), f14: (Int, Mapping[A14]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)
  val field11: Mapping[A11] = f11._2.withPrefix(f11._1)
  val field12: Mapping[A12] = f12._2.withPrefix(f12._1)
  val field13: Mapping[A13] = f13._2.withPrefix(f13._1)
  val field14: Mapping[A14] = f14._2.withPrefix(f14._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row), field11.bind(row), field12.bind(row), field13.bind(row), field14.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10], values(10).asInstanceOf[A11], values(11).asInstanceOf[A12], values(12).asInstanceOf[A13], values(13).asInstanceOf[A14]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
    field11.unbind(v11, row)
    field12.unbind(v12, row)
    field13.unbind(v13, row)
    field14.unbind(v14, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping14[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14] = {
    new ObjectMapping14(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, prefix)
  }

}

class ObjectMapping15[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), f11: (Int, Mapping[A11]), f12: (Int, Mapping[A12]), f13: (Int, Mapping[A13]), f14: (Int, Mapping[A14]), f15: (Int, Mapping[A15]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)
  val field11: Mapping[A11] = f11._2.withPrefix(f11._1)
  val field12: Mapping[A12] = f12._2.withPrefix(f12._1)
  val field13: Mapping[A13] = f13._2.withPrefix(f13._1)
  val field14: Mapping[A14] = f14._2.withPrefix(f14._1)
  val field15: Mapping[A15] = f15._2.withPrefix(f15._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row), field11.bind(row), field12.bind(row), field13.bind(row), field14.bind(row), field15.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10], values(10).asInstanceOf[A11], values(11).asInstanceOf[A12], values(12).asInstanceOf[A13], values(13).asInstanceOf[A14], values(14).asInstanceOf[A15]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
    field11.unbind(v11, row)
    field12.unbind(v12, row)
    field13.unbind(v13, row)
    field14.unbind(v14, row)
    field15.unbind(v15, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping15[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15] = {
    new ObjectMapping15(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, prefix)
  }

}

class ObjectMapping16[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), f11: (Int, Mapping[A11]), f12: (Int, Mapping[A12]), f13: (Int, Mapping[A13]), f14: (Int, Mapping[A14]), f15: (Int, Mapping[A15]), f16: (Int, Mapping[A16]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)
  val field11: Mapping[A11] = f11._2.withPrefix(f11._1)
  val field12: Mapping[A12] = f12._2.withPrefix(f12._1)
  val field13: Mapping[A13] = f13._2.withPrefix(f13._1)
  val field14: Mapping[A14] = f14._2.withPrefix(f14._1)
  val field15: Mapping[A15] = f15._2.withPrefix(f15._1)
  val field16: Mapping[A16] = f16._2.withPrefix(f16._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row), field11.bind(row), field12.bind(row), field13.bind(row), field14.bind(row), field15.bind(row), field16.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10], values(10).asInstanceOf[A11], values(11).asInstanceOf[A12], values(12).asInstanceOf[A13], values(13).asInstanceOf[A14], values(14).asInstanceOf[A15], values(15).asInstanceOf[A16]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
    field11.unbind(v11, row)
    field12.unbind(v12, row)
    field13.unbind(v13, row)
    field14.unbind(v14, row)
    field15.unbind(v15, row)
    field16.unbind(v16, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping16[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16] = {
    new ObjectMapping16(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, prefix)
  }

}

class ObjectMapping17[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), f11: (Int, Mapping[A11]), f12: (Int, Mapping[A12]), f13: (Int, Mapping[A13]), f14: (Int, Mapping[A14]), f15: (Int, Mapping[A15]), f16: (Int, Mapping[A16]), f17: (Int, Mapping[A17]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)
  val field11: Mapping[A11] = f11._2.withPrefix(f11._1)
  val field12: Mapping[A12] = f12._2.withPrefix(f12._1)
  val field13: Mapping[A13] = f13._2.withPrefix(f13._1)
  val field14: Mapping[A14] = f14._2.withPrefix(f14._1)
  val field15: Mapping[A15] = f15._2.withPrefix(f15._1)
  val field16: Mapping[A16] = f16._2.withPrefix(f16._1)
  val field17: Mapping[A17] = f17._2.withPrefix(f17._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row), field11.bind(row), field12.bind(row), field13.bind(row), field14.bind(row), field15.bind(row), field16.bind(row), field17.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10], values(10).asInstanceOf[A11], values(11).asInstanceOf[A12], values(12).asInstanceOf[A13], values(13).asInstanceOf[A14], values(14).asInstanceOf[A15], values(15).asInstanceOf[A16], values(16).asInstanceOf[A17]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
    field11.unbind(v11, row)
    field12.unbind(v12, row)
    field13.unbind(v13, row)
    field14.unbind(v14, row)
    field15.unbind(v15, row)
    field16.unbind(v16, row)
    field17.unbind(v17, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping17[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17] = {
    new ObjectMapping17(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, prefix)
  }

}

class ObjectMapping18[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), f11: (Int, Mapping[A11]), f12: (Int, Mapping[A12]), f13: (Int, Mapping[A13]), f14: (Int, Mapping[A14]), f15: (Int, Mapping[A15]), f16: (Int, Mapping[A16]), f17: (Int, Mapping[A17]), f18: (Int, Mapping[A18]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)
  val field11: Mapping[A11] = f11._2.withPrefix(f11._1)
  val field12: Mapping[A12] = f12._2.withPrefix(f12._1)
  val field13: Mapping[A13] = f13._2.withPrefix(f13._1)
  val field14: Mapping[A14] = f14._2.withPrefix(f14._1)
  val field15: Mapping[A15] = f15._2.withPrefix(f15._1)
  val field16: Mapping[A16] = f16._2.withPrefix(f16._1)
  val field17: Mapping[A17] = f17._2.withPrefix(f17._1)
  val field18: Mapping[A18] = f18._2.withPrefix(f18._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row), field11.bind(row), field12.bind(row), field13.bind(row), field14.bind(row), field15.bind(row), field16.bind(row), field17.bind(row), field18.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10], values(10).asInstanceOf[A11], values(11).asInstanceOf[A12], values(12).asInstanceOf[A13], values(13).asInstanceOf[A14], values(14).asInstanceOf[A15], values(15).asInstanceOf[A16], values(16).asInstanceOf[A17], values(17).asInstanceOf[A18]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
    field11.unbind(v11, row)
    field12.unbind(v12, row)
    field13.unbind(v13, row)
    field14.unbind(v14, row)
    field15.unbind(v15, row)
    field16.unbind(v16, row)
    field17.unbind(v17, row)
    field18.unbind(v18, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping18[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18] = {
    new ObjectMapping18(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, prefix)
  }

}

class ObjectMapping19[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), f11: (Int, Mapping[A11]), f12: (Int, Mapping[A12]), f13: (Int, Mapping[A13]), f14: (Int, Mapping[A14]), f15: (Int, Mapping[A15]), f16: (Int, Mapping[A16]), f17: (Int, Mapping[A17]), f18: (Int, Mapping[A18]), f19: (Int, Mapping[A19]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)
  val field11: Mapping[A11] = f11._2.withPrefix(f11._1)
  val field12: Mapping[A12] = f12._2.withPrefix(f12._1)
  val field13: Mapping[A13] = f13._2.withPrefix(f13._1)
  val field14: Mapping[A14] = f14._2.withPrefix(f14._1)
  val field15: Mapping[A15] = f15._2.withPrefix(f15._1)
  val field16: Mapping[A16] = f16._2.withPrefix(f16._1)
  val field17: Mapping[A17] = f17._2.withPrefix(f17._1)
  val field18: Mapping[A18] = f18._2.withPrefix(f18._1)
  val field19: Mapping[A19] = f19._2.withPrefix(f19._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row), field11.bind(row), field12.bind(row), field13.bind(row), field14.bind(row), field15.bind(row), field16.bind(row), field17.bind(row), field18.bind(row), field19.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10], values(10).asInstanceOf[A11], values(11).asInstanceOf[A12], values(12).asInstanceOf[A13], values(13).asInstanceOf[A14], values(14).asInstanceOf[A15], values(15).asInstanceOf[A16], values(16).asInstanceOf[A17], values(17).asInstanceOf[A18], values(18).asInstanceOf[A19]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
    field11.unbind(v11, row)
    field12.unbind(v12, row)
    field13.unbind(v13, row)
    field14.unbind(v14, row)
    field15.unbind(v15, row)
    field16.unbind(v16, row)
    field17.unbind(v17, row)
    field18.unbind(v18, row)
    field19.unbind(v19, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping19[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19] = {
    new ObjectMapping19(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, prefix)
  }

}

class ObjectMapping20[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), f11: (Int, Mapping[A11]), f12: (Int, Mapping[A12]), f13: (Int, Mapping[A13]), f14: (Int, Mapping[A14]), f15: (Int, Mapping[A15]), f16: (Int, Mapping[A16]), f17: (Int, Mapping[A17]), f18: (Int, Mapping[A18]), f19: (Int, Mapping[A19]), f20: (Int, Mapping[A20]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)
  val field11: Mapping[A11] = f11._2.withPrefix(f11._1)
  val field12: Mapping[A12] = f12._2.withPrefix(f12._1)
  val field13: Mapping[A13] = f13._2.withPrefix(f13._1)
  val field14: Mapping[A14] = f14._2.withPrefix(f14._1)
  val field15: Mapping[A15] = f15._2.withPrefix(f15._1)
  val field16: Mapping[A16] = f16._2.withPrefix(f16._1)
  val field17: Mapping[A17] = f17._2.withPrefix(f17._1)
  val field18: Mapping[A18] = f18._2.withPrefix(f18._1)
  val field19: Mapping[A19] = f19._2.withPrefix(f19._1)
  val field20: Mapping[A20] = f20._2.withPrefix(f20._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row), field11.bind(row), field12.bind(row), field13.bind(row), field14.bind(row), field15.bind(row), field16.bind(row), field17.bind(row), field18.bind(row), field19.bind(row), field20.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10], values(10).asInstanceOf[A11], values(11).asInstanceOf[A12], values(12).asInstanceOf[A13], values(13).asInstanceOf[A14], values(14).asInstanceOf[A15], values(15).asInstanceOf[A16], values(16).asInstanceOf[A17], values(17).asInstanceOf[A18], values(18).asInstanceOf[A19], values(19).asInstanceOf[A20]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
    field11.unbind(v11, row)
    field12.unbind(v12, row)
    field13.unbind(v13, row)
    field14.unbind(v14, row)
    field15.unbind(v15, row)
    field16.unbind(v16, row)
    field17.unbind(v17, row)
    field18.unbind(v18, row)
    field19.unbind(v19, row)
    field20.unbind(v20, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping20[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20] = {
    new ObjectMapping20(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, prefix)
  }

}

class ObjectMapping21[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), f11: (Int, Mapping[A11]), f12: (Int, Mapping[A12]), f13: (Int, Mapping[A13]), f14: (Int, Mapping[A14]), f15: (Int, Mapping[A15]), f16: (Int, Mapping[A16]), f17: (Int, Mapping[A17]), f18: (Int, Mapping[A18]), f19: (Int, Mapping[A19]), f20: (Int, Mapping[A20]), f21: (Int, Mapping[A21]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)
  val field11: Mapping[A11] = f11._2.withPrefix(f11._1)
  val field12: Mapping[A12] = f12._2.withPrefix(f12._1)
  val field13: Mapping[A13] = f13._2.withPrefix(f13._1)
  val field14: Mapping[A14] = f14._2.withPrefix(f14._1)
  val field15: Mapping[A15] = f15._2.withPrefix(f15._1)
  val field16: Mapping[A16] = f16._2.withPrefix(f16._1)
  val field17: Mapping[A17] = f17._2.withPrefix(f17._1)
  val field18: Mapping[A18] = f18._2.withPrefix(f18._1)
  val field19: Mapping[A19] = f19._2.withPrefix(f19._1)
  val field20: Mapping[A20] = f20._2.withPrefix(f20._1)
  val field21: Mapping[A21] = f21._2.withPrefix(f21._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row), field11.bind(row), field12.bind(row), field13.bind(row), field14.bind(row), field15.bind(row), field16.bind(row), field17.bind(row), field18.bind(row), field19.bind(row), field20.bind(row), field21.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10], values(10).asInstanceOf[A11], values(11).asInstanceOf[A12], values(12).asInstanceOf[A13], values(13).asInstanceOf[A14], values(14).asInstanceOf[A15], values(15).asInstanceOf[A16], values(16).asInstanceOf[A17], values(17).asInstanceOf[A18], values(18).asInstanceOf[A19], values(19).asInstanceOf[A20], values(20).asInstanceOf[A21]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
    field11.unbind(v11, row)
    field12.unbind(v12, row)
    field13.unbind(v13, row)
    field14.unbind(v14, row)
    field15.unbind(v15, row)
    field16.unbind(v16, row)
    field17.unbind(v17, row)
    field18.unbind(v18, row)
    field19.unbind(v19, row)
    field20.unbind(v20, row)
    field21.unbind(v21, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping21[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21] = {
    new ObjectMapping21(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21, prefix)
  }

}

class ObjectMapping22[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22)], f1: (Int, Mapping[A1]), f2: (Int, Mapping[A2]), f3: (Int, Mapping[A3]), f4: (Int, Mapping[A4]), f5: (Int, Mapping[A5]), f6: (Int, Mapping[A6]), f7: (Int, Mapping[A7]), f8: (Int, Mapping[A8]), f9: (Int, Mapping[A9]), f10: (Int, Mapping[A10]), f11: (Int, Mapping[A11]), f12: (Int, Mapping[A12]), f13: (Int, Mapping[A13]), f14: (Int, Mapping[A14]), f15: (Int, Mapping[A15]), f16: (Int, Mapping[A16]), f17: (Int, Mapping[A17]), f18: (Int, Mapping[A18]), f19: (Int, Mapping[A19]), f20: (Int, Mapping[A20]), f21: (Int, Mapping[A21]), f22: (Int, Mapping[A22]), index: Int = 0)
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)
  val field3: Mapping[A3] = f3._2.withPrefix(f3._1)
  val field4: Mapping[A4] = f4._2.withPrefix(f4._1)
  val field5: Mapping[A5] = f5._2.withPrefix(f5._1)
  val field6: Mapping[A6] = f6._2.withPrefix(f6._1)
  val field7: Mapping[A7] = f7._2.withPrefix(f7._1)
  val field8: Mapping[A8] = f8._2.withPrefix(f8._1)
  val field9: Mapping[A9] = f9._2.withPrefix(f9._1)
  val field10: Mapping[A10] = f10._2.withPrefix(f10._1)
  val field11: Mapping[A11] = f11._2.withPrefix(f11._1)
  val field12: Mapping[A12] = f12._2.withPrefix(f12._1)
  val field13: Mapping[A13] = f13._2.withPrefix(f13._1)
  val field14: Mapping[A14] = f14._2.withPrefix(f14._1)
  val field15: Mapping[A15] = f15._2.withPrefix(f15._1)
  val field16: Mapping[A16] = f16._2.withPrefix(f16._1)
  val field17: Mapping[A17] = f17._2.withPrefix(f17._1)
  val field18: Mapping[A18] = f18._2.withPrefix(f18._1)
  val field19: Mapping[A19] = f19._2.withPrefix(f19._1)
  val field20: Mapping[A20] = f20._2.withPrefix(f20._1)
  val field21: Mapping[A21] = f21._2.withPrefix(f21._1)
  val field22: Mapping[A22] = f22._2.withPrefix(f22._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row), field2.bind(row), field3.bind(row), field4.bind(row), field5.bind(row), field6.bind(row), field7.bind(row), field8.bind(row), field9.bind(row), field10.bind(row), field11.bind(row), field12.bind(row), field13.bind(row), field14.bind(row), field15.bind(row), field16.bind(row), field17.bind(row), field18.bind(row), field19.bind(row), field20.bind(row), field21.bind(row), field22.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1], values(1).asInstanceOf[A2], values(2).asInstanceOf[A3], values(3).asInstanceOf[A4], values(4).asInstanceOf[A5], values(5).asInstanceOf[A6], values(6).asInstanceOf[A7], values(7).asInstanceOf[A8], values(8).asInstanceOf[A9], values(9).asInstanceOf[A10], values(10).asInstanceOf[A11], values(11).asInstanceOf[A12], values(12).asInstanceOf[A13], values(13).asInstanceOf[A14], values(14).asInstanceOf[A15], values(15).asInstanceOf[A16], values(16).asInstanceOf[A17], values(17).asInstanceOf[A18], values(18).asInstanceOf[A19], values(19).asInstanceOf[A20], values(20).asInstanceOf[A21], values(21).asInstanceOf[A22]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22) = unapply(value).get
    field1.unbind(v1, row)
    field2.unbind(v2, row)
    field3.unbind(v3, row)
    field4.unbind(v4, row)
    field5.unbind(v5, row)
    field6.unbind(v6, row)
    field7.unbind(v7, row)
    field8.unbind(v8, row)
    field9.unbind(v9, row)
    field10.unbind(v10, row)
    field11.unbind(v11, row)
    field12.unbind(v12, row)
    field13.unbind(v13, row)
    field14.unbind(v14, row)
    field15.unbind(v15, row)
    field16.unbind(v16, row)
    field17.unbind(v17, row)
    field18.unbind(v18, row)
    field19.unbind(v19, row)
    field20.unbind(v20, row)
    field21.unbind(v21, row)
    field22.unbind(v22, row)
  }

  override def withPrefix(prefix: Int): ObjectMapping22[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22] = {
    new ObjectMapping22(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21, f22, prefix)
  }

}

