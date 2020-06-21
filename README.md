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
