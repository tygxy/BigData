[TOC]
## Maven学习日记
### mvn常见命令
* 运行测试：mvn test
* 打包：mvn package
* 在本地Repository中安装jar：mvn install
* 清除产生的项目：mvn clean
* 组合使用goal命令，如只打包不测试：mvn -Dtest package
* mvn install -Dmaven.test.skip=true 给任何目标添加maven.test.skip 属性就能跳过测试
* mvn clean install 删除再编译

### setting.xml的具体作用
* 有时候你所在的公司基于安全因素考虑，要求你使用通过安全认证的代理访问因特网。这种情况下，就需要为Maven配置HTTP代理，才能让它正常访问外部仓库，以下载所需要的资源。首先确认自己无法直接访问公共的maven中央仓库，直接运行命令ping repo1.maven.org可以检查网络。
* 需要注意的是，默认情况下，~/.m2/settings.xml文件不存在，用户需要从Maven安装目录复制$M2_HOME/conf/settings.xml文件再进行编辑。
* 配置认证信息和配置远程仓库不同，远程仓库可以直接在pom.xml中配置，但是认证信息必须配置在settings.xml文件中。这是因为pom往往是被提交到代码仓库中供所有成员访问的，而settings.xml一般只存在于本机。因此，在settings.xml中配置认证信息更为安全。
* 用过Maven的都知道，国外的中央仓库用起来太慢了，所以选择一个国内的镜像就很有必要，我推荐国内的阿里云镜像。 阿里云镜像：配置很简单，修改conf文件夹下的settings.xml文件，添加如下镜像配置：
```
<mirrors>
    <mirror>
      <id>alimaven</id>
      <name>aliyun maven</name>
      <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
      <mirrorOf>central</mirrorOf>        
    </mirror>
  </mirrors>
```
但是注意这个mirrorOf会覆盖掉所有，包括你在pom.xml里面定义的repository。

### pom.xml详解
#### pom.xml的开头
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.tengj</groupId>
    <artifactId>springBootDemo1</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>springBootDemo1</name>
</project>
```

* 代码的第一行是XML头，指定了该xml文档的版本和编码方式。
* project是所有pom.xml的根元素，它还声明了一些POM相关的命名空间及xsd元素。
* 根元素下的第一个子元素modelVersion指定了当前的POM模型的版本，对于Maven3来说，它只能是4.0.0
代码中最重要是包含了groupId,artifactId和version了。这三个元素定义了一个项目基本的坐标，在Maven的世界，任何的jar、pom或者jar都是以基于这些基本的坐标进行区分的。
* groupId定义了项目属于哪个组，随意命名，比如谷歌公司的myapp项目，就取名为 com.google.myapp
artifactId定义了当前Maven项目在组中唯一的ID,比如定义hello-world。
* version指定了项目当前的版本0.0.1-SNAPSHOT,SNAPSHOT意为快照，说明该项目还处于开发中，是不稳定的。
* name元素生命了一个对于用户更为友好的项目名称，虽然这不是必须的，但还是推荐为每个POM声明name,以方便信息交流

#### pom.xml的加载中出现了failed to read artifitID
这里还出现了failed to read artifitID的error，后来在stackoverflow上，查阅得知出现这样情况的原因有两种，一种是你的工程是有父类工程的，加载不全，另一种是开始倒入pom.xml的时候延迟过长，伪加载成功，导致external library中没有依赖包。
```
解决手段：是通过在idea的terminal中使用了mvn -U clean install
```
#### maven和idea工程中的比如Scala用的是pom.xml中定义的，还是说idea中选择的Scala路径？

#### ipdatabase的例子，是怎么额外添加依赖包的。
* 1)第一步从命令行进入你想要下载到哪个目录，然后`git clone https://github.com/wzhe06/ipdatabase.git`
* 2)这里的例子，下载的不是一个jar包，而是一个带着pom.xml文件的工程，所以还需要本地打包
* 3）`mvn clean package -DskipTests`本地打包在target里面,这时候你有jar包了
* 4）但是jar包还没有添加到本地仓库里面，你是无法调用的
* 5）安装jar包到自己的Maven仓库中：
`mvn install:install-file -Dfile=路径.jar（你想要安装哪里的jar包到本地仓库.m2/re里面去） -DgroupId=com.ggstar -DartifactId=ipdatabase -Dversion=1.0 -Dpackaging=jar`
* 6)上面是一个例子，最后还有一步，作为一maven项目，是会又resource文件夹的，你有些需要调用的东西在这里面，需要将你下载的IPdatabase中src/resource/ipDatabase.csv ipRegion.xlsx，用cp命令复制到你当前的项目中的resource文件夹中。


#### pom.xml部署到远端仓库
Maven除了能对项目进行编译、测试、打包之外，还能将项目生成的构件部署到远程仓库中。首先，需要编辑项目的pom.xml文件。配置distributionManagement元素
```
<distributionManagement>
        <repository>
            <id>releases</id>
            <name>public</name>
            <url>http://59.50.95.66:8081/nexus/content/repositories/releases</url>
        </repository>
        <snapshotRepository>
            <id>snapshots</id>
            <name>Snapshots</name>
            <url>http://59.50.95.66:8081/nexus/content/repositories/snapshots</url>
        </snapshotRepository>
</distributionManagement>
```

### 仓库
#### 仓库的含义
任何一个构件都有其唯一的坐标，根据这个坐标可以定义其在仓库中的唯一存储路径，这便是Maven的仓库布局方式。
该路经与坐标对应关系为groupId/artifactId/version/artifactId-version.packaging。
举个例子，比如下面这个分页插件依赖如下：
```
<dependency>
      <groupId>com.github.pagehelper</groupId>
      <artifactId>pagehelper-spring-boot-starter</artifactId>
      <version>1.1.0</version>
</dependency>
```
#### 本地仓库
* 本地仓库：本地代码编写时加载的都是在本地的.m2/re里面
* 一般maven项目都是默认从本地仓库开始找的，之前也说了artid,id,version作为一个坐标一样，mvn打包编译或者运行代码的时候，都是先找本地仓库。
* 之前阿里实习的时候，就有mvn的本地仓库问题，我在项目A写了接口，项目B想要调用项目A作为一个依赖包，这时候就需要使用
```
mvn install
```
命令，这样的话，才会进入到本地的.m2/re仓库中去，注意三维坐标的标记，避免冲突。

#### 远端仓库
* 远端仓库：刚开始的时候，本地仓库是没有东西的，需要从远端仓库下载下来，那么就需要中央仓库了，一般的东西都在里面，但也有一些公司内部的仓库，这时候就需要在pom.xml里面配置公司私服仓库的地址，到那里去下载我们需要的构件，有时因为私密性的原因，在pom.xml里面配置私服地址还不够，还需要去setting.xml里面配置账号密码才行。

