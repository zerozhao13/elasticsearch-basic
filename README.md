# ElasticSearch 7.x with Springboot 2.3.x

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

![](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1592484767433&di=866782a3acda52e951dfe94209d9bbe5&imgtype=0&src=http%3A%2F%2Fpic.baiqi008.com%2Fuploads%2Fkppnokdv.jpeg)


那这到底是怎么回事呢？新机子哇一直摸亿多姿，那就是全文检索与反向索引了，我们来看看如何做吧。

![](http://spider.nosdn.127.net/e0893fa3b8aa3f893dfe57f8e8d6b84f.gif)

1. 一首歌有歌名和歌词，分别存入数据库；
1. 对歌词根据关键字进行分词，英文默认以空格进行分词，中文默认以一个一个汉字分词（实际应用中文不能以此为逻辑分词，可以使用新的分词器去分词）；
1. 对歌词分词后，针对歌词建立索引（体会一下，和RDBMS的不一样，将一个字段基于关键词建了索引，而不是针对整个字段），索引标注了该关键字出自哪首歌；
1. 到了这里，想一想是不是找到感觉了？领悟了？
![](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1592486405257&di=5ad01be048c1fa59db3e76ca24d1b762&imgtype=0&src=http%3A%2F%2F5b0988e595225.cdn.sohucs.com%2Fq_70%2Cc_zoom%2Cw_640%2Fimages%2F20181020%2F15536137dd6a4d24a4969580b632751d.jpeg)
1. 真的懂了吗？(~~是的~~)不，(~~我懂了~~)你不懂。