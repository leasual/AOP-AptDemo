#### AOP-AptDemo
###use apt to create a common file.  

####dependency:
#####in root build.gradle file add</br>
dependencies {</br>
  &emsp;&emsp;classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'</br>
}</br>

#####in app build.gradle file add</br>
apply plugin: 'com.neenbedankt.android-apt'</br>
ext {</br>
    &emsp;&emsp;oltu_version = "1.0.1"</br>
    &emsp;&emsp;retrofit_version = "2.0.2"</br>
    &emsp;&emsp;swagger_annotations_version = "1.5.8"</br>
    &emsp;&emsp;junit_version = "4.12"</br>
    &emsp;&emsp;rx_java_version = "1.1.3"</br>
    &emsp;&emsp;rx_android_version = "1.2.1"</br>
    &emsp;&emsp;jodatime_version = "2.9.3"</br>
    &emsp;&emsp;target = "android"</br>
    &emsp;&emsp;okhttp_version = "3.6.0"</br>
}</br>
dependencies {</br>
    &emsp;&emsp;compile "com.squareup.okhttp3:okhttp:$okhttp_version"</br>
    &emsp;&emsp;compile "com.squareup.okhttp3:logging-interceptor:$okhttp_version"</br>
    &emsp;&emsp;compile "com.squareup.retrofit2:retrofit:$retrofit_version"</br>
    &emsp;&emsp;compile "com.squareup.retrofit2:converter-scalars:$retrofit_version"</br>
    &emsp;&emsp;compile "com.squareup.retrofit2:converter-gson:$retrofit_version"</br>
    &emsp;&emsp;compile "com.squareup.retrofit2:adapter-rxjava:$retrofit_version"</br>
    &emsp;&emsp;compile "io.reactivex:rxjava:$rx_java_version"</br>
    &emsp;&emsp;compile "io.reactivex:rxandroid:$rx_android_version"</br>
    &emsp;&emsp;apt project(':compiler')</br>
    &emsp;&emsp;compile project(':annotation')</br>
}</br>

####Use Tips:
project should have HttpService.class,then you should change the line:   
#####"ClassName.get("com.example.swaggerdemo.network", "HttpService")"
