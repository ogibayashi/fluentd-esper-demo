# What is this? #

2013/2/15のFluentd Casual Talks#2で発表した、"fluentd+Esperで動的ストリームクエリ"のデモプログラムです.

スライドはこちら
http://www.slideshare.net/Ogibayashi/20130215-fluentd-esper2

# Environment #

以下の環境で動作確認しています.
* MacOSX 10.7.5
* fluentd 0.10.27
* Esper 4.6.0

# Setup #

## fluentd ##

1. ZMQ publish pluginのインストール

    git clone https://github.com/ogibayashi/fluent-plugin-zmq-pub.git
    cd fluent-plugin-zmq-pub
    rake build
    fluent-gem install zmq
    fluent-gem install ./pkg/fluent-plugin-zmq-pub-0.0.1.gem --local


2. fluentd.confの設定. 

以下はapacheのaccesslogをin_tailで読み込み、ZeroMQにpublish, Esperの出力は"view.**"というtagでfluentdに飛ばされる場合の例です.

    <source>
      type forward
    </source>
    
    <source>
      type tail
      path /var/log/apache2/access_log
      tag apache.access
      format apache
    </source>
    
    <match apache.access>
      type zmq_pub
      pubkey <%tag%>
      bindaddr tcp://*:5556
      flush_interval 1s
    </match> 
    
    <match view.**>
      type stdout
    </match>


## Esper ##

1. jzmqのインストール

https://github.com/zeromq/jzmq

よりインストール.

MacOS+Homebrewの場合の注意点は以下参照
http://stackoverflow.com/questions/3522248/how-do-i-compile-jzmq-for-zeromq-on-osx

2. デモプログラムのビルド

`git clone https://github.com/ogibayashi/fluentd-esper-demo.git
mvn package
`
# Running demo #

1. fluentdの起動

`$ fluentd`

2. デモプログラムの起動

* java.library.pathはjzmqのライブラリパスを指定
* コマンドライン引数(この場合はapache.access)は本プログラムがfluentdの出力をsubscribeする際のキー. 上記設定の場合、tagと合わせる.

    cd fluentd-esper-demo
    java -Djava.library.path=/usr/local/lib -jar target/fluentd-esper-demo-1.0-SNAPSHOT-jar-with-dependencies.jar apache.access 


3. クエリの発行

src/main/scripts以下にサンプルのクライアントとEPLファイルがあります. クライアントからは、クエリの登録、参照、削除ができます.

    cd src/main/scripts
    # クエリの登録
    ./testclient.rb  count_by_code.epl 
    # 登録されているクエリの一覧表示
    ./testclient.rb -s 
    # クエリの削除
    ./testclient.rb -d count_by_code.epl  


出力は、view.<eplファイル名>というタグでfluentdに送られ、上記設定の場合はfluentdの標準出力に出力されます.

# 各javaクラス概要 #

* EsperMain
  * mainメソッドを提供
* EsperSubscriber
  * 指定された文字列をキーにZeroMQからsubscribeし、Esperに投げるクラス
* EPLServer
  * MessagePack-RPCでクライアントからのリクエストをlistenするクラス
* EPLServerHandler
  * MessagePack-RPCにより呼び出されるメソッド(クエリの登録、一覧、削除)を提供するクラス
  
# Copyright

* Copyright (c) 2013- Hironori Ogibayashi
* License
  * Apache License, Version 2.0
