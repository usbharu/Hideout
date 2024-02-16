# Hideout

HideoutはMastodon互換APIを備えたSNSで、ActivityPubに対応し、KotlinとSpring Frameworkを使用して制作されているソフトウェアです。現在は開発中で、SNSとして主要な機能を備えていますが、セキュリティの問題や不安定なテーブル定義などがあるためホストすることはおすすめしません。

## 特徴

### ActivityPubでつながるネットワーク

ActivityPubを実装しているためMastodonやMisskey、Pleromaとつながることができます。また、ActivityPub以外の分散型の通信プロトコルを実装することがあるかもしれません。

### Mastodon互換API

OAuth2プロバイダーを含めたほとんどのAPIがMastodonとの互換性を持っているため、既存のMastodon クライアントを使用することができます!また、今後Fedibirdやglitch-soc互換のAPIを実装するかもしれません。

## セルフホスト

> [!CAUTION]
> **免責事項**
> 
> 本ソフトウェアを利用して発生したすべての事象に対して開発者は責任を負いません。
> 本ソフトウェアはApache License 2.0を採用しています。

現時点でセルフホストはおすすめしませんが、実験用としてホストすることはできます!

### 使用技術

- **Kotlin** 強力な言語機能でアプリケーションの安全性を高めます。
- **Spring Framework** (Spring Boot/Spring Security/Spring Web/Spring Data) 豊富な機能と堅牢なライブラリでソフトウェアの基幹部分を担います。
- **OpenAPI** スキーマファーストのエンドポイント自動生成はAPIの安定性を高めます。

### 要件

#### 起動

- Java 21
- PostgreSQL 12+
- MongoDB(必須でない) 4.4.x+

#### ビルド

- Java 21
- Gradle 8+

実験用途として、PostgreSQLはH2DB(バンドル)のPostgreSQL互換モードでも問題ありません。

Java 17でもビルド/起動ができますが、サポートしません。

MongoDBを使用しない場合、構成で`hideout.use-mongodb`をfalseにする必要があります。

### ビルド

今後のリリースでビルド済みjarなどを使う場合はスキップしてください。


```bash
gradle bootJar
```

`build/libs/hideout-x.x.x.jar`が生成されます。

### 起動

適切に設定した`application-dev.yml`などをクラスパス上に準備します。

`dev`は`prod`などに置き換えたり、[複数指定すること](https://spring.pleiades.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.core.spring.profiles.active)もできます。

```bash
java -jar build/libs/hideout-x.x.x.jar --spring.profiles.active=dev
```

https://spring.pleiades.io/spring-boot/docs/current/reference/html/getting-started.html#getting-started.first-application.executable-jar.gradle

### 注意事項

本ソフトウェアは開発中です。正常に機能しない場合があります。また、連合先に迷惑をかける事になる可能性があります。DB/設定ファイル/その他リソースなどの利用方法に破壊的な変更が入る可能性があります。
