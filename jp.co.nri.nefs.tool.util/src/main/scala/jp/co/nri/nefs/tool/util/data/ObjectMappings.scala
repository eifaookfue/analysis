package jp.co.nri.nefs.tool.util.data
import org.apache.poi.ss.usermodel.Row

class ObjectMapping1[R, A1](apply: A1 => R, unapply: R => Option[A1], f1: (String, Mapping[A1]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)

  override def bind(row: Row): Either[Seq[LineError], R] = {
    merge(field1.bind(row)) match {
      case Left(errors) => Left(errors)
      case Right(values) => Right(apply(values(0).asInstanceOf[A1]))
    }
  }

  override def unbind(value: R, row: Row): Unit = {
    val (v1) = unapply(value).get
    field1.unbind(v1, row)
  }

  override def withIndex(index: String): ObjectMapping1[R, A1] = addIndex(index).map(newKey =>
    new ObjectMapping1(apply, unapply, f1, newKey)
  ).getOrElse(this)

}

class ObjectMapping2[R, A1, A2](apply: (A1, A2) => R, unapply: R => Option[(A1, A2)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1).withIndex(key)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1).withIndex(key)

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

  override def withIndex(index: String): ObjectMapping2[R, A1, A2] = addIndex(index).map(newKey =>
    new ObjectMapping2(apply, unapply, f1, f2, newKey)
  ).getOrElse(this)

}

class ObjectMapping3[R, A1, A2, A3](apply: (A1, A2, A3) => R, unapply: R => Option[(A1, A2, A3)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)

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

  override def withIndex(index: String): ObjectMapping3[R, A1, A2, A3] = addIndex(index).map(newKey =>
    new ObjectMapping3(apply, unapply, f1, f2, f3, newKey)
  ).getOrElse(this)

}

class ObjectMapping4[R, A1, A2, A3, A4](apply: (A1, A2, A3, A4) => R, unapply: R => Option[(A1, A2, A3, A4)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)

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

  override def withIndex(index: String): ObjectMapping4[R, A1, A2, A3, A4] = addIndex(index).map(newKey =>
    new ObjectMapping4(apply, unapply, f1, f2, f3, f4, newKey)
  ).getOrElse(this)

}

class ObjectMapping5[R, A1, A2, A3, A4, A5](apply: (A1, A2, A3, A4, A5) => R, unapply: R => Option[(A1, A2, A3, A4, A5)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)

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

  override def withIndex(index: String): ObjectMapping5[R, A1, A2, A3, A4, A5] = addIndex(index).map(newKey =>
    new ObjectMapping5(apply, unapply, f1, f2, f3, f4, f5, newKey)
  ).getOrElse(this)

}

class ObjectMapping6[R, A1, A2, A3, A4, A5, A6](apply: (A1, A2, A3, A4, A5, A6) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)

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

  override def withIndex(index: String): ObjectMapping6[R, A1, A2, A3, A4, A5, A6] = addIndex(index).map(newKey =>
    new ObjectMapping6(apply, unapply, f1, f2, f3, f4, f5, f6, newKey)
  ).getOrElse(this)

}

class ObjectMapping7[R, A1, A2, A3, A4, A5, A6, A7](apply: (A1, A2, A3, A4, A5, A6, A7) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)

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

  override def withIndex(index: String): ObjectMapping7[R, A1, A2, A3, A4, A5, A6, A7] = addIndex(index).map(newKey =>
    new ObjectMapping7(apply, unapply, f1, f2, f3, f4, f5, f6, f7, newKey)
  ).getOrElse(this)

}

class ObjectMapping8[R, A1, A2, A3, A4, A5, A6, A7, A8](apply: (A1, A2, A3, A4, A5, A6, A7, A8) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)

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

  override def withIndex(index: String): ObjectMapping8[R, A1, A2, A3, A4, A5, A6, A7, A8] = addIndex(index).map(newKey =>
    new ObjectMapping8(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, newKey)
  ).getOrElse(this)

}

class ObjectMapping9[R, A1, A2, A3, A4, A5, A6, A7, A8, A9](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)

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

  override def withIndex(index: String): ObjectMapping9[R, A1, A2, A3, A4, A5, A6, A7, A8, A9] = addIndex(index).map(newKey =>
    new ObjectMapping9(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, newKey)
  ).getOrElse(this)

}

class ObjectMapping10[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)

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

  override def withIndex(index: String): ObjectMapping10[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10] = addIndex(index).map(newKey =>
    new ObjectMapping10(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, newKey)
  ).getOrElse(this)

}

class ObjectMapping11[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)
  val field11: Mapping[A11] = f11._2.withIndex(f11._1)

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

  override def withIndex(index: String): ObjectMapping11[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11] = addIndex(index).map(newKey =>
    new ObjectMapping11(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, newKey)
  ).getOrElse(this)

}

class ObjectMapping12[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), f12: (String, Mapping[A12]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)
  val field11: Mapping[A11] = f11._2.withIndex(f11._1)
  val field12: Mapping[A12] = f12._2.withIndex(f12._1)

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

  override def withIndex(index: String): ObjectMapping12[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12] = addIndex(index).map(newKey =>
    new ObjectMapping12(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, newKey)
  ).getOrElse(this)

}

class ObjectMapping13[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), f12: (String, Mapping[A12]), f13: (String, Mapping[A13]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)
  val field11: Mapping[A11] = f11._2.withIndex(f11._1)
  val field12: Mapping[A12] = f12._2.withIndex(f12._1)
  val field13: Mapping[A13] = f13._2.withIndex(f13._1)

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

  override def withIndex(index: String): ObjectMapping13[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13] = addIndex(index).map(newKey =>
    new ObjectMapping13(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, newKey)
  ).getOrElse(this)

}

class ObjectMapping14[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), f12: (String, Mapping[A12]), f13: (String, Mapping[A13]), f14: (String, Mapping[A14]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)
  val field11: Mapping[A11] = f11._2.withIndex(f11._1)
  val field12: Mapping[A12] = f12._2.withIndex(f12._1)
  val field13: Mapping[A13] = f13._2.withIndex(f13._1)
  val field14: Mapping[A14] = f14._2.withIndex(f14._1)

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

  override def withIndex(index: String): ObjectMapping14[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14] = addIndex(index).map(newKey =>
    new ObjectMapping14(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, newKey)
  ).getOrElse(this)

}

class ObjectMapping15[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), f12: (String, Mapping[A12]), f13: (String, Mapping[A13]), f14: (String, Mapping[A14]), f15: (String, Mapping[A15]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)
  val field11: Mapping[A11] = f11._2.withIndex(f11._1)
  val field12: Mapping[A12] = f12._2.withIndex(f12._1)
  val field13: Mapping[A13] = f13._2.withIndex(f13._1)
  val field14: Mapping[A14] = f14._2.withIndex(f14._1)
  val field15: Mapping[A15] = f15._2.withIndex(f15._1)

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

  override def withIndex(index: String): ObjectMapping15[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15] = addIndex(index).map(newKey =>
    new ObjectMapping15(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, newKey)
  ).getOrElse(this)

}

class ObjectMapping16[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), f12: (String, Mapping[A12]), f13: (String, Mapping[A13]), f14: (String, Mapping[A14]), f15: (String, Mapping[A15]), f16: (String, Mapping[A16]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)
  val field11: Mapping[A11] = f11._2.withIndex(f11._1)
  val field12: Mapping[A12] = f12._2.withIndex(f12._1)
  val field13: Mapping[A13] = f13._2.withIndex(f13._1)
  val field14: Mapping[A14] = f14._2.withIndex(f14._1)
  val field15: Mapping[A15] = f15._2.withIndex(f15._1)
  val field16: Mapping[A16] = f16._2.withIndex(f16._1)

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

  override def withIndex(index: String): ObjectMapping16[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16] = addIndex(index).map(newKey =>
    new ObjectMapping16(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, newKey)
  ).getOrElse(this)

}

class ObjectMapping17[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), f12: (String, Mapping[A12]), f13: (String, Mapping[A13]), f14: (String, Mapping[A14]), f15: (String, Mapping[A15]), f16: (String, Mapping[A16]), f17: (String, Mapping[A17]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)
  val field11: Mapping[A11] = f11._2.withIndex(f11._1)
  val field12: Mapping[A12] = f12._2.withIndex(f12._1)
  val field13: Mapping[A13] = f13._2.withIndex(f13._1)
  val field14: Mapping[A14] = f14._2.withIndex(f14._1)
  val field15: Mapping[A15] = f15._2.withIndex(f15._1)
  val field16: Mapping[A16] = f16._2.withIndex(f16._1)
  val field17: Mapping[A17] = f17._2.withIndex(f17._1)

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

  override def withIndex(index: String): ObjectMapping17[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17] = addIndex(index).map(newKey =>
    new ObjectMapping17(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, newKey)
  ).getOrElse(this)

}

class ObjectMapping18[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), f12: (String, Mapping[A12]), f13: (String, Mapping[A13]), f14: (String, Mapping[A14]), f15: (String, Mapping[A15]), f16: (String, Mapping[A16]), f17: (String, Mapping[A17]), f18: (String, Mapping[A18]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)
  val field11: Mapping[A11] = f11._2.withIndex(f11._1)
  val field12: Mapping[A12] = f12._2.withIndex(f12._1)
  val field13: Mapping[A13] = f13._2.withIndex(f13._1)
  val field14: Mapping[A14] = f14._2.withIndex(f14._1)
  val field15: Mapping[A15] = f15._2.withIndex(f15._1)
  val field16: Mapping[A16] = f16._2.withIndex(f16._1)
  val field17: Mapping[A17] = f17._2.withIndex(f17._1)
  val field18: Mapping[A18] = f18._2.withIndex(f18._1)

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

  override def withIndex(index: String): ObjectMapping18[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18] = addIndex(index).map(newKey =>
    new ObjectMapping18(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, newKey)
  ).getOrElse(this)

}

class ObjectMapping19[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), f12: (String, Mapping[A12]), f13: (String, Mapping[A13]), f14: (String, Mapping[A14]), f15: (String, Mapping[A15]), f16: (String, Mapping[A16]), f17: (String, Mapping[A17]), f18: (String, Mapping[A18]), f19: (String, Mapping[A19]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)
  val field11: Mapping[A11] = f11._2.withIndex(f11._1)
  val field12: Mapping[A12] = f12._2.withIndex(f12._1)
  val field13: Mapping[A13] = f13._2.withIndex(f13._1)
  val field14: Mapping[A14] = f14._2.withIndex(f14._1)
  val field15: Mapping[A15] = f15._2.withIndex(f15._1)
  val field16: Mapping[A16] = f16._2.withIndex(f16._1)
  val field17: Mapping[A17] = f17._2.withIndex(f17._1)
  val field18: Mapping[A18] = f18._2.withIndex(f18._1)
  val field19: Mapping[A19] = f19._2.withIndex(f19._1)

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

  override def withIndex(index: String): ObjectMapping19[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19] = addIndex(index).map(newKey =>
    new ObjectMapping19(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, newKey)
  ).getOrElse(this)

}

class ObjectMapping20[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), f12: (String, Mapping[A12]), f13: (String, Mapping[A13]), f14: (String, Mapping[A14]), f15: (String, Mapping[A15]), f16: (String, Mapping[A16]), f17: (String, Mapping[A17]), f18: (String, Mapping[A18]), f19: (String, Mapping[A19]), f20: (String, Mapping[A20]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)
  val field11: Mapping[A11] = f11._2.withIndex(f11._1)
  val field12: Mapping[A12] = f12._2.withIndex(f12._1)
  val field13: Mapping[A13] = f13._2.withIndex(f13._1)
  val field14: Mapping[A14] = f14._2.withIndex(f14._1)
  val field15: Mapping[A15] = f15._2.withIndex(f15._1)
  val field16: Mapping[A16] = f16._2.withIndex(f16._1)
  val field17: Mapping[A17] = f17._2.withIndex(f17._1)
  val field18: Mapping[A18] = f18._2.withIndex(f18._1)
  val field19: Mapping[A19] = f19._2.withIndex(f19._1)
  val field20: Mapping[A20] = f20._2.withIndex(f20._1)

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

  override def withIndex(index: String): ObjectMapping20[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20] = addIndex(index).map(newKey =>
    new ObjectMapping20(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, newKey)
  ).getOrElse(this)

}

class ObjectMapping21[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), f12: (String, Mapping[A12]), f13: (String, Mapping[A13]), f14: (String, Mapping[A14]), f15: (String, Mapping[A15]), f16: (String, Mapping[A16]), f17: (String, Mapping[A17]), f18: (String, Mapping[A18]), f19: (String, Mapping[A19]), f20: (String, Mapping[A20]), f21: (String, Mapping[A21]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)
  val field11: Mapping[A11] = f11._2.withIndex(f11._1)
  val field12: Mapping[A12] = f12._2.withIndex(f12._1)
  val field13: Mapping[A13] = f13._2.withIndex(f13._1)
  val field14: Mapping[A14] = f14._2.withIndex(f14._1)
  val field15: Mapping[A15] = f15._2.withIndex(f15._1)
  val field16: Mapping[A16] = f16._2.withIndex(f16._1)
  val field17: Mapping[A17] = f17._2.withIndex(f17._1)
  val field18: Mapping[A18] = f18._2.withIndex(f18._1)
  val field19: Mapping[A19] = f19._2.withIndex(f19._1)
  val field20: Mapping[A20] = f20._2.withIndex(f20._1)
  val field21: Mapping[A21] = f21._2.withIndex(f21._1)

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

  override def withIndex(index: String): ObjectMapping21[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21] = addIndex(index).map(newKey =>
    new ObjectMapping21(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21, newKey)
  ).getOrElse(this)

}

class ObjectMapping22[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22](apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22) => R, unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22)], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), f12: (String, Mapping[A12]), f13: (String, Mapping[A13]), f14: (String, Mapping[A14]), f15: (String, Mapping[A15]), f16: (String, Mapping[A16]), f17: (String, Mapping[A17]), f18: (String, Mapping[A18]), f19: (String, Mapping[A19]), f20: (String, Mapping[A20]), f21: (String, Mapping[A21]), f22: (String, Mapping[A22]), val key: String = "")
  extends Mapping[R] with ObjectMapping {

  val field1: Mapping[A1] = f1._2.withIndex(f1._1)
  val field2: Mapping[A2] = f2._2.withIndex(f2._1)
  val field3: Mapping[A3] = f3._2.withIndex(f3._1)
  val field4: Mapping[A4] = f4._2.withIndex(f4._1)
  val field5: Mapping[A5] = f5._2.withIndex(f5._1)
  val field6: Mapping[A6] = f6._2.withIndex(f6._1)
  val field7: Mapping[A7] = f7._2.withIndex(f7._1)
  val field8: Mapping[A8] = f8._2.withIndex(f8._1)
  val field9: Mapping[A9] = f9._2.withIndex(f9._1)
  val field10: Mapping[A10] = f10._2.withIndex(f10._1)
  val field11: Mapping[A11] = f11._2.withIndex(f11._1)
  val field12: Mapping[A12] = f12._2.withIndex(f12._1)
  val field13: Mapping[A13] = f13._2.withIndex(f13._1)
  val field14: Mapping[A14] = f14._2.withIndex(f14._1)
  val field15: Mapping[A15] = f15._2.withIndex(f15._1)
  val field16: Mapping[A16] = f16._2.withIndex(f16._1)
  val field17: Mapping[A17] = f17._2.withIndex(f17._1)
  val field18: Mapping[A18] = f18._2.withIndex(f18._1)
  val field19: Mapping[A19] = f19._2.withIndex(f19._1)
  val field20: Mapping[A20] = f20._2.withIndex(f20._1)
  val field21: Mapping[A21] = f21._2.withIndex(f21._1)
  val field22: Mapping[A22] = f22._2.withIndex(f22._1)

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

  override def withIndex(index: String): ObjectMapping22[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22] = addIndex(index).map(newKey =>
    new ObjectMapping22(apply, unapply, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21, f22, newKey)
  ).getOrElse(this)

}
