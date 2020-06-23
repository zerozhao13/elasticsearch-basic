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

1. 你会一直用代码去操作Oracle、MySQL、Mongo、Redis吗？
不会，你会找一个工具来方便直接操作数据库，那么用Kibana操作ES就是这么一个工具；

1. 在Kibana里快速了解与实践ES有哪些接口、分别能做什么，加深对其功能与数据结构的了解；

## 找到Kibana的ES操作面板
- 在浏览器访问你的Kibana地址(我本机是 localhost:5601)；
- 在左侧导航菜单找到Dev Tools并点击；
- 看到一个左右分栏的页面, 现在我们在左分页输入一个指令看看
`GET /_cat/health?v`
这是查看ES的健康状况，如果使用的是单节点，状态会始终是 yellow 不会是 green。想要绿还得多节点运动。

## 创建一个索引

### 直接插入一条数据来自动创建索引

- 当插入一条数据时，如果ES原本没有这个index，系统默认会自动创建该索引，我们来试试：

```javascript
PUT /customer/_doc/1
{
  "name": "乔尔",
  "des": "最后生还者的主角",
  "game": "The last of us"
}
```
你会得到下面的返回：

```javascript
{
  "_index" : "customer",
  "_type" : "_doc",
  "_id" : "1",
  "_version" : 2,
  "result" : "created",
  "_shards" : {
    "total" : 2,
    "successful" : 1,
    "failed" : 0
  },
  "_seq_no" : 1,
  "_primary_term" : 1
}
```
- 我们来查询一下该数据是否已存在：

```javascript
GET customer/_doc/1
```
返回回来了：

```javascript
{
  "_index" : "customer",
  "_type" : "_doc",
  "_id" : "1",
  "_version" : 2,
  "_seq_no" : 1,
  "_primary_term" : 1,
  "found" : true,
  "_source" : {
    "name" : "乔尔",
    "des" : "最后生还者的主角",
    "game" : "The last of us"
  }
}
```
是的，老乔尔在这里了，还没有被干掉，向乔尔敬个礼，最后生还者2的编剧夹带了太多私货，还有天理吗？还有王法吗？这样做你的良心不会痛吗？
不能像我一样做个不加带私货，就单纯讲讲技术~~（私货）~~ 的人吗。

![在这里插入图片描述](https://imgconvert.csdnimg.cn/aHR0cHM6Ly90aW1nc2EuYmFpZHUuY29tL3RpbWc_aW1hZ2UmcXVhbGl0eT04MCZzaXplPWI5OTk5XzEwMDAwJnNlYz0xNTkyODk0Mjg4NDUwJmRpPTZjOWQ1ZDJhNzBiZDBkNTBlNGY4ZTViYzc4NGYyNzRkJmltZ3R5cGU9MCZzcmM9aHR0cDovL3d3dy5rZWRvLmdvdi5jbi91cGxvYWQvcmVzb3VyY2VzL2ltYWdlLzIwMTUvMDgvMTgvOTg1MjYuanBnI3BpY19jZW50ZXI?x-oss-process=image/format,png#pic_center)

 - 使用过旧版ES的老司机会纠结type，在ES7开始已经取消了type，_doc在API里也是一个关键字，类似效果的还有下面这个API：
 

```javascript
//将_doc替换成了_create, 再次提交该请求时，会提示该document已存在
PUT /customer/_create/2
{
  "name": "艾莉",
  "des": "最后生还者的女主角",
  "game": "The last of us"
}
```
是不是很方便，不用像关系型数据库一样还要建表，直接插入数据就表也有了，数据也有了，有了这样的电动车还要啥自行车？

![在这里插入图片描述](https://imgconvert.csdnimg.cn/aHR0cHM6Ly90aW1nc2EuYmFpZHUuY29tL3RpbWc_aW1hZ2UmcXVhbGl0eT04MCZzaXplPWI5OTk5XzEwMDAwJnNlYz0xNTkyODk1ODM4NTU0JmRpPTY5MDA4ZmQ1ZDM5YmEwZmFhNjQ1M2QzNWNlNTdkMTQ3JmltZ3R5cGU9MCZzcmM9aHR0cDovL3AyLnNzbC5jZG4uYnRpbWUuY29tL3QwMTkzZWY1MTRlZTlmMmRjMGMuZ2lmP3NpemU9MjQweDI0MA)
自行车还是要的，毕竟电动车还要电，没电的时候怎么办呢？难道用爱发电？不要骗自己了，程序员哪里有爱？所以我们一起来看看自行车吧。

### 正儿八经创建一个index
之前怎么就不正儿八经了？
![在这里插入图片描述](https://imgconvert.csdnimg.cn/aHR0cHM6Ly90aW1nc2EuYmFpZHUuY29tL3RpbWc_aW1hZ2UmcXVhbGl0eT04MCZzaXplPWI5OTk5XzEwMDAwJnNlYz0xNTkyODk2MTczNzUxJmRpPWJiYzcwNDUwOGFlMDYwOGMzZDFkMTY4OTEyNTk5MDBiJmltZ3R5cGU9MCZzcmM9aHR0cDovL2ltZy5pdGx1bi5jbi91cGxvYWRzL2FsbGltZy8xNjExMTUvMS0xNjExMTUxOTMxNTQtbHAuanBn?x-oss-process=image/format,png)
ES的数据存入后，还记得前言中数据入库到建立索引的过程吗？一大精华就是如何将你的数据建立索引，如果你不告诉ES，他是不知道的，那么就只能自己匹配了，在简单的应用场景下没有什么问题，可是如果想把你的电动车玩成酷炫的改装痛车，那么就要为你的每个零件做定制，现在我们就先来看看如何为不同字段指定类型。

我们先删除刚才创建的index：

```javascript
DELETE /customer
```

接下来我们重新建立索引 character：

```javascript
PUT /character
{
    "settings" : {
        "number_of_shards" : 1
    },
    "mappings" : {
        "properties" : {
            "name" : { "type" : "keyword" },
            "des" : { "type" : "text" },
            "game" : { "type" : "text" },
            "age" : { "type" : "integer" }
        }
    }
}
```
上面的命令让我们建立了一个新的index -> character，其中的mappings下的properties，我们指定了字段的属性，我们将name的属性设为keyword，那么它将不会进行分词，而是整个被索引，这就和其他数据库的索引在效果上一样了。

ES有以下这些基本类型：

#### 核心数据类型：

 - string
text, keyword, wildcard
 - Numeric
long, integer, short, byte, double, float, half_float, scaled_float
 - Date
date
 - Date nanoseconds
date_nanos
 - Boolean
boolean
 - Binary
binary
 - Range
integer_range, float_range, long_range, double_range, date_range, ip_range

#### 复合数据类型：

 - Object
单个json类型
 - Nested
一组嵌套的json类型

#### 地理位置数据类型：

 - Geo-point
geo_point 经纬度坐标点
 - Geo-shape
geo_shape 一个多边形区域

## 对index干点别的吧
### 克隆索引
克隆的命令很简单，不过有一些注意事项，克隆只需要这样就可以：

```javascript
// 将character克隆到character2
POST /character/_clone/character2
```
**这里要注意一些前提条件**

 1. 你的ES节点健康度必须是绿的，不绿不行；
 2. 将现有index变为只读，如何变为只读呢？
 

```javascript
PUT /character/_settings
{
  "settings": {
    "index.blocks.write": true
  }
}
```
### 删除与关闭index
#### 删除
当你想抹去某个index的存在时，就使用该命令，如果你的原始数据在别的库，ES仅仅作为一个索引库时，该操作影响相对有限，否则那就真的是从删库到跑路了：

```javascript
DELETE /character
```
![在这里插入图片描述](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9waWMucm1iLmJkc3RhdGljLmNvbS8wYmE2OGE1ZTBjYWM1ZThlMTEzNDUxY2Y3YTNkNDc0OC5qcGVnQHdtXzIsdF81NW0rNWE2MjVZKzNMK2FXbk9hZG9PUzRsdWVWakE9PSxmY19mZmZmZmYsZmZfVTJsdFNHVnAsc3pfMTEseF83LHlfNw?x-oss-process=image/format,png)
#### 关闭
当你想要保留某个index又不想人读写它时，就用该命令：

```javascript
POST /character/_close
```

## 查看index
如果已经建立了index，我们自然想要看看这个index到底存在不存在，到底长啥样，对吧。
### 我有多少index
在kibana中输入下面命令看看你刚才创建的index在吗？没有建或者已删库？

```javascript
GET _cat/indices
```
在里面看到你的index了吗？
### 查看具体某个index
现在你想知道某个具体的index，看看他的properties，又几个副本，什么时候创建的，以及是否有特殊settings？这个命令也很简单：

```javascript
GET character
```
你也可以用如下命令来判断某个index是否存在，如果存在会返回200，不存在则返回404。

```javascript
HEAD character
```
## 是时候来点document了
### 自带ID的数据插入
我们在做数据插入时，可以为数据指定ID，这样系统在存入数据时ID就会是我们传入的值，我们来看看怎么做

```javascript
//PUT /{index_name}/_doc/{id}
PUT /character/_doc/2
{
  "name": "艾莉",
  "des": "最后生还者的女主角",
  "game": "The last of us 2",
  "age": 16
}
```
这里的返回数据会告诉我们，数据的ID为2，那么我们如果不指定ID呢？不指定的时候会怎样呢？让我们来看看，老乔尔走起：
### 自动生成ID
```javascript
//这里我们将请求方式改为了POST，因为这里一定是创建，URI最后没有ID
POST /character/_doc/
{
  "name" : "乔尔",
  "des" : "最后生还者的主角",
  "game" : "The last of us",
  "age": 46
}
```
我们来看看返回数据：

```javascript
{
  "_index" : "character",
  "_type" : "_doc",
  "_id" : "Rpvl33IB_-zdUMCBv_pD",
  "_version" : 1,
  "result" : "created",
  "_shards" : {
    "total" : 2,
    "successful" : 1,
    "failed" : 0
  },
  "_seq_no" : 1,
  "_primary_term" : 1
}
```
ID是一串随机生成的字符串，当然我们可以修改ID的激活规则，自动生成ID的规则由settings中的action.auto_create_index控制：

```javascript
// 这个命令表示character使用自动生成ID，以customer开头的关闭自动生成ID，以user开头的使用自增ID
PUT _cluster/settings
{
    "persistent": {
        "action.auto_create_index": "character,-customer*,+user*" 
    }
}

PUT _cluster/settings
{
    "persistent": {
        "action.auto_create_index": "false" 
    }
}

PUT _cluster/settings
{
    "persistent": {
        "action.auto_create_index": "true" 
    }
}
```
### 添加超时时间
ES的写入效率并不是特别高，在实际生产环境中，我们总要考虑网络IO的耗时以及其所带来的线程占用及可能的应用崩溃，而在微服务中不考虑这样的问题甚至可能造成系统的雪崩，所以我们希望系统能够快速失败，失败后我们可以重试也可以选择其他降级方案，ES也为我们提供了这样的能力，可以让我们快速失败：

```javascript
//我们在URI后添加了timeout，如果3s还没响应则会返回我们超时
POST character/_doc?timeout=3s
{
  "name": "艾比",
  "des": "最后生还者2的反派，但是你却不得不操作她去杀主角，WTF。",
  "game": "The last of us 2",
  "age": 20
}
```
## 简单查询
### 看看这个index下的所有documents
我们有时候就想随便看看一个数据库表中的一些数据，我们对于数据的感觉常常要看到这些数据才会有，这时候我们要怎么做呢？

```javascript
GET character/_search
```
不带任何参数，是不是很简单。
### 查看具体ID的数据

```javascript
//Rpvl33IB_-zdUMCBv_pD : 乔尔的ID
//返回乔尔的document
GET character/_doc/Rpvl33IB_-zdUMCBv_pD
//判断乔尔是否存在
HEAD character/_doc/Rpvl33IB_-zdUMCBv_pD
//只返回乔尔的数据本身
GET character/_source/Rpvl33IB_-zdUMCBv_pD
```

### 只返回特定字段
这时我们只要在调用时添加要返回的source字段即可：

```javascript
//我们只要知道乔尔的名字以及出自哪个游戏
GET character/_source/Rpvl33IB_-zdUMCBv_pD?_source=name,game
```
现在我们已经会根据ID查询了，虽然和想象的不太一样，因为我们想要的不应该是用ID来查询，我们想要根据关键字就找到数据。

不要急，我们要循序渐进让你找到感觉，这种感觉就是在轮环中前进，接下来让我们看看更高级的用于搜索的奇巧yinji（哎呀我这输入法怎么坏了打不出来了）
![在这里插入图片描述](https://imgconvert.csdnimg.cn/aHR0cHM6Ly90aW1nc2EuYmFpZHUuY29tL3RpbWc_aW1hZ2UmcXVhbGl0eT04MCZzaXplPWI5OTk5XzEwMDAwJnNlYz0xNTkyOTA5OTg0NjcyJmRpPTA3OGQxZTE2ZmM4OGNjZWI5Y2QzMzJjMzY0NjU5OWFmJmltZ3R5cGU9MCZzcmM9aHR0cDovL3A0LnNzbC5jZG4uYnRpbWUuY29tL3QwMTc1Mzk4ZTBkMDZhYjIyNjcuZ2lmP3NpemU9Mzk5eDE3MQ)
憋说话，开搞！
## 复杂搜索
### 特定属性值
我们想要找到乔尔，但是我们并不知道他的ID，我们只记住了他的名字，可怜的老乔尔，让我们试试找到他吧。

```javascript
//我们之前用过这个_search，现在我们加上了一个新参数 -> q=name:乔尔
GET character/_search?q=name:乔尔&_source=name,game
```
我们找到乔尔了，但是这样是不是会感觉比较乱？那么我们来这样试试：

```javascript
GET /character/_search
{
  "query": {
    "term": {
      "name": "乔尔"
    }
  }
}
```
**以前有小伙伴质疑过这样的请求不是应该是POST吗? GET要跟在URI后面，其实不是这样的。**

### 对返回数据做分页与排序
我们要找到所有描述中带“最”字的数据，然后根据年轻倒序排序，并返回前两个，我们来看看怎么做吧：

```javascript
GET /character/_search
{
  "query": {
    "match": {
      "des": "最"
    }
  },
  "sort": [{"age": "desc"}], 
  "from": 0,
  "size": 2
}
```
### 复杂查询条件
上面我们的查询条件还是比较单一，要知道再SQL里面我们可以有各种 AND, OR, NOT 以及括号操作，在这里我们要怎么办呢？
我想要描述里带最的数据，同时年龄要大于16岁小于等于46岁的，怎么办呢？居然还有这么骚的操作？
![在这里插入图片描述](https://imgconvert.csdnimg.cn/aHR0cHM6Ly90aW1nc2EuYmFpZHUuY29tL3RpbWc_aW1hZ2UmcXVhbGl0eT04MCZzaXplPWI5OTk5XzEwMDAwJnNlYz0xNTkyOTEzMjAwMDIyJmRpPTk0N2Q4MzI4NDUwZDU2MGNmNTI5NjM0OGZiZGM5ODZiJmltZ3R5cGU9MCZzcmM9aHR0cDovLzViMDk4OGU1OTUyMjUuY2RuLnNvaHVjcy5jb20vaW1hZ2VzLzIwMTkwNTIyL2NmOWU3MzFmMGQ0YTRiZmU4YTk2YmYwMWY2MjE3YTFkLmpwZWc?x-oss-process=image/format,png)
那么我们来看看怎么在查询中满足要求：

```javascript
GET /character/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "des": "最后"
          }
        }
      ],
      "filter": [
        {
          "range": {
            "age": {
              "gt": 16,
              "lte": 46
            }
          }
        }  
      ]
    }
  },
  "sort": [{"age": "desc"}], 
}
```

 - bool 关键字表示我们要进行组合查询；
 - must 表示里面的条件都要满足，类似于SQL的AND关键字；
 - should 表示其中的条件满足其一就可以，类似于SQL的OR；
 - must_not 表示不需要满足该条件，类似于SQL的NOT；
 - 这里我们使用了与 must 并列的 filter，这里面可以输入多个条件，它只是根据条件做数据过滤，不进行打分等操作，在性能上优于match的查询，因此如果我们需要快速缩小查询范围时，就可以用filter。
 
 ### 模糊搜索与同义词搜索
 #### 模糊搜索
 当我们在query中使用 match 时，他默认已经支持了模糊搜索，这里的模糊搜索与我们在关系型数据库中的含义不太一样，在关系型数据库里表示我们要使用 LIKE 了，但是在ES里，表示我们输错了一两个字，它依然帮我们把数据找出来。
 让我们试试下面的搜索条件：
 

```javascript
GET /character/_search
{
  "query": {
    "match": {
      "des": "最后生活者"
    }
  }
}
```
我们的数据依然返回了，但是大家看到几条数据的score值，都没有满分，因为我们有错别字。

#### 同义词
该功能的使用需要对同义词做一些配置，这样我们就可以简化搜索，比如：
tlou = the last of us
这里我们不对这一块深入讲解了，因为其难点不在使用。

### 同一关键字在不同字段匹配
这个可以认为时对于 bool 操作的简化，我们来试一试：

```javascript
GET /character/_search
{
  "query": {
    "multi_match": {
      "query": "最后生活者",
      "fields": ["game", "des"]
    }
  }
}
```
## 删除数据
到这里，已经很累了，感觉已经精疲力尽了，饭也没吃厕所也没上的写，我们终于要开始做数据删除了，这个篇章也就快要接近收尾了。
![在这里插入图片描述](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9zczMuYmRzdGF0aWMuY29tLzcwY0Z2OFNoX1ExWW54R2twb1dLMUhGNmhoeS9pdC91PTEzNjM1MjUxMDMsMjEwNDM1OTU1MCZmbT0yNiZncD0wLmpwZw?x-oss-process=image/format,png#pic_center)
### 根据ID删除数据
这个语法比较简单，不过要注意不要一不小心删除了整个index

```javascript
//删除ID为1的角色
DELETE /character/_doc/1
```

### 根据查询条件删除
我们来让艾比离开最后生还者的世界吧：

```javascript
POST character/_delete_by_query
{
  "query": {
    "match": {
      "name": "艾比"
    }
  }
}
```
这样艾比就不见了，我们用_search去找时，再也找不到她了，TLOU的世界清净了。
是不是似曾相识的语法? 如果你很好的掌握了上面的复杂查询，那么这里将没有任何难度。
BTW，如果艾比的形象和艾莉丝与蒂法差不多，我们还是可以让她留下来并且接受她成为主角。
![在这里插入图片描述](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9zczEuYmRzdGF0aWMuY29tLzcwY0Z1WFNoX1ExWW54R2twb1dLMUhGNmhoeS9pdC91PTIzMDcyMDI0NzQsMjI5ODE4NzI1OSZmbT0yNiZncD0wLmpwZw?x-oss-process=image/format,png#pic_center)
## 更新数据
### 根据ID覆盖原数据
这个操作会根据id，覆盖原本的数据，不会只修改你提交的字段：

```javascript
PUT /character/_doc/2
{
  "des": "最后生还者1和2的女主角，在2的表现让人失望",
  "age": 16
}
```
这样提交后，ID为2的数据，就没有name与game属性了，那么显然这不是我们想要的。
### 只修改有变动的自动
通常我们只想要修改要改变的字段，没有变动的字段保持原样，那么我们该怎么做呢？
```javascript
POST /character/_update/2
{
  "doc": {
    "des": "最后生还者1和2的女主角，在2的表现让人失望",
    "age": 18
  }
}
```
这样提交后原本的name与game属性则不会发生变化。
### 根据查询条件进行数据修改
是的，现在我们并不知道数据的ID，我们想要找到艾莉，在我们写这段分享的漫长过程中，艾莉又长大了一岁，真是光阴似箭岁月如梭，艾莉已经19岁了，我们应该怎么办呢？

```javascript
POST character/_update_by_query
{
  "script": {
    "source": "ctx._source['age'] = 19"
  },
  "query": {
    "match": {
      "name": "艾莉"
    }
  }
}
```
query的部分依然参考上面的复杂查询模块，script是一个新知识点，但是我们也不做展开了，我们只要理解 "source": "ctx._source['age'] = 19" 是将查询到的数据的age字段改为19就可以了。

关于通过Kibana进行ES的CRUD操作，我们就介绍到这里了，不知不觉这已经是一篇万字长文了。

下一步，我们就要移步到通过 Springboot 与 Spring Data Elasticsearch 来使用ES了。想想还有些小激动，是不是。
![在这里插入图片描述](https://imgconvert.csdnimg.cn/aHR0cHM6Ly90aW1nc2EuYmFpZHUuY29tL3RpbWc_aW1hZ2UmcXVhbGl0eT04MCZzaXplPWI5OTk5XzEwMDAwJnNlYz0xNTkyOTE5MDk2Mzk1JmRpPTY0MTM4YzlmNTNkNGUwYjU0OWI4ZWRkOGIxNzI2N2E2JmltZ3R5cGU9MCZzcmM9aHR0cDovL2ltYWdlLmJpdGF1dG8uY29tL2RlYWxlci9uZXdzLzEwMDA0NjM5NC9jMzRjMDVjNS1iZGMwLTRmMDQtOTQwYy1hOWUxNTViYTVhODMuanBn?x-oss-process=image/format,png)
基础知识还要补充的小伙伴请前往：
[ElasticSearch 7.x with Springboot 2.3.x - 前言](https://blog.csdn.net/weixin_42288219/article/details/106883402)
[ElasticSearch 7.x with Springboot 2.3.x - 基础准备](https://blog.csdn.net/weixin_42288219/article/details/106908129)

# 让Springboot与ES结合

*Springboot 与 Spring data elasticsearch 为我们做了很多封装，有兴趣的小伙伴也可以试试自己直接在java中去操作ES，也不难就是麻烦一些，但是更有利于理解外部系统如何与ES交互。*

*这里我们不涉及这一块主要是在生产环境中我们不太会这么干，能少写代码就少写，不要折腾自己，节约出来的时间与精力可以喝个下午茶，想想怎么创新，好好感受一下这宝贵的一去不复返的人生。*
