| プログラム名 | 概要                                                         | 目的                                         |
| ------------ | ------------------------------------------------------------ | -------------------------------------------- |
| Bringin      | キャッシュフォルダの中にある指定された日付以降のファイルが含まれるフォルダをすべてコピーする | 会社少しずつファイルを持ち込むため           |
| Cash2Local   | Cashフォルダにあるpropertiesファイルを探索し、httpのキーワードがあり、sourceではないものに対し、propertiesファイルと同じフォルダにあるivyファイルからartifact情報を抜き出し、buildファイルを生成後、antでivy:installを実行する。<br />IntelliJがファイルをつかんでいるとinstallできない可能性があるので、IntelliJを閉じて実行 | localにファイルを配置するため                |
| FindInvalid  | ivy.xmlにscalVersionが設定されているのにscalaフォルダにないもの、scalaVersionが設定されていないのにscalaフォルダにあるものを抽出、削除する | 昔誤って配置していたモジュールを削除するため |
| DeleteCache  | CacheとLocal両方に保存されているものからCacheにあるものを削除する。IntelliJ起動中はIntelliJがファイルをつかんでいる可能性があるので、いったん閉じてJarを実行する。 | Localで一元管理するため                      |



### scala-langのダウンロードサイト変更

- scala-langに関してはhttp://scala-tools.org/repo-releasesからダウンロードする。詳細は下記。

[Ivy custom url resolver](https://stackoverflow.com/questions/3599319/ivy-custom-url-resolver)



