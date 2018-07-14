# 《快速上手Linux玩转典型应用》

以Centos为例

## ssh

- 服务器安装ssh服务
	- 安装ssh yum install openssh-server
	- 启动ssh service sshd start
	- 设置开机启动 chkconfig sshd on

- 客户端安装ssh服务
	- 安装ssh yum install openssh-clients

- 常见ssh命令
	- 连接 ssh root@ip
	- 配置config，方便连接
		- cd ~/.ssh
		- touch config
		- 编辑config
		```
		host "dianxin"
		    HostName ip
		    User root
		    Port 22
		```
- 免秘钥登陆服务器
	- 生成公钥私钥 ssh-keygen 
	- 加载私钥 ssh-add ~/.ssh/私钥名称
	- 在服务器的 ~/.ssh/authorized_keys下存放生成的公钥

- 修改ssh端口 修改/etc/ssh/sshd_config中的port，然后service sshd restart重启服务

## 常见命令

- 软件操作命令
	- 软件包管理器 yum
		- 安装 yum install xx
		- 卸载 yum remove xxx
		- 搜索 yum serach xxx
		- 清缓存 yum clean packages
		- 列表 yum list
		- 软件包信息 yum info xxx

- 服务器硬件资源信息命令
	- 内存 free -m
	- 硬盘 df -h 
	- 负载 w or top
	- cpu cat /proc/cpuinfo

- 文件操作命令
	- vim
		- 移动到全文首行 gg
		- 移动到全文尾行 G
		- 删行 dd
		- 撤销 u
		- 复制一行 yy
		- 粘贴 p
	- 文件权限 rwx(4+2+1)
		- chown -R immoc:immoc /data   // 递归将/data文件夹的数据用户组改为immoc
	- grep查找关键字 
		- grep -n "guoxingyu" test // 在test文件中查找guoxingyu，-n显示行号
		- ps -ef | grep httpd  // 查看httpd服务有没有启动
	- find 
		- find . -name "*.txt"
		- find /xx/xx -type f
	- wc统计次数
		- grep "111" test | wc -l 
	- 压缩/解压缩
		- tar -cvf xx.tar xx
		- tar -xvf xx.tar
		- tar -czvf xx.tar.gz xx
		- tar -xzvf xx.tar.gzs

- 系统用户操作命令
	- 添加用户  useradd xxx
	- 删除用户  userdel xxx
	- 设置密码  passwd xxx

- 防火墙设置
	- 安装 yum install firewalld
	- 启动 service firewalld start
	- 检查状态 service firewalld status
	- 关闭 service firewalld stop

- 提权 sudo + command 

- 文件传递 
	-上传文件 scp [-P port] file root@ip:/path      
	-下载到本地 scp [-P port] root@ip:/path/file  ./  

## WebServer
- Apache
	- 安装 yum install httpd
	- 启动 service httpd start
	- 停止 service httpd stop
	- 虚拟主机
	- 伪静态

- Nginx

## Mysql

- 下载源
	- wget xxx
	- yum localinstall xxx

- 基本操作
	- yum install mysql-community-server
	- service mysqld start/restart/stop

- 查看默认密码
	- cat /var/log/mysqld.log | grep password

- 登陆
	- mysql -h127.0.0.1 -uroot -p

- 开启远程连接
	- 连接数据库
	- use mysql
	- update user set host = '%' where user = 'root' and host = 'localhost';  // 给某个User,IP开启所有IP均可以访问
	- flush privileges
	- 前提是防火墙需要关闭 sudo service firewalld stop

- 添加用户和授权
	- 连接数据库
	- create user 'userName' @ '%' identified by 'passwd'; 
	- 赋权
		- grant all privileges on *.* to 'userName'@'%' identified by 'passwd' with grant option; // 赋予该账户所有库的权限 *.*就是所有库的所有表
		- grant select,insert,update on *.* to 'userName'@'%' identified by 'passwd' with grant option; // 赋予查，插，更新权限
		- revoke all privileges on *.* from 'userName'; // 收回权限
	- flush privileges


## Redis

- 安装
	- wget http://xxxx
	- sudo yum install gcc
	- make malloc=lib
	- sudo make install

- 启动
	- ./src/redis-server 

- 常见操作参考Redis

## Git

- 安装 yum install git

- 添加公钥至github

- 常见命令
	- git clone XXX
	- git remote -v  // 查看git的源，可以是github等
	- git status
	- git commit -am 'modify readme'














