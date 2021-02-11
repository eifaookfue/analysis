package jp.co.nri.tcatool.common.upload

import java.nio.charset.Charset
import java.nio.file.Path

case class UploadInformation(input: Path, splitter: String, charSet: Charset, uploadPerCount: Int,
                             startNumber: Int, maxNumberOfLine: Option[Int])

case class MappingInformation(input: Path, splitter: String, charSet: Charset, startNumber: Int)

case class UploadAndMappingInformation(
                                        uploadInformation: UploadInformation,
                                        symbol: MappingInformation,
                                        broker: MappingInformation,
                                        brokerEntity: MappingInformation,
                                        strategy: MappingInformation
                                      )
