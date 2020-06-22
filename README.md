@[TOC](ElasticSearch 7.x with Springboot 2.3.x - 前言)

# ElasticSearch 7.x with Springboot 2.3.x
这是ES应用系列分享的第一部分，目标是基于这几个分享结束，在工程中实际运用ES不再会有大的问题（不可能几篇分享看完就能解决所有问题，生产环境的挑战正是在于情况的复杂，这种复杂也正是乐趣所在）。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200621110108109.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MjI4ODIxOQ==,size_16,color_FFFFFF,t_70#pic_center)

**在分享中我们会掌握如下关键内容**

- ES的一些基础知识及简单实现原理
- ES环境的搭建与准备（本分享基于Docker单节点）
- 如何在Kibana中对ES进行数据操作
- 如何在Springboot的项目中使用ES

**对于完全没有相关知识基础的小伙伴们可以参考文档中的文档进行学习，ES提供的接口以及Springboot与Spring data elasticsearch所做的封装，大幅简化了应用的开发，让应用开发者可以更少关注底层，而将经历更多专注于进行业务的实现，节省下来的时间可以喝一个下午茶并思考如何创新。**

## 环境与工具准备（可根据自己情况与差异替换）

|环境/工具|备注|
| ------------ | ------------ |
| JDK 1.8  |   |
| Intellij IDEA 202001 社区版  |安装Spring Assistant插件|
| Docker Desktop Windows  | 使用docker比较方便。为了降低失败国内最好添加国内镜像库。  |
| ES7.7.1 Docker 镜像  | [文档地址](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started-index.html)  |
| Kibana7.7.1 Docker 镜像  |版本必须与ES一样 [文档地址](https://www.elastic.co/guide/en/kibana/7.7/settings.html)  |
| [Springboot 2.3.1](https://spring.io/projects/spring-boot)  |   |
| [Spring Data Elastic 4.0.1](https://docs.spring.io/spring-data/elasticsearch/docs/4.0.1.RELEASE/reference/html/#elasticsearch.operations)  |pom文件不用专门声明版本号   |

# 前言

*前言部分主要是一些基本概念的简单科普，已经掌握的小伙伴大可直接跳过，也欢迎对于其中的疏漏与不足进行指正。*

## 全文检索
简单来讲，全文检索就是将数据的内容进行分析提取、重新组织，以新的规则进行存储，让用户能通过数据内容的关键字（或者说关键信息），通过内部的算法获取返回用户想要的数据。

这么一看可能有人会问，我在RDBMS使用LIKE关键字不也能有这样的效果吗？从结果来看确实如此，但是使用过LIKE的小伙伴都知道这样的性能是让人捉急的，那么全文技术又是如何解决这个问题的呢？

*上面的谜题我们在接下来大概讲一下反向索引(倒排索引)后一并解密。*

## 反向索引(Inverted index)
在讲概念之前，我们先来看一个例子，你听到一首歌，只记得一句歌词“淡黄的长裙”，然后在搜索引擎里面一搜，通过这一句歌词把这首歌找到了。

这样的场景虽然已经习以为常，但是细品，你细品，你在mysql或oracle里面设计数据库的时候，你会怎么设计？我们可能会把歌名、歌手做成索引，然后我们就能通过歌名、歌手名字快速找到这首歌了。但是这个“淡黄的长裙”好像是歌词。

如果把歌词建成索引？嗯？那样的话我估计DBA的内心是酱婶儿的

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly90aW1nc2EuYmFpZHUuY29tL3RpbWc_aW1hZ2UmcXVhbGl0eT04MCZzaXplPWI5OTk5XzEwMDAwJnNlYz0xNTkyNDg0NzY3NDMzJmRpPTg2Njc4MmEzYWNkYTUyZTk1MWRmZTk0MjA5ZDliYmU1JmltZ3R5cGU9MCZzcmM9aHR0cCUzQSUyRiUyRnBpYy5iYWlxaTAwOC5jb20lMkZ1cGxvYWRzJTJGa3Bwbm9rZHYuanBlZw?x-oss-process=image/format,png)]


那这到底是怎么回事呢？新机子哇一直摸亿多姿，那就是全文检索与反向索引了，我们来看看如何做吧。

![](https://imgconvert.csdnimg.cn/aHR0cDovL3NwaWRlci5ub3Nkbi4xMjcubmV0L2UwODkzZmEzYjhhYTNmODkzZGZlNTdmOGU4ZDZiODRmLmdpZg)

1. 一首歌有歌名和歌词，分别存入数据库；

2. 对歌词根据关键字进行分词，英文默认以空格进行分词，中文默认以一个一个汉字分词（实际应用中文不能以此为逻辑分词，可以使用新的分词器去分词）；

3. 对歌词分词后，针对歌词建立索引（体会一下，和RDBMS的不一样，将一个字段基于关键词建了索引，而不是针对整个字段），索引标注了该关键字出自哪首歌；

4. 到了这里，想一想是不是找到感觉了？领悟了？
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly90aW1nc2EuYmFpZHUuY29tL3RpbWc_aW1hZ2UmcXVhbGl0eT04MCZzaXplPWI5OTk5XzEwMDAwJnNlYz0xNTkyNDg2NDA1MjU3JmRpPTVhZDAxYmUwNDhjMWZhNTlkYjNlNzZjYTI0ZDFiNzYyJmltZ3R5cGU9MCZzcmM9aHR0cCUzQSUyRiUyRjViMDk4OGU1OTUyMjUuY2RuLnNvaHVjcy5jb20lMkZxXzcwJTJDY196b29tJTJDd182NDAlMkZpbWFnZXMlMkYyMDE4MTAyMCUyRjE1NTM2MTM3ZGQ2YTRkMjRhNDk2OTU4MGI2MzI3NTFkLmpwZWc?x-oss-process=image/format,png)

5. 真的懂了吗？(~~是的~~)不，(~~我懂了~~)你不懂。没关系，下面我们来张数据存储到建立索引的简单的流程图帮助理解。

6. ![在这里插入图片描述](https://img-blog.csdnimg.cn/20200621110043194.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MjI4ODIxOQ==,size_16,color_FFFFFF,t_70)

7. 用户输入想要检索的关键字；

8. 数据库对关键字进行词法分析与语法分析
(*这里面的知识很有趣也很专业，想要深入了解的小伙伴建议前往官网查看官方文档*  ---->  [**lucene**](https://lucene.apache.org/))

9. 引擎根据关键字检索索引，再从索引库的文档链表获取对应文档ID；

10. 取出相应文档并根据相关度排序，最终返回结果集给到用户。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200621110043189.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MjI4ODIxOQ==,size_16,color_FFFFFF,t_70)

**存储，词法分析、检索、相关度排序等知识，有兴趣的小伙伴请前往 [官方文档](https://lucene.apache.org/core/documentation.html) 深入研究，这里不做展开。**

到此为止，我想大家对全文检索与反向索引如何进行sao操作已经有个概念了，虽然不了解好像也不影响使用？不管怎么样，上面这段讲解装B装的还可以吧。

![在这里插入图片描述](https://imgconvert.csdnimg.cn/aHR0cHM6Ly90aW1nc2EuYmFpZHUuY29tL3RpbWc_aW1hZ2UmcXVhbGl0eT04MCZzaXplPWI5OTk5XzEwMDAwJnNlYz0xNTkyNzE5NTU5OTY1JmRpPTljMjE1ZDYzNmJlNmVlZWE3MjEzYjcwODg2MGUyOWY2JmltZ3R5cGU9MCZzcmM9aHR0cDovL2ltZy53eGNoYS5jb20vZmlsZS8yMDE5MTIvMDcvZDY3ZGYxNjc0Ni5qcGcjcGljX2NlbnRlcg?x-oss-process=image/format,png#pic_center)

## RESTful
我们为什么在这里强调一下RESTful呢？因为和ES交互，我们基本都是基于其接口进行交互的。
在日常工作中，很多小伙伴可能每天都在写接口，虽然是每天都在做的事，但是对于什么是接口什么是RESTful并没有概念，觉得就是提供一个http调用的地址，从结果上是没错的。那我们为什么还要在设计接口的时候去尽量遵从RESTful规范呢？我相信有小朋友的内心里在说，我的接口和这个RESTful有啥不一样？
我的接口和RESTful ![](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1592804210754&di=72d987f729bb1492cbccf9e419f2a490&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201804%2F09%2F20180409222150_LVRUE.thumb.224_0.jpeg)

首先我们快速理解3个基础概念：

1. REST => Representational State Transfer, 表现层状态转化:

	1. Representational(表现层): 表现层是资源展现的具体形式，通过URI指向一个资源（数据、静态页面、图片），通过状态转换后进行呈现；
	1. State Transfer（状态转换）：用户在客户端输入一个车牌号，然后返回了该车牌号对应的下载地址、简单介绍、和一些截图。计算机处理用户输入，最终返回用户想要的资源的过程就是状态转换。

1. URI => Uniform Resource Identifier, 统一资源标识符：

    我们来看一段URI: cars/sw-331 , 这段URI指向了一个资源的位置，它不关注你要怎么展示它使用它，它就是告诉你这个资源在这里，剩下的你自己看着办。
	
![](https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2856094932,2948188405&fm=26&gp=0.jpg)

1. HTTP基本交互方式：
	- GET: 获取资源
	- POST: 创建资源（注意幂等）
	- PUT: 更新资源（注意幂等）
	- DELETE: 删除资源（注意带上条件，保险一点禁用不指定具体资源的删除，至于是否需要幂等？都已经删了还存在反复鞭shi的可能性吗？）

在这里不对RESTful做太多扩展，虽然看到具体项目中很多使用的非最佳实践，后面可以再来一个关于RESTful的专门介绍，包括建议的实现方式、安全性、并发等等。

这里大家只要基本理解，我们在使用ES时，会通过不同的HTTP交互方式，通过RESTful的URL获取ES中的数据转换成我们想要的展示效果即可（特别是通过kibana操作时），这就像是一种共识，先记住就好。

## ES与其他数据库的简单异同

这里我们从通俗的角度选了几个各自比较有代表性的，为什么是通俗的角度呢？因为严格的角度来说比如ES更偏向于搜索引擎，但是这里放在一起比较是从一般使用者的角度，就像透明性原则一样，你知道你用的是ES就好，ES帮你隔离和包装的底层，就假装视而不见吧。

- MYSQL: 关系型数据的代表，数据字段规整不经常变化，通过SQL进行数据操作，良好的持久化以及对事务的支持。（MYSQL也可以支持全文检索，有兴趣小伙伴可以自己研究）；

- MongoDB：NoSQL数据库的典型代表，是文档型数据库，没有严格的表结构，适合存储灵活变化的数据，但是这些数据不应该具有特别复杂的逻辑关系；

- Redis：KV内存数据库，高性能的读写，简单的基础数据结构，我们对其的使用应尽量保持简单整洁，不要将特别复杂的数据塞入其中，也不要依赖其处理复杂具有复杂逻辑关系的数据；

- Hbase：Hadoop的存储部件，良好的横向扩展使得其具备理论上无限扩展的容量，良好的分片与自我复制使得其能胜任大数据的处理，但是其列族存储的模式以及查询对key的依赖，以及Hadoop生态相对较重，都使得其更适合用于OLAP场景；

- TiDB：这是一个比较新也比较有趣的数据库，对MySQL有良好的支持，同时具备关系型数据库与非关系型数据库的一些特点，同时可用于OLTP与OLAP场景；

- ES：全文检索、反向索引，自动分片，但是其写入性能一般，占用资源比较厉害。

在一般项目中，用MySQL就可以，对于复杂项目，可能会基于不同场景选择不同数据库，这些数据库都很优秀，没有好坏，只是有不同的适用场景而已，在项目中可以酌情选择。

准备好学习什么情况下选用什么数据库了吗？

![](https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1794551523,3031662900&fm=26&gp=0.jpg)

但是什么时候用什么数据库不是这里的重点，我们将要开始对ES的安装与使用了。

## 参考文档
- [ES 7.7](https://www.elastic.co/guide/en/kibana/7.7/settings.html)

- [Kibana 7.7](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started-index.html)

- [Springboot 2.3](https://spring.io/projects/spring-boot)

- [Spring data elasticsearch 4.0.1](https://docs.spring.io/spring-data/elasticsearch/docs/4.0.1.RELEASE/reference/html/#elasticsearch.operations)

# 基础准备

## ES应用场景

ES有很多应用场景，大家最熟悉也最有可能用到的就是搜索引擎、电商商品搜索、用户标签、消息中心、日志查看。下面我们来简单讲讲如何应用：

- 搜索引擎：好像不用讲什么？因为这背后更多的是算法的差异，有两个特别的点就是停用词与高亮词的使用，停用词就是敏感词，放入敏感词你就搜不到了；

- 电商商品搜索：电商搜索栏上有一些固定选项用于缩小筛选范围，如手机，可以先选操作系统，选品牌，选屏幕尺寸，价格区间等，通过filter缩小了选择范围，如果要进一步准确，则通过在搜索框输入的关键字搜索想要的商品，然后还能选择根据什么进行排序，如价格、购买最多；

- 用户标签：一个标签就是一个关键词，我们要通过一个或一组关键词找到一组（个）用户，但是打标签是另一个过程与课题了；

- 消息中心：从消息检索的角度，与搜索引擎、日志查看差不多；

- 日志查看：ELK，graylog，这里讲一下graylog，它通过ES检索到消息的ID，再通过ID到mongo中找到原始数据，有一次和某位CTO聊天，说到我们用的graylog进行的日志搜集与查看，他说他知道的，graylog就是一个简化版的ELK嘛，我只能露出一个尴尬而不失礼貌的微笑。

![](https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3919334208,37253891&fm=26&gp=0.jpg)

## ES的安装

*这里的安装主要是为了方便本地开发，所以使用了docker，我们不讲解过多生产级关于ES的运维事务，甚至如果你在生产购买云服务都不需要关注其如何安装。*

### 配置Docker环境（基于win10）
- 安装Docker Desktop Windows
	- 如果遇到启动时提示未开启Hyper-V，在小娜输入Hyper-V；（点击开始输入Hyper-V也可以）
	- 选中启动或关闭windows功能；
	- 找到Hyper-V相关组件开启，重启电脑即可；

- 配置国内docker hub仓库，否则速度慢还可能拉取失败
	- 在docker/daemon.json中添加内容（也可以直接在Docker dashboard -> Settings -> Docker Engine中修改参数）：


     {
        "registry-mirrors": ["https://registry.docker-cn.com","http://hub-mirror.c.163.com"]
     }

- 重启docker, 可通过 docker info 命令查看刚才配置的镜像地址是否已添加成功。

### ES我来了

- 找到ES的镜像，这里我使用的官方镜像
	- 官方支持很多安装方式，感兴趣的自己 -> [前往选择](https://www.elastic.co/guide/en/elasticsearch/reference/7.x/install-elasticsearch.html)
	
	- 在命令行中拉取ES的docker镜像，你能看到拉取的过程
`docker pull docker.elastic.co/elasticsearch/elasticsearch:7.7.1`

	- 镜像拉取完以后我们简单的试试镜像能否跑起来(这是一个简单的单节点模式，我们也没有修改其暴露端口及参数)
`docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.7.1`

	- 打开浏览器输入 localhost:9200 访问看看，如果是一个如下的json说明你成功了，之所以这里可以使用localhost可以访问是因为虚拟网络为我们做了到容器的映射；
![](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1592830936368&di=8e2484a3292fe807357bb327e3793636&imgtype=0&src=http%3A%2F%2Fimg.yxad.cn%2Fimages%2F20200222%2Ffc7fc23b28f24b569d23fd29ead32781.jpeg)


    {
    "name": "59871db95f53",
    "cluster_name": "docker-cluster",
    "cluster_uuid": "lyaf9jAYRWCETndmKRPiVw",
    "version": {
      "number": "7.7.1",
      "build_flavor": "default",
      "build_type": "docker",
      "build_hash": "ad56dce891c901a492bb1ee393f12dfff473a423",
      "build_date": "2020-05-28T16:30:01.040088Z",
      "build_snapshot": false,
      "lucene_version": "8.5.1",
      "minimum_wire_compatibility_version": "6.8.0",
      "minimum_index_compatibility_version": "6.0.0-beta1"
    },
    "tagline": "You Know, for Search"
    }


- 当然你可能会需要进入容器修改一些参数，可以在容器启动后通过下面命令进入容器，进去以后与你在linux操作没有太多区别，进去后你就可以做一些你爱做的事了，注意安全就好。
`docker exec -i -t {你的容器名} bash`


## Kibana的安装

- 在命令行中拉取Kibana的docker 镜像
`docker pull docker.elastic.co/kibana/kibana:7.7.1`

- 启动kibana的镜像（启动的镜像需要找到我们刚才启动的ES镜像）
`docker run --link eager_kare:elasticsearch -p 5601:5601 docker.elastic.co/kibana/kibana:7.7.1`
*这里简单讲一下 --link参数，--link <name or id>:alias，eager_kare 是我刚才启动的ES容器的名字，冒号后的elasticsearch是kibana容器中配置文件中使用的别名*

- 打开浏览器访问: http://localhost:5601/
这是你应该可以看到Kibana的页面了，就是这么简单。

![](https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=3861360436,1618488301&fm=26&gp=0.jpg)

**到这里，我们的基础准备都已经就绪了，你们最喜欢的实操就要到来了。但是是不是有点累了，想走了？**

![](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1592833503386&di=d70abc295b8a5fea77ddfa1108303cb9&imgtype=0&src=http%3A%2F%2Fimg.wxcha.com%2Ffile%2F201804%2F26%2F0314279062.jpg)

# 通过Kibana操作ES

## 为什么要用Kibana操作ES呢？

1. 你会一直用代码去操作Oracle、MySQL、Mongo、Redis吗？不会，你会找一个工具来方便直接操作数据库，那么用Kibana操作ES就是这么一个工具；

1. 在Kibana里快速了解与实践ES有哪些接口、分别能做什么，加深对其返回数据结构的了解；

# 让Springboot与ES结合

*Springboot 与 Spring data elasticsearch 为我们做了很多封装，有兴趣的小伙伴也可以试试自己直接在java中去操作ES，也不难就是麻烦一些，但是更有利于理解外部系统如何与ES交互。*

*这里我们不涉及这一块主要是在生产环境中我们不太会这么干，能少写代码就少写，不要折腾自己，节约出来的时间与精力可以喝个下午茶，想想怎么创新，好好感受一下这宝贵的一去不复返的人生。*
